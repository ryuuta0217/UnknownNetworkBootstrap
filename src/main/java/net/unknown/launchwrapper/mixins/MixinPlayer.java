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

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;
import net.unknown.launchwrapper.enchantment.CustomEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nonnull;
import java.util.Optional;

@Mixin(Player.class)
public abstract class MixinPlayer extends LivingEntity {
    protected MixinPlayer(EntityType<? extends LivingEntity> type, Level world) {
        super(type, world);
    }

    @Shadow @Nonnull public abstract ItemStack getWeaponItem();

    @Shadow public abstract float getAttackStrengthScale(float baseTime);

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean onAttack(Entity entity, DamageSource source, float amount) {
        Optional<Holder.Reference<Enchantment>> doubleAttackEnchOpt = this.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(CustomEnchantments.DOUBLE_ATTACK);
        if (doubleAttackEnchOpt.isPresent()) {
            Holder.Reference<Enchantment> doubleAttackEnch = doubleAttackEnchOpt.get();
            int enchLevel = this.getWeaponItem().getEnchantments().getLevel(doubleAttackEnch);
            if (enchLevel > 0 && this.getAttackStrengthScale(0.5f) >= 0.8f) {
                if (this.level() != null && this.level().getLevelData() instanceof ServerLevelData levelData) {
                    levelData.getScheduledEvents().schedule("double_attack", this.level().getGameTime() + 11, (server, events, time) -> entity.hurt(source, amount));
                }
                return entity.hurt(source, amount);
            }
        }
        return entity.hurt(source, amount);
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean onLivingEntityAttack(LivingEntity entity, DamageSource source, float amount) {
        Optional<Holder.Reference<Enchantment>> doubleAttackEnchOpt = this.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(CustomEnchantments.DOUBLE_ATTACK);
        if (doubleAttackEnchOpt.isPresent()) {
            Holder.Reference<Enchantment> doubleAttackEnch = doubleAttackEnchOpt.get();
            int enchLevel = this.getWeaponItem().getEnchantments().getLevel(doubleAttackEnch);
            if (enchLevel > 0 && this.getAttackStrengthScale(0.5f) >= 0.8f) {
                if (this.level() != null && this.level().getLevelData() instanceof ServerLevelData levelData) {
                    levelData.getScheduledEvents().schedule("double_attack", this.level().getGameTime() + 11, (server, events, time) -> entity.hurt(source, amount));
                }
                return entity.hurt(source, amount);
            }
        }
        return entity.hurt(source, amount);
    }
}
