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

package net.unknown.launchwrapper.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class WrappedBlockPos {
    private ResourceKey<Level> level; // weak reference to level
    private final BlockPos blockPos;

    public WrappedBlockPos(ResourceKey<Level> level, BlockPos blockPos) {
        this.level = level;
        this.blockPos = blockPos;
    }

    public WrappedBlockPos(Level level, BlockPos blockPos) {
        this.level = level != null ? level.dimension() : Level.OVERWORLD;
        this.blockPos = blockPos;
    }

    @Deprecated
    public Level level() {
        return this.serverLevel();
    }

    @Deprecated
    public ServerLevel serverLevel() {
        return MinecraftServer.getServer().getLevel(this.level);
    }

    public ResourceKey<Level> levelKey() {
        return this.level;
    }

    public Level level(Level newLevel) {
        Level old = this.level();
        this.level = newLevel.dimension();
        return old;
    }

    public BlockPos blockPos() {
        return this.blockPos;
    }

    @Nullable
    public BlockEntity getBlockEntity(boolean load) {
        ServerLevel level = this.serverLevel();
        if (level != null) {
            if (!level.isLoaded(this.blockPos()) && load) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(this.blockPos().getX() >> 4, this.blockPos().getZ() >> 4);
                if (chunk != null) return chunk.getBlockEntity(this.blockPos());
            } else if (level.isLoaded(this.blockPos())) {
                return level.getBlockEntity(this.blockPos());
            }
        }
        return null;
    }
}
