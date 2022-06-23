package net.unknown.launchwrapper.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopperStairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.unknown.launchwrapper.util.CopperBlockUtil;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WeatheringCopperStairBlock.class)
public abstract class MixinWeatheringCopperStairBlock implements WeatheringCopper {
    @Override
    public void onRandomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        CopperBlockUtil.randomTick(this, state, world, pos, random);
    }
}
