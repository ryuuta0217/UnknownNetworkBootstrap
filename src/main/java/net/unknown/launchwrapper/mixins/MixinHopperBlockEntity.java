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
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mixin(HopperBlockEntity.class)
public abstract class MixinHopperBlockEntity extends RandomizableContainerBlockEntity implements IMixinHopperBlockEntity {
    @Shadow @Final private static AABB HOPPER_ITEM_SUCK_OVERALL;
    @Shadow @Final private static AABB[] HOPPER_ITEM_SUCK_INDIVIDUAL;
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

    @Inject(method = "hopperPull", at = @At("HEAD"), cancellable = true)
    private static void onHopperPull(Level level, Hopper hopper, Container container, ItemStack origItemStack, int i, CallbackInfoReturnable<Boolean> cir) {
        if (hopper instanceof MixinHopperBlockEntity mHopper) {
            if (mHopper.filterMode != FilterType.DISABLED) {
                // ﾌｨﾙﾀ対象
                boolean filteringResult = mHopper.filters.stream().anyMatch(filter -> filter.matches(origItemStack));

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
                    boolean filterResult = hopper.getFilters().stream().anyMatch(filter -> filter.matches(itemEntity.getItem()));

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

    @Inject(method = "load", at = @At("RETURN"))
    public void onLoad(CompoundTag tag, CallbackInfo ci) {
        // {Filter: {Mode: WHITELIST, Filters: [{id: "minecraft:stick", nbt: {}}, {tag: "minecraft:logs", nbt: {}]}}
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
                                    CompoundTag nbt = filterData.contains("nbt") ? filterData.getCompound("nbt") : null;
                                    this.filters.add(new ItemFilter(item.get(), nbt));
                                }
                            }

                            if (filterData.contains("tag")) {
                                TagKey<Item> itemTag = TagKey.create(Registries.ITEM, new ResourceLocation(filterData.getString("tag")));
                                CompoundTag nbt = filterData.contains("nbt") ? filterData.getCompound("nbt") : null;
                                this.filters.add(new TagFilter(itemTag, nbt));
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
    }

    @Inject(method = "saveAdditional", at = @At("RETURN"))
    public void onSaveAdditional(CompoundTag nbt, CallbackInfo ci) {
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

            if (filter.getNbt() != null) {
                filterTag.put("nbt", filter.getNbt());
            }
            filters.add(filterTag);
        });

        filterRootTag.put("Filters", filters);
        nbt.put("Filter", filterRootTag);

        CompoundTag ItemFind = new CompoundTag();
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
