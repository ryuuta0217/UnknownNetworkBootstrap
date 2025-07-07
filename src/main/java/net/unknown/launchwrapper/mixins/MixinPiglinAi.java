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

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.ArrayList;
import java.util.Iterator;

@Mixin(PiglinAi.class)
public class MixinPiglinAi {
    /**
     * @author ryuuta0217
     * @reason Piglins think that the player is their friend when the player is wearing a piglin's head.
     */
    @Overwrite
    public static boolean isWearingSafeArmor(LivingEntity entity) {
        Iterable<ItemStack> iterable = new ArrayList<>() {{
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = entity.getItemBySlot(slot);
                add(stack);
            }
        }};
        Iterator<ItemStack> iterator = iterable.iterator();

        ItemStack stack;

        do {
            if (!iterator.hasNext()) {
                return false;
            }

            stack = iterator.next();
        } while (!stack.is(Items.PIGLIN_HEAD) && !stack.is(ItemTags.PIGLIN_SAFE_ARMOR));

        return true;
    }
}
