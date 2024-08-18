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

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.unknown.launchwrapper.mixininterfaces.IMixinBarrelBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BarrelBlockEntity.class)
public abstract class MixinBarrelBlockEntity extends RandomizableContainerBlockEntity implements IMixinBarrelBlockEntity {
    @Shadow private NonNullList<ItemStack> items;
    private boolean large = false;

    protected MixinBarrelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;withSize(ILjava/lang/Object;)Lnet/minecraft/core/NonNullList;"))
    private <E> NonNullList<E> onInit(int size, E defaultElement) {
        return NonNullList.withSize(this.large ? 54 : 27, defaultElement);
    }

    @Inject(method = "saveAdditional", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/RandomizableContainerBlockEntity;saveAdditional(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V", shift = At.Shift.AFTER))
    private void onSaving(CompoundTag nbt, HolderLookup.Provider registryLookup, CallbackInfo ci) {
        if (this.large) nbt.putBoolean("Large", this.large);
    }

    @Inject(method = "loadAdditional", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/RandomizableContainerBlockEntity;loadAdditional(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V"))
    private void onLoading(CompoundTag nbt, HolderLookup.Provider registryLookup, CallbackInfo ci) {
        if (nbt.contains("Large")) this.setLarge(nbt.getBoolean("Large"));
    }

    /**
     * @author ryuuta0217
     * @reason Support for large barrels (open menu as 6 rows)
     */
    @Nullable
    @Overwrite
    protected AbstractContainerMenu createMenu(int syncId, @Nullable Inventory playerInventory) {
        if (!this.large) return ChestMenu.threeRows(syncId, playerInventory, this);
        else return ChestMenu.sixRows(syncId, playerInventory, this);
    }

    /**
     * @author ryuuta0217
     * @reason Support for large barrels
     */
    @Overwrite
    @Override
    public int getContainerSize() {
        return this.large ? 54 : 27;
    }

    @Override
    public boolean isLarge() {
        return this.large;
    }

    @Override
    public void setLarge(boolean large) {
        if (this.large != large) {
            this.large = large;
            NonNullList<ItemStack> newItems = NonNullList.withSize(this.large ? 54 : 27, ItemStack.EMPTY);
            for (int i = 0; i < this.items.size(); i++) {
                if (i < newItems.size()) newItems.set(i, this.items.get(i));
                else dropItemImmediately(this.items.get(i));
            }
            this.items = newItems;
        }
    }

    private void dropItemImmediately(ItemStack stack) {
        if (this.hasLevel()) {
            Vec3 dropPos = Vec3.atCenterOf(this.getBlockPos());
            this.getLevel().addFreshEntity(new ItemEntity(this.getLevel(), dropPos.x(), dropPos.y() + 1, dropPos.z(), stack));
        }
    }
}
