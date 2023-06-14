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
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.unknown.launchwrapper.linkchest.LinkChestMode;
import net.unknown.launchwrapper.mixininterfaces.IMixinChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChestBlock.class)
public abstract class MixinChestBlock extends BlockBehaviour {
    public MixinChestBlock(Properties settings) {
        super(settings);
    }

    @Inject(method = "onRemove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Containers;dropContents(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/Container;)V"))
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved, CallbackInfo ci) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof IMixinChestBlockEntity chest) {
            if (chest.getChestTransportMode() == LinkChestMode.CLIENT) {
                chest.setChestTransportMode(LinkChestMode.DISABLED);
            }
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof IMixinChestBlockEntity chest) {
            if (chest.isVoidChest()) {
                List<ItemStack> originalDrops = super.getDrops(state, builder);
                originalDrops.forEach(stack -> {
                    if (stack.getItem() == Items.CHEST) {
                        CompoundTag tag = stack.getOrCreateTag();

                        CompoundTag blockEntityTag = tag.contains("BlockEntityTag") ? tag.getCompound("BlockEntityTag") : new CompoundTag();
                        blockEntityTag.putBoolean("VoidChest", chest.isVoidChest());
                        tag.put("BlockEntityTag", blockEntityTag);

                        CompoundTag displayTag = tag.contains("display") ? tag.getCompound("display") : new CompoundTag();
                        displayTag.putString("Name", Component.Serializer.toJson(Component.literal("ゴミ箱").setStyle(Style.EMPTY.withColor(ChatFormatting.RED).withBold(true).withItalic(false))));
                        ListTag loreTag = displayTag.contains("Lore") ? displayTag.getList("Lore", Tag.TAG_STRING) : new ListTag();
                        loreTag.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal("ゴミ箱").setStyle(Style.EMPTY.withColor(ChatFormatting.RED).withItalic(true)))));
                        displayTag.put("Lore", loreTag);
                        tag.put("display", displayTag);
                    }
                });
                return originalDrops;
            }
        }
        return super.getDrops(state, builder);
    }
}
