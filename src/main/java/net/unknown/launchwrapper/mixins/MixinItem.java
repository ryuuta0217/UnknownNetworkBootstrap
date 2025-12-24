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

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.unknown.launchwrapper.mixininterfaces.IMixinItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public abstract class MixinItem implements IMixinItem {
    @Mutable
    @Shadow @Final private DataComponentMap components;

    @Shadow public abstract DataComponentMap components();

    @Override
    public <T> void setComponent(DataComponentType<T> type, T value) {
        this.components = this.getComponentBuilder().set(type, value).build();
    }

    @Override
    public DataComponentMap.Builder getComponentBuilder() {
        return DataComponentMap.builder().addAll(this.components);
    }

    /*@Inject(method = "verifyComponentsAfterLoad", at = @At("HEAD")) TODO: Implement
    private void onVerifyComponentsAfterLoad(ItemStack stack, CallbackInfo ci) {
        // Previous MixinItems implementation is set Egg's default DataComponent minecraft:max_stack_size to 64, but this is server-side only, so we need to *FORCE* set it here to apply client to max_stack_size to 64.
        // This operation will prevent you from setting the maximum stack size of eggs to 16. You can set it to 15 or 17, etc., but not 16.
        if ((Object) this instanceof EggItem) {
            if (stack.getComponents().has(DataComponents.MAX_STACK_SIZE) && stack.getComponents().get(DataComponents.MAX_STACK_SIZE).equals(this.components().get(DataComponents.MAX_STACK_SIZE))) {
                stack.applyComponents(DataComponentPatch.builder()
                        .set(DataComponents.MAX_STACK_SIZE, 64)
                        .build());
            }
        }
    }*/
}
