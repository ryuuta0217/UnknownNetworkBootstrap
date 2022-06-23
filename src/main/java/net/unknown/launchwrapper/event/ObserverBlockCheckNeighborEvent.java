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
