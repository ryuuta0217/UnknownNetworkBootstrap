/*
 * Copyright (c) 2023 Unknown Network Developers and contributors.
 *
 * All rights reserved.
 *
 * NOTICE: This license is subject to change without prior notice.
 *
 * Redistribution and use in source and binary forms, *without modification*,
 *     are permitted provided that the following conditions are met:
 *
 * I. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 * II. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 * III. Neither the name of Unknown Network nor the names of its contributors may be used to
 *     endorse or promote products derived from this software without specific prior written permission.
 *
 * IV. This source code and binaries is provided by the copyright holders and contributors "AS-IS" and
 *     any express or implied warranties, including, but not limited to, the implied warranties of
 *     merchantability and fitness for a particular purpose are disclaimed.
 *     In not event shall the copyright owner or contributors be liable for
 *     any direct, indirect, incidental, special, exemplary, or consequential damages
 *     (including but not limited to procurement of substitute goods or services;
 *     loss of use data or profits; or business interruption) however caused and on any theory of liability,
 *     whether in contract, strict liability, or tort (including negligence or otherwise)
 *     arising in any way out of the use of this source code, event if advised of the possibility of such damage.
 */

package net.unknown.launchwrapper.mixins;

import co.aikar.timings.MinecraftTimings;
import net.minecraft.Util;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MCUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {
    @Shadow public abstract GameProfileCache getProfileCache();

    @Shadow @Final public static Logger LOGGER;

    @Shadow public abstract void onServerExit();

    @Shadow public LevelStorageSource.LevelStorageAccess storageSource;

    @Shadow private volatile boolean isSaving;

    @Shadow public MinecraftServer.ReloadableResources resources;

    @Shadow public abstract boolean saveAllChunks(boolean suppressLogs, boolean flush, boolean force);

    @Shadow public abstract Iterable<ServerLevel> getAllLevels();

    @Shadow public abstract boolean pollTask();

    @Shadow public boolean forceTicks;

    @Shadow private long nextTickTime;

    @Shadow @Final public Map<ResourceKey<Level>, ServerLevel> levels;

    @Shadow private PlayerList playerList;

    @Shadow private volatile boolean isRestarting;

    @Shadow @Nullable public abstract ServerConnectionListener getConnection();

    @Shadow public CraftServer server;

    @Shadow public abstract void cancelRecordingMetrics();

    @Shadow private MetricsRecorder metricsRecorder;

    @Shadow @Final private Object stopLock;

    @Shadow private boolean hasStopped;

    @Shadow public abstract boolean isDebugging();

    @Shadow private boolean hasLoggedStop;

    @Shadow public volatile Thread shutdownThread;

    @Shadow public abstract boolean isSameThread();

    @Shadow public abstract Thread getRunningThread();

    @Shadow public volatile boolean hasFullyShutdown;

    @Shadow public volatile boolean abnormalExit;

    /**
     * @return Server Modded Name
     * @author ryuuta0217
     * @reason Unknown Network - Unknown Network > // Paper - Paper > // Spigot - Spigot > // CraftBukkit - cb > vanilla!
     */
    @DontObfuscate
    @Overwrite
    public String getServerModName() {
        return "Unknown Network";
    }

    /**
     * @author ryuuta0217
     * @reason Print stacktrace on exit
     */
    @Overwrite
    public void stopServer() {
        // CraftBukkit start - prevent double stopping on multiple threads
        synchronized(this.stopLock) {
            if (this.hasStopped) return;
            this.hasStopped = true;
        }
        if (!hasLoggedStop && isDebugging()) io.papermc.paper.util.TraceUtil.dumpTraceForThread("Server stopped"); // Paper
        // Paper start - kill main thread, and kill it hard
        shutdownThread = Thread.currentThread();
        org.spigotmc.WatchdogThread.doStop(); // Paper
        if (!isSameThread()) {
            MinecraftServer.LOGGER.info("Stopping main thread (Ignore any thread death message you see! - DO NOT REPORT THREAD DEATH TO PAPER)");
            while (this.getRunningThread().isAlive()) {
                this.getRunningThread().stop();
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {}
            }
        }
        // Paper end
        // CraftBukkit end
        if (this.metricsRecorder.isRecording()) {
            this.cancelRecordingMetrics();
        }

        MinecraftServer.LOGGER.info("Stopping server");
        MinecraftTimings.stopServer(); // Paper
        // CraftBukkit start
        if (this.server != null) {
            this.server.disablePlugins();
            this.server.waitForAsyncTasksShutdown(); // Paper
        }
        // CraftBukkit end
        if (this.getConnection() != null) {
            this.getConnection().stop();
        }

        this.isSaving = true;
        if (this.playerList != null) {
            MinecraftServer.LOGGER.info("Saving players");
            this.playerList.saveAll();
            this.playerList.removeAll(this.isRestarting); // Paper
            try { Thread.sleep(100); } catch (InterruptedException ex) {} // CraftBukkit - SPIGOT-625 - give server at least a chance to send packets
        }

        MinecraftServer.LOGGER.info("Saving worlds");
        this.getAllLevels().forEach(level -> {
            if(level != null) {
                level.noSave = false;
            }
        });

        // Paper start - let's be a little more intelligent around crashes
        // make sure level.dat saves
        for (ServerLevel level : this.getAllLevels()) {
            level.saveLevelDat();
        }
        // Paper end - let's be a little more intelligent around crashes

        while(this.levels.values().stream().anyMatch(level -> level.getChunkSource().chunkMap.hasWork())) {
            this.nextTickTime = Util.getMillis() + 1L;

            this.getAllLevels().forEach(level -> {
                level.getChunkSource().removeTicketsOnClosing();
                level.getChunkSource().tick(() -> true, false);
                while(level.getChunkSource().pollTask()); // Paper - drain tasks
            });

            this.forceTicks = true; // Paper
            while (this.pollTask()); // Paper - drain tasks
        }

        this.saveAllChunks(false, true, false);
        this.getAllLevels().forEach(level -> {
            if(level != null) {
                try {
                    level.close();
                } catch (IOException e) {
                    MinecraftServer.LOGGER.error("Exception closing the level", e);
                }
            }
        });

        this.isSaving = false;
        this.resources.close();

        try {
            this.storageSource.close();
        } catch (IOException ioexception1) {
            MinecraftServer.LOGGER.error("Failed to unlock level {}", this.storageSource.getLevelId(), ioexception1);
        }
        // Spigot start
        MCUtil.asyncExecutor.shutdown(); // Paper
        try {
            MCUtil.asyncExecutor.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS); // Paper
        } catch (java.lang.InterruptedException ignored) {} // Paper

        if (org.spigotmc.SpigotConfig.saveUserCacheOnStopOnly) {
            MinecraftServer.LOGGER.info("Saving usercache.json");
            this.getProfileCache().save(false); // Paper
        }
        // Spigot end
        // Paper start - move final shutdown items here
        LOGGER.info("Flushing Chunk IO");
        com.destroystokyo.paper.io.PaperFileIOThread.Holder.INSTANCE.close(true, true); // Paper
        LOGGER.info("Closing Thread Pool");
        Util.shutdownExecutors(); // Paper
        LOGGER.info("Closing Server");

        try {
            net.minecrell.terminalconsole.TerminalConsoleAppender.close(); // Paper - Use TerminalConsoleAppender
        } catch (Exception e) {
        }

        this.onServerExit();
        // Paper end
    }
}
