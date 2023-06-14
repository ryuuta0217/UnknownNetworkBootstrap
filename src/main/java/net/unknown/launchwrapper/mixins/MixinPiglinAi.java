package net.unknown.launchwrapper.mixins;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Iterator;

@Mixin(PiglinAi.class)
public class MixinPiglinAi {
    /**
     * @author ryuuta0217
     * @reason Piglins think that the player is their friend when the player is wearing a piglin's head.
     */
    @Overwrite
    public static boolean isWearingGold(LivingEntity entity) {
        Iterable<ItemStack> iterable = entity.getArmorSlots();
        Iterator<ItemStack> iterator = iterable.iterator();

        Item item;

        do {
            if (!iterator.hasNext()) {
                return false;
            }

            ItemStack itemstack = iterator.next();

            item = itemstack.getItem();
        } while (!(item instanceof ArmorItem) || ((ArmorItem) item).getMaterial() != ArmorMaterials.GOLD || item != Items.PIGLIN_HEAD);

        return true;
    }
}
