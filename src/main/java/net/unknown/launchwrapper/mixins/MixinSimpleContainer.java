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

import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SimpleContainer.class)
public class MixinSimpleContainer {
    @Shadow
    @Final
    @Mutable
    public NonNullList<ItemStack> items;
    @Shadow
    @Final
    @Mutable
    private int size;

    @Inject(method = "<init>(ILorg/bukkit/inventory/InventoryHolder;)V", at = @At("RETURN"))
    public void onInit(int i, InventoryHolder owner, CallbackInfo ci) {
        if ((Object) this instanceof PlayerEnderChestContainer) {
            this.size = 54; // force overwrite EnderChest size
            this.items = NonNullList.withSize(this.size, ItemStack.EMPTY); // re-create items list
        }
    }

    @Inject(method = "getContainerSize", at = @At("RETURN"), cancellable = true)
    public void getContainerSize(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof PlayerEnderChestContainer enderChest) {
            if (enderChest.getOwner() instanceof CraftPlayer p) {
                // TODO FEATURE: Reference UNC PlayerData#isBuyedSixRowsEnderChest ?
                // but requires a MultiProject
                if (p.isOp() || p.hasPermission("unknown.mixin.feature.ender_chest.six_rows")) cir.setReturnValue(54);
                else if (p.hasPermission("unknown.mixin.feature.ender_chest.five_rows")) cir.setReturnValue(45);
                else if (p.hasPermission("unknown.mixin.feature.ender_chest.four_rows")) cir.setReturnValue(36);
                else cir.setReturnValue(27);
            }
        }
    }
}
