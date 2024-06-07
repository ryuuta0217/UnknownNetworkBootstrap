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
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
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
import java.util.*;

// TODO 搬入、搬出でフィルタを分ける (これまでのFilterは搬入フィルタとする)
@Mixin(HopperBlockEntity.class)
public abstract class MixinHopperBlockEntity extends RandomizableContainerBlockEntity implements Hopper, IMixinHopperBlockEntity {
    /* SHADOW */
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

    @Shadow private static boolean skipPullModeEventFire;

    @Shadow
    @Nullable
    protected static Container getSourceContainer(Level world, Hopper hopper, BlockPos pos, BlockState state) {
        return null;
    }

    @Shadow
    protected static int[] getSlots(Container inventory, Direction side) {
        return null;
    }

    @Shadow
    protected static boolean tryTakeInItemFromSlot(Hopper ihopper, Container iinventory, int i, Direction enumdirection, Level world) {
        return false;
    }

    /* SHADOW */

    // Unknown Network start - Add Filter
    public Set<Filter> incomingFilters = Sets.newHashSet();
    public FilterType incomingFilterMode = FilterType.DISABLED;
    public Set<Filter> outgoingFilters = Sets.newHashSet();
    public FilterType outgoingFilterMode = FilterType.DISABLED;
    // Unknown Network end

    // Unknown Network start - Add findItem, pullItem, pushItem and disableEvent, debug
    private boolean findItem = true;
    private boolean pullItem = true;
    private boolean pushItem = true;
    private boolean disableEvent = false;
    // Unknown Network end

    // Unknown Network start - Add findItem range
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
    // Unknown Network end

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
        if (hopper instanceof IMixinHopperBlockEntity mHopper && !mHopper.isEnabledPushItem()) {
            return false; // Unknown Network - Supports disable pushItem
        }

        boolean foundItem = false;
        for (int i = 0; i < hopper.getContainerSize(); ++i) {
            final ItemStack item = hopper.getItem(i);
            if (!item.isEmpty()) {
                boolean filterPassed = true; // Unknown Network - Supports filter

                // Unknown Network start - Supports filter
                if ((Hopper) hopper instanceof IMixinHopperBlockEntity mHopper) {
                    filterPassed = processFilter(mHopper, item, TransportType.PUSH_TO_CONTAINER);
                }
                // Unknown Network end

                if (filterPassed) { // Unknown Network - Supports filter
                    foundItem = true;
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
                }
            }
        }
        if (foundItem && level.paperConfig().hopper.cooldownWhenFull) { // Inventory was full - cooldown
            ((MixinHopperBlockEntity) ((Object) hopper)).setCooldown(level.spigotConfig.hopperTransfer);
        }
        return false;
    }

    /**
     * @author ryuuta0217
     * @reason for debug
     */
    @Overwrite
    public static boolean suckInItems(Level world, Hopper hopper) {
        BlockPos blockposition = BlockPos.containing(hopper.getLevelX(), hopper.getLevelY() + 1.0D, hopper.getLevelZ());
        BlockState iblockdata = world.getBlockState(blockposition);
        Container iinventory = getSourceContainer(world, hopper, blockposition, iblockdata);

        if (iinventory != null) {
            Direction enumdirection = Direction.DOWN;
            skipPullModeEventFire = skipHopperEvents; // Paper - Perf: Optimize Hoppers
            int[] aint = getSlots(iinventory, enumdirection);
            int i = aint.length;

            for (int j = 0; j < i; ++j) {
                int k = aint[j];

                if (tryTakeInItemFromSlot(hopper, iinventory, k, enumdirection, world)) { // Spigot
                    return true;
                }
            }

            return false;
        } else {
            boolean flag = hopper.isGridAligned() && iblockdata.isCollisionShapeFullBlock(world, blockposition) && !iblockdata.is(BlockTags.DOES_NOT_BLOCK_HOPPERS);

            if (!flag) {
                for (ItemEntity item : HopperBlockEntity.getItemsAtAndAbove(world, hopper)) {
                    if (HopperBlockEntity.addItem(hopper, item)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }


    @Inject(method = "hopperPull", at = @At("HEAD"), cancellable = true)
    private static void onHopperPull(Level level, Hopper hopper, Container container, ItemStack origItemStack, int i, CallbackInfoReturnable<Boolean> cir) {
        if (hopper instanceof IMixinHopperBlockEntity mHopper) {
            if (mHopper.getIncomingFilterMode() != FilterType.DISABLED) {
                if (!processFilter(mHopper, origItemStack, TransportType.PULL_FROM_CONTAINER)) {
                    cir.setReturnValue(false);
                    cir.cancel();
                }
            }
        }
    }

    @Inject(method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/entity/item/ItemEntity;)Z", at = @At("HEAD"), cancellable = true)
    private static void onAddItem(Container inventory, ItemEntity itemEntity, CallbackInfoReturnable<Boolean> cir) {
        if (inventory instanceof IMixinHopperBlockEntity hopper) {
            if (!hopper.isEnabledPullItem()) {
                cir.setReturnValue(false);
                cir.cancel();
            }

            if (isFilteringEnabled(hopper, TransportType.PULL_FROM_DROPPED_ITEM)) {
                if (!processFilter(hopper, itemEntity.getItem(), TransportType.PULL_FROM_DROPPED_ITEM)) {
                    cir.setReturnValue(false);
                    cir.cancel();
                }
            }
        }
    }

    @Inject(method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private static void onAddItemFromContainer(Container from, Container _to, ItemStack stack, Direction side, CallbackInfoReturnable<ItemStack> cir) {
        if (from instanceof IMixinHopperBlockEntity hopper) {
            if (isFilteringEnabled(hopper, TransportType.PULL_FROM_CONTAINER)) {
                if (!processFilter(hopper, stack, TransportType.PULL_FROM_CONTAINER)) {
                    cir.setReturnValue(stack);
                    cir.cancel();
                }
            }
        }

        if (_to instanceof IMixinHopperBlockEntity hopper) {
            if (isFilteringEnabled(hopper, TransportType.PUSH_TO_CONTAINER)) {
                if (!processFilter(hopper, stack, TransportType.PUSH_TO_CONTAINER)) {
                    cir.setReturnValue(stack);
                    cir.cancel();
                }
            }
        }
    }

    // ホッパーが任意のコンテナからアイテムを吸引しようとしたとき、それが可能であるかを返します。
    // コンテナは別のホッパーの可能性があります。
    @Inject(method = "canTakeItemFromContainer", at = @At("HEAD"), cancellable = true)
    private static void onCanTakeItemFromContainer(Container hopperInventory, Container fromInventory, ItemStack stack, int slot, Direction facing, CallbackInfoReturnable<Boolean> cir) {
        if (hopperInventory instanceof IMixinHopperBlockEntity hopper) {
            if (!hopper.isEnabledPullItem()) {
                cir.setReturnValue(false);
                cir.cancel();
            }

            if (isFilteringEnabled(hopper, TransportType.PULL_FROM_CONTAINER)) {
                if (!processFilter(hopper, stack, TransportType.PULL_FROM_CONTAINER)) {
                    cir.setReturnValue(false);
                    cir.cancel();
                }
            }
        }
    }

    private static boolean isFilteringEnabled(IMixinHopperBlockEntity hopper, TransportType transportType) {
        return (transportType.isIncoming() && hopper.isIncomingFilterEnabled()) || (transportType.isOutgoing() && hopper.isOutgoingFilterEnabled());
    }

    /**
     * フィルタを通過できるかどうかをテストします。
     *
     * @param hopper ホッパー
     * @param target 対象のアイテム
     * @return フィルタを通過できるかどうか
     */
    private static boolean processFilter(IMixinHopperBlockEntity hopper, ItemStack target, TransportType transportType) {
        Set<Filter> filters = transportType.isIncoming() ? hopper.getIncomingFilters() : hopper.getOutgoingFilters();
        FilterType filterMode = transportType.isIncoming() ? hopper.getIncomingFilterMode() : hopper.getOutgoingFilterMode();

        if (isFilteringEnabled(hopper, transportType)) {
            boolean filteringResult = filters.stream().anyMatch(filter -> filter.matches(target, transportType));
            if (filteringResult) {
                return filterMode != FilterType.BLACKLIST;
            } else {
                return filterMode != FilterType.WHITELIST;
            }
        } else return !hopper.isIncomingFilterEnabled() && !hopper.isOutgoingFilterEnabled();
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
        if (hopper instanceof IMixinHopperBlockEntity hp) {
            List<ItemEntity> entities;
            if (!hp.isEnabledFindItem()) {
                entities = Collections.emptyList();
            } else {
                entities = world.getEntitiesOfClass(ItemEntity.class, hp.getItemFindAABB(d0, d1, d2), Entity::isAlive);
            }
            cir.setReturnValue(entities);
            cir.cancel();
        }
        // UnknownNet end
    }

    @Inject(method = "loadAdditional", at = @At("RETURN"))
    public void onLoad(CompoundTag tag, HolderLookup.Provider registryLookup, CallbackInfo ci) {
        if (tag.contains("DisableEvent")) this.disableEvent = tag.getBoolean("DisableEvent");
        // {Filter: {Mode: WHITELIST, IncomingFilters: [{id: "minecraft:stick", components: {}}, {tag: "minecraft:logs", components: {}}], OutgoingFilters: []}}
        if (tag.contains("Filter")) {
            CompoundTag filter = tag.getCompound("Filter");
            if ((filter.contains("IncomingFilterMode") || filter.contains("Mode"))) {
                this.incomingFilterMode = FilterType.valueOfOrDefault(filter.getString(filter.contains("Mode") ? "Mode" : "IncomingFilterMode").toUpperCase(), FilterType.DISABLED);
            }

            if (filter.contains("OutgoingFilterMode")) {
                this.outgoingFilterMode = FilterType.valueOfOrDefault(filter.getString("OutgoingFilterMode").toUpperCase(), FilterType.DISABLED);
            }

            if (filter.contains("IncomingFilters") || filter.contains("Filters")) { // Migrate from old version
                ListTag items = filter.getList(filter.contains("Filters") ? "Filters" : "IncomingFilters", CompoundTag.TAG_COMPOUND);
                this.incomingFilters.clear();
                items.forEach(filterDataTag -> {
                    if (filterDataTag instanceof CompoundTag filterData) {
                        this.incomingFilters.add(Filter.fromTag(filterData, registryLookup));
                    }
                });
            }

            if (filter.contains("OutgoingFilters")) {
                ListTag items = filter.getList("OutgoingFilters", CompoundTag.TAG_COMPOUND);
                this.outgoingFilters.clear();
                items.forEach(filterDataTag -> {
                    if (filterDataTag instanceof CompoundTag filterData) {
                        this.outgoingFilters.add(Filter.fromTag(filterData, registryLookup));
                    }
                });
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
                    ListTag A = Range.getList("A", CompoundTag.TAG_DOUBLE);
                    if (A.size() >= 3) {
                        this.findItem1.set(0, DoubleTag.valueOf(A.getDouble(0)));
                        this.findItem1.set(1, DoubleTag.valueOf(A.getDouble(1)));
                        this.findItem1.set(2, DoubleTag.valueOf(A.getDouble(2)));
                    }
                }

                if (Range.contains("B")) {
                    ListTag B = Range.getList("B", CompoundTag.TAG_DOUBLE);
                    if (B.size() >= 3) {
                        this.findItem2.set(0, DoubleTag.valueOf(B.getDouble(0)));
                        this.findItem2.set(1, DoubleTag.valueOf(B.getDouble(1)));
                        this.findItem2.set(2, DoubleTag.valueOf(B.getDouble(2)));
                    }
                }
            }
        }

        if (tag.contains("ItemPull")) {
            CompoundTag tagItemPull = tag.getCompound("ItemPull");
            if (tagItemPull.contains("Active")) {
                this.pullItem = tagItemPull.getBoolean("Active");
            }
        }

        if (tag.contains("ItemPush")) {
            CompoundTag tagItemPush = tag.getCompound("ItemPush");
            if (tagItemPush.contains("Active")) {
                this.pushItem = tagItemPush.getBoolean("Active");
            }
        }
    }

    @Inject(method = "saveAdditional", at = @At("RETURN"))
    public void onSaveAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup, CallbackInfo ci) {
        if (this.disableEvent) nbt.putBoolean("DisableEvent", true);

        CompoundTag filterRootTag = new CompoundTag();

        filterRootTag.putString("IncomingFilterMode", this.getIncomingFilterMode().name());
        ListTag incomingFilters = new ListTag();
        this.incomingFilters.forEach(filter -> incomingFilters.add(filter.toTag(registryLookup)));
        filterRootTag.put("IncomingFilters", incomingFilters);

        filterRootTag.putString("OutgoingFilterMode", this.getOutgoingFilterMode().name());
        ListTag outgoingFilters = new ListTag();
        this.outgoingFilters.forEach(filter -> outgoingFilters.add(filter.toTag(registryLookup)));
        filterRootTag.put("OutgoingFilters", outgoingFilters);

        CompoundTag outgoingFilterRootTag = new CompoundTag();
        outgoingFilterRootTag.putString("Mode", this.getOutgoingFilterMode().name());

        nbt.put("Filter", filterRootTag);

        CompoundTag ItemFind = new CompoundTag();
        ItemFind.putBoolean("Active", this.findItem);
        CompoundTag Range = new CompoundTag();
        Range.put("A", this.findItem1);
        Range.put("B", this.findItem2);
        ItemFind.put("Range", Range);
        nbt.put("ItemFind", ItemFind);

        CompoundTag ItemPull = new CompoundTag();
        ItemPull.putBoolean("Active", this.pullItem);
        nbt.put("ItemPull", ItemPull);

        CompoundTag ItemPush = new CompoundTag();
        ItemPush.putBoolean("Active", this.pushItem);
        nbt.put("ItemPush", ItemPush);
    }

    @Inject(method = "callPushMoveEvent", at = @At("HEAD"), cancellable = true)
    private static void onPushMoveEventCalling(Container iinventory, ItemStack itemstack, HopperBlockEntity hopper, CallbackInfoReturnable<ItemStack> cir) {
        if (hopper instanceof IMixinHopperBlockEntity mHopper) {
            if (mHopper.isEventDisabled()) {
                cir.setReturnValue(null);
                cir.cancel();
            }
        }
    }

    @Inject(method = "callPullMoveEvent", at = @At("HEAD"), cancellable = true)
    private static void onPullMoveEventCalling(Hopper hopper, Container container, ItemStack itemstack, CallbackInfoReturnable<ItemStack> cir) {
        if (hopper instanceof IMixinHopperBlockEntity mHopper) {
            if (mHopper.isEventDisabled()) {
                cir.setReturnValue(null);
                cir.cancel();
            }
        }
    }

    @Override
    public AABB getSuckAabb() {
        return this.getItemFindAABB(0, 0, 0);
    }

    // Unknown Network start
    @Override
    public Set<Filter> getIncomingFilters() {
        return this.incomingFilters;
    }

    @Override
    public Set<Filter> getOutgoingFilters() {
        return this.outgoingFilters;
    }

    @Override
    public void setIncomingFilters(Set<Filter> incomingFilters) {
        this.incomingFilters = incomingFilters;
    }

    @Override
    public void setOutgoingFilters(Set<Filter> outgoingFilters) {
        this.outgoingFilters = outgoingFilters;
    }

    @Override
    public void addIncomingFilter(Filter filter) {
        this.incomingFilters.add(filter);
    }

    @Override
    public void addOutgoingFilter(Filter filter) {
        this.outgoingFilters.add(filter);
    }

    @Override
    public FilterType getIncomingFilterMode() {
        return this.incomingFilterMode;
    }

    @Override
    public FilterType getOutgoingFilterMode() {
        return this.outgoingFilterMode;
    }

    @Override
    public void setIncomingFilterMode(FilterType incomingFilterMode) {
        this.incomingFilterMode = incomingFilterMode;
    }

    @Override
    public void setOutgoingFilterMode(FilterType outgoingFilterMode) {
        this.outgoingFilterMode = outgoingFilterMode;
    }

    @Override
    public boolean isIncomingFilterEnabled() {
        return (this.incomingFilterMode != null && this.incomingFilterMode != FilterType.DISABLED);
    }

    @Override
    public boolean isOutgoingFilterEnabled() {
        return (this.outgoingFilterMode != null && this.outgoingFilterMode != FilterType.DISABLED);
    }

    @Override
    public boolean isEnabledFindItem() {
        return this.findItem;
    }

    @Override
    public boolean isEnabledPullItem() {
        return this.pullItem;
    }

    @Override
    public boolean isEnabledPushItem() {
        return this.pushItem;
    }

    @Override
    public void setEnabledFindItem(boolean enabled) {
        this.findItem = enabled;
    }

    @Override
    public void setEnabledPullItem(boolean enabled) {
        this.pullItem = enabled;
    }

    @Override
    public void setEnabledPushItem(boolean enabled) {
        this.pushItem = enabled;
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

    @Override
    public boolean isEventDisabled() {
        return this.disableEvent;
    }
    // Unknown Network end
}
