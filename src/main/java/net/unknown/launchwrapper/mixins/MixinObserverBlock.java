package net.unknown.launchwrapper.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.unknown.launchwrapper.event.ObserverBlockCheckNeighborEvent;
import net.unknown.launchwrapper.event.ObserverBlockTickEvent;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ObserverBlock.class)
public abstract class MixinObserverBlock extends Block {
    @Shadow protected abstract void updateNeighborsInFront(Level world, BlockPos pos, BlockState state);

    @Shadow protected abstract void startSignal(LevelAccessor world, BlockPos pos);

    private BlockState lastNeighborState = null;

    public MixinObserverBlock(Properties settings) {
        super(settings);
    }

    /**
     * @author ryuuta0217
     * @reason overwrite.
     */
    @Overwrite
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        ObserverBlockCheckNeighborEvent.WrappedData wrappedData = new ObserverBlockCheckNeighborEvent.WrappedData(state, direction, neighborState, world, pos, neighborPos);
        ObserverBlockCheckNeighborEvent event = new ObserverBlockCheckNeighborEvent((data) -> {
            boolean isDirectionValid = data.observer().getValue(ObserverBlock.FACING) == data.observerDirection();
            boolean isPowered = data.observer().getValue(ObserverBlock.POWERED);
            return isDirectionValid && !isPowered;
        }, wrappedData);

        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) return event.getObserver();
        if (event.getPredicate().test(wrappedData)) {
            this.startSignal(world, pos);
        }

        return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
    }

    /**
     * @author ryuuta0217
     * @reason Add Event
     */
    @Overwrite
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        ObserverBlockTickEvent event = new ObserverBlockTickEvent(state, world, pos, random);

        Bukkit.getPluginManager().callEvent(event);

        if(!event.isCancelled()) {
            if (event.getObserver().getValue(ObserverBlock.POWERED)) {
                // CraftBukkit start
                if (CraftEventFactory.callRedstoneChange(event.getLevel(), event.getObserverPos(), 15, 0).getNewCurrent() != 0) {
                    return;
                }
                // CraftBukkit end
                event.getLevel().setBlock(pos, event.getObserver().setValue(ObserverBlock.POWERED, false), Block.UPDATE_CLIENTS);
            } else {
                // CraftBukkit start
                if (CraftEventFactory.callRedstoneChange(event.getLevel(), event.getObserverPos(), 0, 15).getNewCurrent() != 15) {
                    return;
                }
                // CraftBukkit end
                event.getLevel().setBlock(pos, event.getObserver().setValue(ObserverBlock.POWERED, true), Block.UPDATE_CLIENTS);
                event.getLevel().scheduleTick(pos, this, 2);
            }
        }

        if(event.isUpdateFrontNeighbors()) this.updateNeighborsInFront(event.getLevel(), event.getObserverPos(), event.getObserver()); // ブロックの変化を前方のブロックに通知する
    }
}
