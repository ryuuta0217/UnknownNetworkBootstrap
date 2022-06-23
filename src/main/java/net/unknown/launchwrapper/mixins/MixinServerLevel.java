/*
 * Copyright (c) 2022 Unknown Network Developers and contributors.
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

import io.papermc.paper.configuration.WorldConfiguration;
import io.papermc.paper.util.math.ThreadUnsafeRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.spigotmc.SpigotWorldConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel extends Level {
    @Shadow @Final private ThreadUnsafeRandom randomTickRandom;

    protected MixinServerLevel(WritableLevelData worlddatamutable, ResourceKey<Level> resourcekey, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean flag, boolean flag1, long i, int j, ChunkGenerator gen, BiomeProvider biomeProvider, World.Environment env, Function<SpigotWorldConfig, WorldConfiguration> paperWorldConfigCreator, Executor executor) {
        super(worlddatamutable, resourcekey, holder, supplier, flag, flag1, i, j, gen, biomeProvider, env, paperWorldConfigCreator, executor);
    }

    @Inject(method = "neighborChanged(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/core/BlockPos;)V", at = @At("HEAD"))
    public void onUpdateNeighborsAt(BlockPos blockPos, Block block, BlockPos originalPos, CallbackInfo ci) {
        if ((Object) this instanceof ServerLevel level) {
            if (this.isLoaded(blockPos)) {
                BlockState blockState = this.getBlockState(blockPos);
                if (blockState.getBlock() instanceof LeavesBlock || blockState.is(BlockTags.LEAVES)) {
                    if (blockState.hasProperty(LeavesBlock.PERSISTENT) && !blockState.getValue(LeavesBlock.PERSISTENT)) {
                        blockState.setValue(LeavesBlock.DISTANCE, LeavesBlock.DECAY_DISTANCE);
                        blockState.randomTick(level, blockPos, this.randomTickRandom);
                    }
                }
            }
        }
    }
}
