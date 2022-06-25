package net.unknown.launchwrapper.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

public class ObserverBlockRemoveEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final BlockState state;
    private final Level world;
    private final BlockPos pos;
    private final BlockState newState;
    private final boolean moved;

    public ObserverBlockRemoveEvent(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        this.state = state;
        this.world = world;
        this.pos = pos;
        this.newState = newState;
        this.moved = moved;
    }

    public BlockState getState() {
        return this.state;
    }

    public Level getWorld() {
        return this.world;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public BlockState getNewState() {
        return this.newState;
    }

    public boolean isMoved() {
        return this.moved;
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
