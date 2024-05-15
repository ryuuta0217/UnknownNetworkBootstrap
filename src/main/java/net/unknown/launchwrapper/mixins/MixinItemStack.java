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

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {
    @Shadow @Nullable public abstract CompoundTag getTagElement(String key);

    @Shadow protected abstract void processText();

    @Inject(method = "setTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;processEnchantOrder(Lnet/minecraft/nbt/CompoundTag;)V", shift = At.Shift.AFTER))
    private void onTagUpdated(CompoundTag nbt, CallbackInfo ci) {
        this.processText();
    }

    @Inject(method = "processText", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;contains(Ljava/lang/String;I)Z"))
    private void onProcessTextParsingLore(CallbackInfo ci) {
        CompoundTag display = this.getTagElement("display");
        if (display != null) {
            if (display.contains("Name", Tag.TAG_STRING)) {
                String json = display.getString("Name");
                if (json != null) {
                    display.put("Name", StringTag.valueOf(GsonComponentSerializer.gson().serialize(GsonComponentSerializer.gson().deserialize(json))));
                }
            }

            if (display.contains("Lore", Tag.TAG_LIST)) {
                ListTag list = display.getList("Lore", Tag.TAG_STRING);

                for (int i = 0; i < list.size(); i++) {
                    String json = list.getString(i);
                    if (json != null) {
                        list.set(i, StringTag.valueOf(GsonComponentSerializer.gson().serialize(GsonComponentSerializer.gson().deserialize(json))));
                    }
                }
            }
        }
    }
}
