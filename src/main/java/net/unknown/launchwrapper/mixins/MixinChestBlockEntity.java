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
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.unknown.launchwrapper.linkchest.LinkChestMode;
import net.unknown.launchwrapper.mixininterfaces.IMixinChestBlockEntity;
import net.unknown.launchwrapper.util.WrappedBlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(ChestBlockEntity.class)
public abstract class MixinChestBlockEntity extends RandomizableContainerBlockEntity implements LidBlockEntity, IMixinChestBlockEntity {
    @Shadow
    private NonNullList<ItemStack> items;
    private boolean isVoidChest = false;
    private final NonNullList<ItemStack> voidList = NonNullList.withSize(27, ItemStack.EMPTY);

    /* Link Chest - Unknown Network*/
    private LinkChestMode linkChestMode = LinkChestMode.DISABLED;
    private WrappedBlockPos linkChestSource;
    /* Link Chest - end */

    protected MixinChestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public boolean isVoidChest() {
        return this.isVoidChest;
    }

    public void setVoidChest(boolean isVoidChest) {
        if (this.linkChestMode == LinkChestMode.CLIENT) throw new IllegalStateException("Cannot set as void chest when in LinkedChest: CLIENT mode!");
        this.isVoidChest = isVoidChest;
    }

    @Inject(method = "load", at = @At("RETURN"))
    public void onLoad(CompoundTag nbt, CallbackInfo ci) {
        if (nbt.contains("Link")) {
            CompoundTag link = nbt.getCompound("Link");
            if (link.contains("Mode", CompoundTag.TAG_STRING)) {
                this.linkChestMode = LinkChestMode.valueOfOrDefault(link.getString("Mode"), LinkChestMode.DISABLED);
            }

            if (this.linkChestMode == LinkChestMode.CLIENT && link.contains("SourcePos", CompoundTag.TAG_COMPOUND)) {
                CompoundTag sourcePos = link.getCompound("SourcePos");
                if (sourcePos.contains("Level") && sourcePos.contains("Pos", CompoundTag.TAG_INT_ARRAY)) {
                    ResourceKey<Level> levelKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(sourcePos.getString("Level")));
                    int[] pos = sourcePos.getIntArray("Pos");
                    if (pos.length == 3) {
                        this.linkChestSource = new WrappedBlockPos(levelKey, new BlockPos(pos[0], pos[1], pos[2]));
                    }
                }
            }
        }

        if (nbt.contains("VoidChest", CompoundTag.TAG_BYTE)) {
            this.isVoidChest = nbt.getBoolean("VoidChest");
        }
    }

    @Inject(method = "saveAdditional", at = @At("RETURN"))
    public void onSaveAdditional(CompoundTag nbt, CallbackInfo ci) {
        if (this.linkChestMode != LinkChestMode.DISABLED) {
            CompoundTag link = new CompoundTag();

            link.putString("Mode", this.linkChestMode.name());
            if (this.linkChestMode == LinkChestMode.CLIENT && this.linkChestSource != null) {
                CompoundTag sourcePos = new CompoundTag();
                sourcePos.putString("Level", this.linkChestSource.levelKey().location().toString());
                sourcePos.putIntArray("Pos", new int[]{this.linkChestSource.blockPos().getX(), this.linkChestSource.blockPos().getY(), this.linkChestSource.blockPos().getZ()});
                link.put("SourcePos", sourcePos);
            }

            nbt.put("Link", link);
        }

        nbt.putBoolean("VoidChest", this.isVoidChest);
    }

    @Override
    public LinkChestMode getChestTransportMode() {
        return this.linkChestMode;
    }

    @Override
    public void setChestTransportMode(LinkChestMode transportMode) {
        this.linkChestMode = transportMode;
    }

    @Override
    public WrappedBlockPos getLinkSource() {
        return this.linkChestSource;
    }

    /**
     * @author ryuuta0217
     * @reason Use #getItems() instead of #getContents()
     */
    @Overwrite
    public List<ItemStack> getContents() {
        return this.getItems();
    }


    /**
     * Overwrite a getItems method.
     *
     * @author ryuuta0217
     */
    @Overwrite
    public NonNullList<ItemStack> getItems() {
        if (this.isVoidChest) {
            this.voidList.clear();
            return this.voidList;
        }

        if (this.linkChestMode == LinkChestMode.CLIENT) {
            if (this.linkChestSource != null) {
                BlockEntity blockEntity = this.linkChestSource.getBlockEntity(true, 3);
                if (blockEntity instanceof MixinChestBlockEntity chestBlockEntity) {
                    if (chestBlockEntity.getChestTransportMode() == LinkChestMode.SOURCE) {
                        return chestBlockEntity.getItems();
                    } else {
                        this.selfDestroy();
                    }
                } else if (blockEntity != null) {
                    this.selfDestroy();
                }
            } else {
                this.selfDestroy();
            }
        }
        return this.items;
    }

    @Inject(method = "setItems", at = @At("HEAD"), cancellable = true)
    public void onSetItems(NonNullList<ItemStack> list, CallbackInfo ci) {
        if (this.isVoidChest) {
            ci.cancel();
        }

        if (this.linkChestMode == LinkChestMode.CLIENT) {
            if (this.linkChestSource != null) {
                BlockEntity blockEntity = this.linkChestSource.getBlockEntity(true, 3);
                if (blockEntity instanceof MixinChestBlockEntity chestBlockEntity) {
                    if (chestBlockEntity.getChestTransportMode() == LinkChestMode.SOURCE) {
                        chestBlockEntity.setItems(list);
                        ci.cancel();
                    }
                }
            }
        }
    }

    private void selfDestroy() {
        if (this.hasLevel() && this.getLevel() != null) {
            this.getLevel().destroyBlock(this.getBlockPos(), true);
        }
    }
}
