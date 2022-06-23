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
