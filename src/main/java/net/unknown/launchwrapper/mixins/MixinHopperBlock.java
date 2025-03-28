/*
 * Copyright (c) 2023 Unknown Network Developers and contributors.
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

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.unknown.launchwrapper.hopper.IMixinHopperBlockEntity;
import net.unknown.launchwrapper.util.ComponentUtil;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;

@Mixin(HopperBlock.class)
public abstract class MixinHopperBlock extends BlockBehaviour {
    public MixinHopperBlock(Properties settings) {
        super(settings);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof IMixinHopperBlockEntity hopper) {
            if (hopper.getIncomingFilterMode() != null) {
                if (!hopper.getIncomingFilters().isEmpty() || !hopper.getOutgoingFilters().isEmpty()) {
                    List<ItemStack> drops = super.getDrops(state, builder);
                    drops.forEach(stack -> {
                        if (stack.getItem() == state.getBlock().asItem()) {
                            stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(((BlockEntity) hopper).saveWithId(MinecraftServer.getDefaultRegistryAccess())));

                            List<Component> styledLore = new ArrayList<>() {{
                                if (!hopper.getIncomingFilters().isEmpty()) {
                                    add(Component.literal("搬入フィルターモード: " + hopper.getIncomingFilterMode().getLocalizedName()).withStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.AQUA)));
                                    add(Component.literal("搬入フィルター登録数: " + hopper.getIncomingFilters().size()).withStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.AQUA)));
                                }
                                if (!hopper.getOutgoingFilters().isEmpty()) {
                                    add(Component.literal("搬出フィルターモード: " + hopper.getOutgoingFilterMode().getLocalizedName()).withStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.AQUA)));
                                    add(Component.literal("搬出フィルター登録数: " + hopper.getOutgoingFilters().size()).withStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.AQUA)));
                                }
                            }};

                            ItemLore lore = new ItemLore(ComponentUtil.stripStyles(styledLore), styledLore);
                            stack.set(DataComponents.LORE, lore);
                        }
                    });
                    return drops;
                }
            }
        }

        return super.getDrops(state, builder);
    }
}
