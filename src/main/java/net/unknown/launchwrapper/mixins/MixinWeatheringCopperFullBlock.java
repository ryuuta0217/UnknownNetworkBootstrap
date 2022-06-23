package net.unknown.launchwrapper.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopperFullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.unknown.launchwrapper.util.CopperBlockUtil;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nonnull;

@Mixin(WeatheringCopperFullBlock.class)
public abstract class MixinWeatheringCopperFullBlock implements WeatheringCopper {
    @Override
    public void onRandomTick(@Nonnull BlockState state, @Nonnull ServerLevel world, @Nonnull BlockPos pos, @Nonnull RandomSource random) {
        CopperBlockUtil.randomTick(this, state, world, pos, random);
    }
}
