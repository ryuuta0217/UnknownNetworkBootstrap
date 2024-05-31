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

import com.google.common.collect.Sets;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.unknown.launchwrapper.hopper.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mixin(HopperBlockEntity.class)
public abstract class MixinHopperBlockEntity extends RandomizableContainerBlockEntity implements IMixinHopperBlockEntity {
    @Shadow private static boolean skipPushModeEventFire;
    @Shadow public static boolean skipHopperEvents;

    @Shadow
    @Nullable
    protected static ItemStack callPushMoveEvent(Container iinventory, ItemStack itemstack, HopperBlockEntity hopper) {
        return null;
    }

    @Shadow
    public static ItemStack addItem(@Nullable Container from, Container to, ItemStack stack, @Nullable Direction side) {
        return null;
    }

    @Shadow
    private void setCooldown(int cooldown) {
    }

    public Set<Filter> filters = Sets.newHashSet();
    public FilterType filterMode = FilterType.DISABLED;

    private boolean findItem = true;

    private final ListTag findItem1 = new ListTag() {{
        add(0, DoubleTag.valueOf(-0.5D));
        add(1, DoubleTag.valueOf(0D));
        add(2, DoubleTag.valueOf(-0.5D));
    }};

    private final ListTag findItem2 = new ListTag() {{
        add(0, DoubleTag.valueOf(0.5D));
        add(1, DoubleTag.valueOf(1.5D));
        add(2, DoubleTag.valueOf(0.5D));
    }};

    protected MixinHopperBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * @author ryuuta0217
     * @reason Supports TransportType.PUSH_TO_CONTAINER
     */
    @Overwrite
    private static boolean hopperPush(final Level level, final Container destination, final Direction direction, final HopperBlockEntity hopper) {
        skipPushModeEventFire = skipHopperEvents;
        boolean foundItem = false;
        for (int i = 0; i < hopper.getContainerSize(); ++i) {
            final ItemStack item = hopper.getItem(i);
            if (!item.isEmpty()) {
                foundItem = true;
                boolean filterPassed = true; // Unknown Network - Supports filter

                // Unknown Network start - Supports filter
                if ((Hopper) hopper instanceof MixinHopperBlockEntity mHopper) {
                    if (mHopper.filterMode != FilterType.DISABLED) {
                        // ﾌｨﾙﾀ対象
                        boolean filteringResult = mHopper.filters.stream().anyMatch(filter -> filter.matches(item, TransportType.PUSH_TO_CONTAINER));

                        if (filteringResult) {
                            if (mHopper.getFilterMode() == FilterType.BLACKLIST) {
                                filterPassed = false;
                            }
                        } else {
                            if (mHopper.getFilterMode() == FilterType.WHITELIST) {
                                filterPassed = false;
                            }
                        }
                    }
                }
                // Unknown Network end

                if (filterPassed) { // Unknown Network - Supports filter
                    ItemStack origItemStack = item;
                    ItemStack movedItem = origItemStack;

                    final int originalItemCount = origItemStack.getCount();
                    final int movedItemCount = Math.min(level.spigotConfig.hopperAmount, originalItemCount);
                    origItemStack.setCount(movedItemCount);

                    // We only need to fire the event once to give protection plugins a chance to cancel this event
                    // Because nothing uses getItem, every event call should end up the same result.
                    if (!skipPushModeEventFire) {
                        movedItem = callPushMoveEvent(destination, movedItem, hopper);
                        if (movedItem == null) { // cancelled
                            origItemStack.setCount(originalItemCount);
                            return false;
                        }
                    }

                    final ItemStack remainingItem = addItem(hopper, destination, movedItem, direction);
                    final int remainingItemCount = remainingItem.getCount();
                    if (remainingItemCount != movedItemCount) {
                        origItemStack = origItemStack.copy(true);
                        origItemStack.setCount(originalItemCount);
                        if (!origItemStack.isEmpty()) {
                            origItemStack.setCount(originalItemCount - movedItemCount + remainingItemCount);
                        }
                        hopper.setItem(i, origItemStack);
                        destination.setChanged();
                        return true;
                    }
                    origItemStack.setCount(originalItemCount);
                } else {
                    foundItem = false; // Unknown Network - Supports filter
                }
            }
        }
        if (foundItem && level.paperConfig().hopper.cooldownWhenFull) { // Inventory was full - cooldown
            ((MixinHopperBlockEntity) ((Object) hopper)).setCooldown(level.spigotConfig.hopperTransfer);
        }
        return false;
    }

    @Inject(method = "hopperPull", at = @At("HEAD"), cancellable = true)
    private static void onHopperPull(Level level, Hopper hopper, Container container, ItemStack origItemStack, int i, CallbackInfoReturnable<Boolean> cir) {
        if (hopper instanceof MixinHopperBlockEntity mHopper) {
            if (mHopper.filterMode != FilterType.DISABLED) {
                // ﾌｨﾙﾀ対象
                boolean filteringResult = mHopper.filters.stream().anyMatch(filter -> filter.matches(origItemStack, TransportType.PULL_FROM_CONTAINER));

                if (filteringResult) {
                    if (mHopper.getFilterMode() == FilterType.BLACKLIST) {
                        cir.cancel();
                    }
                } else {
                    if (mHopper.getFilterMode() == FilterType.WHITELIST) {
                        cir.cancel();
                    }
                }
            }
        }
    }

    @Inject(method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/entity/item/ItemEntity;)Z", at = @At("HEAD"), cancellable = true)
    private static void onAddItem(Container inventory, ItemEntity itemEntity, CallbackInfoReturnable<Boolean> cir) {
        if (inventory instanceof MixinHopperBlockEntity hopper) {
            if (hopper.getFilterMode() != FilterType.DISABLED) {
                if (hopper.getFilters().size() > 0) {
                    boolean filterResult = hopper.getFilters().stream().anyMatch(filter -> filter.matches(itemEntity.getItem(), TransportType.PULL_FROM_DROPPED_ITEM));

                    if (hopper.getFilterMode() == FilterType.WHITELIST && !filterResult) {
                        cir.cancel();
                    }

                    if (hopper.getFilterMode() == FilterType.BLACKLIST && filterResult) {
                        cir.cancel();
                    }
                }
            }
        }
    }

    /**
     * for Customizable entity finding range and enabled/disabled
     *
     * @author ryuuta0217
     */
    @Inject(method = "getItemsAtAndAbove", at = @At("HEAD"), cancellable = true)
    private static void onGetItemsAtAndAbove(Level world, Hopper hopper, CallbackInfoReturnable<List<ItemEntity>> cir) {
        double d0 = hopper.getLevelX();
        double d1 = hopper.getLevelY();
        double d2 = hopper.getLevelZ();

        // UnknownNet start - Customizable entity finding range
        if (hopper instanceof MixinHopperBlockEntity hp) {
            if (!hp.isEnabledFindItem()) cir.setReturnValue(Collections.emptyList());
            else cir.setReturnValue(world.getEntitiesOfClass(ItemEntity.class, hp.getItemFindAABB(d0, d1, d2), Entity::isAlive));
            cir.cancel();
        }
        // UnknownNet end
    }

    @Inject(method = "loadAdditional", at = @At("RETURN"))
    public void onLoad(CompoundTag tag, HolderLookup.Provider registryLookup, CallbackInfo ci) {
        // {Filter: {Mode: WHITELIST, Filters: [{id: "minecraft:stick", components: {}}, {tag: "minecraft:logs", components: {}]}}
        if (tag.contains("Filter")) {
            CompoundTag filter = tag.getCompound("Filter");
            if (filter.contains("Mode")) {
                this.filterMode = FilterType.valueOfOrDefault(filter.getString("Mode").toUpperCase(), FilterType.DISABLED);

                if (filter.contains("Filters")) {
                    ListTag items = filter.getList("Filters", CompoundTag.TAG_COMPOUND);
                    this.filters.clear();
                    items.forEach(filterDataTag -> {
                        if (filterDataTag instanceof CompoundTag filterData) {
                            if (filterData.contains("id")) {
                                ResourceLocation id = ResourceLocation.tryParse(filterData.getString("id"));
                                Optional<Item> item = BuiltInRegistries.ITEM.getOptional(id);
                                if (item.isPresent()) {
                                    DataComponentPatch componentPatch = filterData.contains("components") ? DataComponentPatch.CODEC.parse(registryLookup.createSerializationContext(NbtOps.INSTANCE), filterData.get("components")).getOrThrow() : null;
                                    this.filters.add(new ItemFilter(item.get(), componentPatch));
                                }
                            }

                            if (filterData.contains("tag")) {
                                TagKey<Item> itemTag = TagKey.create(Registries.ITEM, new ResourceLocation(filterData.getString("tag")));
                                DataComponentPatch componentPatch = filterData.contains("nbt") ? DataComponentPatch.CODEC.parse(registryLookup.createSerializationContext(NbtOps.INSTANCE), filterData.get("components")).getOrThrow() : null;
                                this.filters.add(new TagFilter(itemTag, componentPatch));
                            }
                        }
                    });
                }
            }
        }

        // {"ItemFind":{"Active":"true", "Range":{"A":[-0.5, 0, -0.5], "B":[0.5, 1.5, 0.5]}}}
        if (tag.contains("ItemFind")) {
            CompoundTag tagItemFind = tag.getCompound("ItemFind");
            if (tagItemFind.contains("Active")) {
                this.findItem = tagItemFind.getBoolean("Active");
            }

            if (tagItemFind.contains("Range")) {
                CompoundTag Range = tagItemFind.getCompound("Range");
                if (Range.contains("A")) {
                    ListTag A = Range.getList("A", CompoundTag.TAG_LIST);
                    if (A.size() >= 3) {
                        this.findItem1.set(0, DoubleTag.valueOf(A.getDouble(0)));
                        this.findItem1.set(1, DoubleTag.valueOf(A.getDouble(1)));
                        this.findItem1.set(2, DoubleTag.valueOf(A.getDouble(2)));
                    }
                }

                if (Range.contains("B")) {
                    ListTag B = Range.getList("B", CompoundTag.TAG_LIST);
                    if (B.size() >= 3) {
                        this.findItem2.set(0, DoubleTag.valueOf(B.getDouble(0)));
                        this.findItem2.set(1, DoubleTag.valueOf(B.getDouble(1)));
                        this.findItem2.set(2, DoubleTag.valueOf(B.getDouble(2)));
                    }
                }
            }
        }
    }

    @Inject(method = "saveAdditional", at = @At("RETURN"))
    public void onSaveAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup, CallbackInfo ci) {
        CompoundTag filterRootTag = new CompoundTag();
        filterRootTag.putString("Mode", this.getFilterMode().name());

        ListTag filters = new ListTag();
        this.filters.forEach(filter -> {
            CompoundTag filterTag = new CompoundTag();
            if (filter instanceof ItemFilter itemFilter) {
                filterTag.putString("id", BuiltInRegistries.ITEM.getKey(itemFilter.getItem()).toString());
            } else if (filter instanceof TagFilter tagFilter) {
                filterTag.putString("tag", tagFilter.getTag().location().toString());
            }

            if (filter.getDataPatch() != null) {
                filterTag.put("components", DataComponentPatch.CODEC.encodeStart(registryLookup.createSerializationContext(NbtOps.INSTANCE), filter.getDataPatch()).getOrThrow());
            }
            filters.add(filterTag);
        });

        filterRootTag.put("Filters", filters);
        nbt.put("Filter", filterRootTag);

        CompoundTag ItemFind = new CompoundTag();
        ItemFind.putBoolean("Active", this.findItem);
        CompoundTag Range = new CompoundTag();
        Range.put("A", this.findItem1);
        Range.put("B", this.findItem2);
        ItemFind.put("Range", Range);
        nbt.put("ItemFind", ItemFind);
    }

    @Override
    public Set<Filter> getFilters() {
        return this.filters;
    }

    @Override
    public void setFilters(Set<Filter> filters) {
        this.filters = filters;
    }

    @Override
    public void addFilter(Filter filter) {
        this.filters.add(filter);
    }

    @Override
    public FilterType getFilterMode() {
        return this.filterMode;
    }

    @Override
    public void setFilterMode(FilterType filterMode) {
        this.filterMode = filterMode;
    }

    @Override
    public boolean isFilterEnabled() {
        return (this.filterMode != null && this.filterMode != FilterType.DISABLED) && !this.filters.isEmpty();
    }

    @Override
    public boolean isEnabledFindItem() {
        return this.findItem;
    }

    @Override
    public void setEnabledFindItem(boolean enabled) {
        this.findItem = enabled;
    }

    @Override
    public AABB getItemFindAABB(double baseX, double baseY, double baseZ) {
        return new AABB(baseX + this.findItem1.getDouble(0), baseY + this.findItem1.getDouble(1), baseZ + this.findItem1.getDouble(2), baseX + this.findItem2.getDouble(0), baseY + this.findItem2.getDouble(1), baseZ + this.findItem2.getDouble(2));
    }

    @Override
    public void setItemFindAABB(double aX, double aY, double aZ, double bX, double bY, double bZ) {
        this.findItem1.set(0, DoubleTag.valueOf(aX));
        this.findItem1.set(1, DoubleTag.valueOf(aY));
        this.findItem1.set(2, DoubleTag.valueOf(aZ));

        this.findItem2.set(0, DoubleTag.valueOf(bX));
        this.findItem2.set(1, DoubleTag.valueOf(bY));
        this.findItem2.set(2, DoubleTag.valueOf(bZ));
    }
}
