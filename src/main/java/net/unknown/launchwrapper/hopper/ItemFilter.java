package net.unknown.launchwrapper.hopper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;

public record ItemFilter(Item item, @Nullable CompoundTag tag) {
}
