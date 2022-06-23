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

package net.unknown.launchwrapper.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ObserverBlockTickEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final BlockState state;
    private final ServerLevel level;
    private final BlockPos pos;
    private final RandomSource random;
    private boolean isUpdateFrontNeighbors = true;
    private boolean isCancelled = false;

    public ObserverBlockTickEvent(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        this.state = state;
        this.level = level;
        this.pos = pos;
        this.random = random;
    }

    public BlockState getObserver() {
        return this.state;
    }

    public Direction getObserverDirection() {
        return this.state.getValue(ObserverBlock.FACING);
    }

    public ServerLevel getLevel() {
        return this.level;
    }

    public BlockPos getObserverPos() {
        return this.pos;
    }

    @Nullable
    public BlockState getNeighbor() {
        return this.getLevel().getBlockStateIfLoaded(this.getNeighborPos());
    }

    public BlockPos getNeighborPos() {
        return this.pos.relative(this.getObserverDirection());
    }

    public RandomSource getRandom() {
        return this.random;
    }

    public boolean isUpdateFrontNeighbors() {
        return this.isUpdateFrontNeighbors;
    }

    public void setUpdateFrontNeighbors(boolean update) {
        this.isUpdateFrontNeighbors = update;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    @Nonnull
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
