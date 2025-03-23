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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.unknown.launchwrapper.event.ObserverBlockCheckNeighborEvent;
import net.unknown.launchwrapper.event.ObserverBlockRemoveEvent;
import net.unknown.launchwrapper.event.ObserverBlockTickEvent;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ObserverBlock.class)
public abstract class MixinObserverBlock extends Block {
    @Shadow protected abstract void updateNeighborsInFront(Level world, BlockPos pos, BlockState state);

    @Shadow protected abstract void startSignal(LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos);

    private BlockState lastNeighborState = null;

    public MixinObserverBlock(Properties settings) {
        super(settings);
    }

    /**
     * @author ryuuta0217
     * @reason overwrite.
     */
    @Overwrite
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        ObserverBlockCheckNeighborEvent.WrappedData wrappedData = new ObserverBlockCheckNeighborEvent.WrappedData(state, direction, neighborState, (LevelAccessor) level, pos, neighborPos);
        ObserverBlockCheckNeighborEvent event = new ObserverBlockCheckNeighborEvent((data) -> {
            boolean isDirectionValid = data.observer().getValue(ObserverBlock.FACING) == data.observerDirection();
            boolean isPowered = data.observer().getValue(ObserverBlock.POWERED);
            return isDirectionValid && !isPowered;
        }, wrappedData);

        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) return event.getObserver();
        if (event.getPredicate().test(wrappedData)) {
            this.startSignal(level, scheduledTickAccess, pos);
        }

        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    /**
     * @author ryuuta0217
     * @reason Add Event
     */
    @Overwrite
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        ObserverBlockTickEvent event = new ObserverBlockTickEvent(state, world, pos, random);

        Bukkit.getPluginManager().callEvent(event);

        if(!event.isCancelled()) {
            if (event.getObserver().getValue(ObserverBlock.POWERED)) {
                // CraftBukkit start
                if (CraftEventFactory.callRedstoneChange(event.getLevel(), event.getObserverPos(), 15, 0).getNewCurrent() != 0) {
                    return;
                }
                // CraftBukkit end
                event.getLevel().setBlock(pos, event.getObserver().setValue(ObserverBlock.POWERED, false), Block.UPDATE_CLIENTS);
            } else {
                // CraftBukkit start
                if (CraftEventFactory.callRedstoneChange(event.getLevel(), event.getObserverPos(), 0, 15).getNewCurrent() != 15) {
                    return;
                }
                // CraftBukkit end
                event.getLevel().setBlock(pos, event.getObserver().setValue(ObserverBlock.POWERED, true), Block.UPDATE_CLIENTS);
                event.getLevel().scheduleTick(pos, this, 2);
            }
        }

        if(event.isUpdateFrontNeighbors()) this.updateNeighborsInFront(event.getLevel(), event.getObserverPos(), event.getObserver()); // ブロックの変化を前方のブロックに通知する
    }

    @Inject(method = "onRemove", at = @At("HEAD"))
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved, CallbackInfo ci) {
        ObserverBlockRemoveEvent event = new ObserverBlockRemoveEvent(state, world, pos, newState, moved);
        Bukkit.getPluginManager().callEvent(event);
    }
}
