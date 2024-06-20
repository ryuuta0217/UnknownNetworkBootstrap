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

import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Enchantments.class)
public abstract class MixinDamageEnchantment {
    @Shadow @Final public static ResourceKey<Enchantment> SHARPNESS;

    @ModifyArg(method = "bootstrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantments;register(Lnet/minecraft/data/worldgen/BootstrapContext;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/item/enchantment/Enchantment$Builder;)V", shift = At.Shift.BEFORE))
    private static Enchantment.Builder onBootstrap(BootstrapContext<Enchantment> ctx, ResourceKey<Enchantment> key, Enchantment.Builder builder) {
        if (key == SHARPNESS) {
            HolderGetter<Enchantment> enchantLookup = ctx.lookup(Registries.ENCHANTMENT);
            HolderGetter<Item> itemLookup = ctx.lookup(Registries.ITEM);
            return Enchantment.enchantment(
                            Enchantment.definition(
                                    itemLookup.getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                                    10,
                                    9,
                                    Enchantment.dynamicCost(1, 11),
                                    Enchantment.dynamicCost(12, 11),
                                    1,
                                    EquipmentSlotGroup.ARMOR
                            )
                    )
                    .exclusiveWith(enchantLookup.getOrThrow(EnchantmentTags.ARMOR_EXCLUSIVE))
                    .withEffect(
                            EnchantmentEffectComponents.DAMAGE_PROTECTION,
                            new AddValue(LevelBasedValue.perLevel(1.0F)),
                            DamageSourceCondition.hasDamageSource(
                                    DamageSourcePredicate.Builder.damageType().tag(TagPredicate.isNot(DamageTypeTags.BYPASSES_INVULNERABILITY))
                            ));
        } else {
            return builder;
        }
    }
}
