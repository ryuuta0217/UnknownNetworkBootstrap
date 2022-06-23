/*
 * Copyright (c) 2022 Unknown Network Developers and contributors.
 *
 * All rights reserved.
 *
 * NOTICE: This license is subject to change without prior notice.
 *
 * Redistribution and use in source and binary forms, *without modification*,
 *     are permitted provided that the following conditions are met:
 *
 * I. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 * II. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 * III. Neither the name of Unknown Network nor the names of its contributors may be used to
 *     endorse or promote products derived from this software without specific prior written permission.
 *
 * IV. This source code and binaries is provided by the copyright holders and contributors "AS-IS" and
 *     any express or implied warranties, including, but not limited to, the implied warranties of
 *     merchantability and fitness for a particular purpose are disclaimed.
 *     In not event shall the copyright owner or contributors be liable for
 *     any direct, indirect, incidental, special, exemplary, or consequential damages
 *     (including but not limited to procurement of substitute goods or services;
 *     loss of use data or profits; or business interruption) however caused and on any theory of liability,
 *     whether in contract, strict liability, or tort (including negligence or otherwise)
 *     arising in any way out of the use of this source code, event if advised of the possibility of such damage.
 */

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
