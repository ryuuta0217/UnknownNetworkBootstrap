package net.unknown.launchwrapper.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class CopperBlockUtil {
    public static void randomTick(@Nonnull WeatheringCopper copper, @Nonnull BlockState state, @Nonnull ServerLevel world, @Nonnull BlockPos pos, @Nonnull RandomSource random) {
        if (random.nextFloat() < 0.05688889F) { // Vanilla
            copper.applyChangeOverTime(state, world, pos, random);
        }

        int r = random.nextInt(1, 3);
        System.out.println("r: " + r);
        if (world.getBlockState(pos.below()).is(Blocks.CAMPFIRE) && r == 2) { // Unknown Network
            System.out.println();
            copper.applyChangeOverTime(state, world, pos, random);
        }
    }
}
