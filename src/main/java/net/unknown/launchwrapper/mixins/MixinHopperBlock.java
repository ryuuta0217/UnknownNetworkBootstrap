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

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.unknown.launchwrapper.hopper.IMixinHopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collections;
import java.util.List;

@Mixin(HopperBlock.class)
public abstract class MixinHopperBlock extends BlockBehaviour {
    public MixinHopperBlock(Properties settings) {
        super(settings);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof IMixinHopperBlockEntity hopper) {
            if (hopper.getFilterMode() != null) {
                if (hopper.getFilters().size() > 0) {
                    ItemStack is = new ItemStack(state.getBlock().asItem());
                    CompoundTag tag = is.getOrCreateTag();
                    tag.put("BlockEntityTag", ((BlockEntity) hopper).saveWithoutMetadata());
                    CompoundTag display = new CompoundTag();
                    ListTag lore = new ListTag();
                    lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent("フィルターモード: " + hopper.getFilterMode().getLocalizedName()).withStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.AQUA)))));
                    lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent("登録フィルター数: " + hopper.getFilters().size()).withStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.AQUA)))));
                    display.put("Lore", lore);
                    tag.put("display", display);
                    return Collections.singletonList(is);
                }
            }
        }

        return super.getDrops(state, builder);
    }
}
