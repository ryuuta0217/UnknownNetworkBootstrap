package net.unknown.launchwrapper.hopper;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.phys.AABB;

import java.util.Set;

public interface IMixinHopperBlockEntity extends Hopper {
    Set<ItemFilter> getFilters();

    FilterType getFilterMode();

    AABB getItemFindAABB(double baseX, double baseY, double baseZ);

    NonNullList<ItemStack> getItems0();
}
