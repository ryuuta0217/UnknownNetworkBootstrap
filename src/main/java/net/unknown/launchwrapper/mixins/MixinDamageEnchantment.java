package net.unknown.launchwrapper.mixins;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DamageEnchantment.class)
public abstract class MixinDamageEnchantment extends Enchantment {
    @Shadow @Final public int type;

    protected MixinDamageEnchantment(Rarity rarity, EnchantmentCategory target, EquipmentSlot[] slotTypes) {
        super(rarity, target, slotTypes);
    }

    @Override
    public int getMaxLevel() {
        return this.type == DamageEnchantment.ALL ? 10 : 5;
    }
}
