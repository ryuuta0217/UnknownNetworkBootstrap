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

package net.unknown.launchwrapper.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Predicate;

public class ObserverBlockCheckNeighborEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private Predicate<WrappedData> predicate;
    private final WrappedData data;
    private boolean isCancelled;

    public ObserverBlockCheckNeighborEvent(@Nonnull Predicate<WrappedData> predicate, @Nonnull WrappedData data) {
        this.predicate = predicate;
        this.data = data;
    }

    public Predicate<WrappedData> getPredicate() {
        return this.predicate;
    }

    public void setPredicate(@Nonnull Predicate<WrappedData> predicate) {
        Objects.requireNonNull(predicate);
        this.predicate = predicate;
    }

    public LevelAccessor getLevel() {
        return this.data.level();
    }

    public BlockState getObserver() {
        return this.data.observer();
    }

    public BlockPos getObserverPos() {
        return this.data.observerPos();
    }

    public Direction getObserverDirection() {
        return this.data.observerDirection();
    }

    public BlockState getNeighborState() {
        return this.data.neighborState();
    }

    public BlockPos getNeighborPos() {
        return this.data.neighborPos();
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

    public record WrappedData(BlockState observer, Direction observerDirection, BlockState neighborState, LevelAccessor level, BlockPos observerPos, BlockPos neighborPos) {

    }
}
