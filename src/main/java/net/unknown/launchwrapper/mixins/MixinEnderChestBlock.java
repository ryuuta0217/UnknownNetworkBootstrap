package net.unknown.launchwrapper.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EnderChestBlock.class)
public class MixinEnderChestBlock {

    @Shadow @Final private static Component CONTAINER_TITLE;

    /**
     * Overwrite reason: Inject de yaruno kuso mendo-kusai
     *
     * @author ryuuta0217
     * @see net.unknown.launchwrapper.mixins.MixinSimpleContainer
     */
    @Overwrite
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        PlayerEnderChestContainer playerEnderChestContainer = player.getEnderChestInventory();
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof EnderChestBlockEntity) {
            BlockPos blockPos = pos.above();
            if (world.getBlockState(blockPos).isRedstoneConductor(world, blockPos)) {
                return InteractionResult.sidedSuccess(world.isClientSide);
            } else if (world.isClientSide) {
                return InteractionResult.SUCCESS;
            } else {
                EnderChestBlockEntity enderChestBlockEntity = (EnderChestBlockEntity) blockEntity;
                playerEnderChestContainer.setActiveChest(enderChestBlockEntity);
                player.openMenu(new SimpleMenuProvider((syncId, inventory, playerx) -> {
                    if (playerEnderChestContainer.getContainerSize() == 27)
                        return ChestMenu.threeRows(syncId, inventory, playerEnderChestContainer);
                    else if (playerEnderChestContainer.getContainerSize() == 36)
                        return new ChestMenu(MenuType.GENERIC_9x4, syncId, inventory, playerEnderChestContainer, 4);
                    else if (playerEnderChestContainer.getContainerSize() == 45)
                        return new ChestMenu(MenuType.GENERIC_9x5, syncId, inventory, playerEnderChestContainer, 5);
                    else if (playerEnderChestContainer.getContainerSize() == 54)
                        return ChestMenu.sixRows(syncId, inventory, playerEnderChestContainer);
                    else return ChestMenu.threeRows(syncId, inventory, playerEnderChestContainer);
                }, CONTAINER_TITLE));
                player.awardStat(Stats.OPEN_ENDERCHEST);
                PiglinAi.angerNearbyPiglins(player, true);
                return InteractionResult.CONSUME;
            }
        } else {
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
    }
}
