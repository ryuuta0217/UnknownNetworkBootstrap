package net.unknown.launchwrapper.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class WrappedBlockPos {
    private Level level;
    private final BlockPos blockPos;

    public WrappedBlockPos(Level level, BlockPos blockPos) {
        this.level = level;
        this.blockPos = blockPos;
    }

    public Level level() {
        return this.level;
    }

    public Level level(Level newLevel) {
        Level old = this.level;
        this.level = newLevel;
        return old;
    }

    public BlockPos blockPos() {
        return this.blockPos;
    }
}
