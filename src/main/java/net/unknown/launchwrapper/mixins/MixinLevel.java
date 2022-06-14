package net.unknown.launchwrapper.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(Level.class)
public abstract class MixinLevel {

    @Shadow public abstract boolean isLoaded(BlockPos pos);

    @Shadow public abstract BlockState getBlockState(BlockPos pos);

    @Shadow public abstract Random getRandom();

    @Inject(method = "neighborChanged", at = @At("HEAD"))
    public void onUpdateNeighborsAt(BlockPos blockPos, Block block, BlockPos originalPos, CallbackInfo ci) {
        if((Object) this instanceof ServerLevel level) {
            if (this.isLoaded(blockPos)) {
                BlockState blockState = this.getBlockState(blockPos);
                if(blockState.getBlock() instanceof LeavesBlock || BlockTags.LEAVES.contains(blockState.getBlock())) {
                    if (blockState.hasProperty(LeavesBlock.PERSISTENT) && !blockState.getValue(LeavesBlock.PERSISTENT)) {
                        blockState.setValue(LeavesBlock.DISTANCE, LeavesBlock.DECAY_DISTANCE);
                        blockState.randomTick(level, blockPos, this.getRandom());
                    }
                }
            }
        }
    }
}
