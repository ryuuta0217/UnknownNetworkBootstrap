package net.unknown.launchwrapper.mixins;

import io.papermc.paper.configuration.WorldConfiguration;
import io.papermc.paper.util.math.ThreadUnsafeRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.spigotmc.SpigotWorldConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel extends Level {
    @Shadow @Final private ThreadUnsafeRandom randomTickRandom;

    protected MixinServerLevel(WritableLevelData worlddatamutable, ResourceKey<Level> resourcekey, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean flag, boolean flag1, long i, int j, ChunkGenerator gen, BiomeProvider biomeProvider, World.Environment env, Function<SpigotWorldConfig, WorldConfiguration> paperWorldConfigCreator, Executor executor) {
        super(worlddatamutable, resourcekey, holder, supplier, flag, flag1, i, j, gen, biomeProvider, env, paperWorldConfigCreator, executor);
    }

    @Inject(method = "neighborChanged(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/core/BlockPos;)V", at = @At("HEAD"))
    public void onUpdateNeighborsAt(BlockPos blockPos, Block block, BlockPos originalPos, CallbackInfo ci) {
        if ((Object) this instanceof ServerLevel level) {
            if (this.isLoaded(blockPos)) {
                BlockState blockState = this.getBlockState(blockPos);
                if (blockState.getBlock() instanceof LeavesBlock || blockState.is(BlockTags.LEAVES)) {
                    if (blockState.hasProperty(LeavesBlock.PERSISTENT) && !blockState.getValue(LeavesBlock.PERSISTENT)) {
                        blockState.setValue(LeavesBlock.DISTANCE, LeavesBlock.DECAY_DISTANCE);
                        blockState.randomTick(level, blockPos, this.randomTickRandom);
                    }
                }
            }
        }
    }
}
