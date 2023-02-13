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
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.unknown.launchwrapper.hopper.FilterType;
import net.unknown.launchwrapper.hopper.IMixinHopperBlockEntity;
import net.unknown.launchwrapper.hopper.ItemFilter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
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
    public Set<ItemFilter> filters = Sets.newHashSet();
    public FilterType filterMode = FilterType.DISABLED;

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
                boolean filteringResult = mHopper.filters.stream().anyMatch(filter -> {
                    if (origItemStack.getItem().equals(filter.item())) {
                        return NbtUtils.compareNbt(origItemStack.getTag(), filter.tag(), true);
                    }
                    return false;
                });

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
                    boolean filterResult = hopper.getFilters().stream().anyMatch(filter -> {
                        if (itemEntity.getItem().getItem().equals(filter.item())) {
                            return NbtUtils.compareNbt(itemEntity.getItem().getTag(), filter.tag(), true);
                        }

                        //itemEntity.getServer().getTags().getTagOrThrow()

                        return false;
                    });

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
    @Overwrite
    public static List<ItemEntity> getItemsAtAndAbove(Level world, Hopper hopper) {

        // Paper start - Optimize item suck in. remove streams, restore 1.12 checks. Seriously checking the bowl?!
        double d0 = hopper.getLevelX();
        double d1 = hopper.getLevelY();
        double d2 = hopper.getLevelZ();

        // UnknownNet start - Customizable entity finding range
        if (hopper instanceof MixinHopperBlockEntity hp) {
            if (!hp.isEnabledFindItem()) return Collections.emptyList();
            return world.getEntitiesOfClass(ItemEntity.class, hp.getItemFindAABB(d0, d1, d2), Entity::isAlive);
        }
        // UnknownNet end

        AABB bb = new AABB(d0 - 0.5D, d1, d2 - 0.5D, d0 + 0.5D, d1 + 1.5D, d2 + 0.5D);
        return world.getEntitiesOfClass(ItemEntity.class, bb, Entity::isAlive);
        // Paper end
    }

    @Inject(method = "load", at = @At("RETURN"))
    public void onLoad(CompoundTag nbt, CallbackInfo ci) {
        // {"filter":{"mode":"whitelist", items:[{"id":"minecraft:stick", "tag":{}}]}}
        if (nbt.contains("filter")) {
            CompoundTag filter = nbt.getCompound("filter");
            if (filter.contains("mode")) {
                String modeStr = filter.getString("mode");
                FilterType mode = FilterType.DISABLED;
                if (modeStr.matches("(?i)(white|black)list")) mode = FilterType.valueOf(modeStr.toUpperCase());
                this.filterMode = mode;

                if (filter.contains("items")) {
                    ListTag items = filter.getList("items", CompoundTag.TAG_COMPOUND);
                    this.filters.clear();
                    items.forEach(filterDataTag -> {
                        if (filterDataTag instanceof CompoundTag filterData) {
                            if (filterData.contains("id")) {
                                String idStr = filterData.getString("id");
                                ResourceLocation id = ResourceLocation.tryParse(idStr);
                                Optional<Item> item = BuiltInRegistries.ITEM.getOptional(id);
                                if (item.isPresent()) {
                                    CompoundTag tag = filterData.getCompound("tag");
                                    this.filters.add(new ItemFilter(item.get(), tag));
                                }
                            }
                        }
                    });
                }
            }
        }

        // {"ItemFind":{"Active":"true", "Range":{"A":[-0.5, 0, -0.5], "B":[0.5, 1.5, 0.5]}}}
        if (nbt.contains("ItemFind")) {
            CompoundTag ItemFind = nbt.getCompound("ItemFind");
            if (ItemFind.contains("Range")) {
                CompoundTag Range = ItemFind.getCompound("Range");
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
    public void onSaveAdditional(CompoundTag nbt, CallbackInfo ci) {
        CompoundTag filterDataTag = new CompoundTag();
        filterDataTag.putString("mode", this.getFilterMode().name().toLowerCase());

        ListTag filters = new ListTag();
        this.filters.forEach(filter -> {
            CompoundTag filterTag = new CompoundTag();
            filterTag.putString("id", BuiltInRegistries.ITEM.getKey(filter.item()).toString());
            if (filter.tag() != null) filterTag.put("tag", filter.tag());
            filters.add(filterTag);
        });

        filterDataTag.put("items", filters);
        nbt.put("filter", filterDataTag);

        CompoundTag ItemFind = new CompoundTag();
        CompoundTag Range = new CompoundTag();
        Range.put("A", this.findItem1);
        Range.put("B", this.findItem2);
        ItemFind.put("Range", Range);
        nbt.put("ItemFind", ItemFind);
    }

    @Override
    public Set<ItemFilter> getFilters() {
        return this.filters;
    }

    @Override
    public void setFilters(Set<ItemFilter> filters) {
        this.filters = filters;
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
        return this.getBlockState().getValue(HopperBlock.ENABLED);
    }

    @Override
    public void setEnabledFindItem(boolean enabled) {
        this.getBlockState().setValue(HopperBlock.ENABLED, enabled);
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
