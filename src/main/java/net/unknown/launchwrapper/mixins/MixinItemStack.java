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

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.unknown.launchwrapper.mixininterfaces.IMixinItemStackWhoCrafted;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(ItemStack.class)
public abstract class MixinItemStack implements IMixinItemStackWhoCrafted {
    @Unique
    private static final String TAG_WHO_CRAFTED = "WhoCrafted";

    private UUID whoCrafted = Util.NIL_UUID;

    @Override
    public UUID getWhoCrafted() {
        return this.whoCrafted;
    }

    @Override
    public void setWhoCrafted(UUID whoCrafted) {
        this.whoCrafted = whoCrafted;
    }

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;getByte(Ljava/lang/String;)B", shift = At.Shift.AFTER))
    private void onLoad(CompoundTag nbt, CallbackInfo ci) {
        this.whoCrafted = nbt.contains(TAG_WHO_CRAFTED, CompoundTag.TAG_INT_ARRAY) ? nbt.getUUID(TAG_WHO_CRAFTED) : Util.NIL_UUID;

        // Migration from Item's Tag
        if (nbt.contains("tag", CompoundTag.TAG_COMPOUND) && nbt.getCompound("tag").contains(TAG_WHO_CRAFTED, CompoundTag.TAG_INT_ARRAY)) {
            if (this.whoCrafted.equals(Util.NIL_UUID)) this.whoCrafted = nbt.getCompound("tag").getUUID(TAG_WHO_CRAFTED);
            nbt.getCompound("tag").remove(TAG_WHO_CRAFTED);
        }
    }

    @Inject(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;putByte(Ljava/lang/String;B)V", shift = At.Shift.AFTER))
    private void onSave(CompoundTag nbt, CallbackInfoReturnable<CompoundTag> cir) {
        nbt.putUUID(TAG_WHO_CRAFTED, this.whoCrafted);
    }

    @Redirect(method = "copy(Z)Lnet/minecraft/world/item/ItemStack;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;setPopTime(I)V"))
    private void onCopy(ItemStack instance, int bobbingAnimationTime) {
        instance.setPopTime(bobbingAnimationTime); // Original call

        if ((Object) instance instanceof IMixinItemStackWhoCrafted mixinStack) { // Additional call
            mixinStack.setWhoCrafted(this.whoCrafted);
        }
    }
}
