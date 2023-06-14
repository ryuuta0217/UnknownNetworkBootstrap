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

import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.unknown.launchwrapper.linkchest.ChestTransportMode;
import net.unknown.launchwrapper.mixininterfaces.IMixinChestBlockEntity;
import net.unknown.launchwrapper.linkchest.LinkedChest;
import net.unknown.launchwrapper.util.WrappedBlockPos;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(ChestBlockEntity.class)
public abstract class MixinChestBlockEntity extends RandomizableContainerBlockEntity implements LidBlockEntity, IMixinChestBlockEntity {
    private static final Map<UUID, LinkedChest> LINKED_CHESTS = Maps.newHashMap();
    @Shadow
    private NonNullList<ItemStack> items;
    private boolean isVoidChest = false;
    private final NonNullList<ItemStack> voidList = NonNullList.withSize(27, ItemStack.EMPTY);
    private ChestTransportMode previousTransportMode = null;
    private ChestTransportMode transportMode = ChestTransportMode.DISABLED;
    private UUID previousUniqueId = null;
    private UUID linkUniqueId = null;
    protected MixinChestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public boolean isVoidChest() {
        return this.isVoidChest;
    }

    public void setVoidChest(boolean isVoidChest) {
        this.isVoidChest = isVoidChest;
    }

    @Inject(method = "load", at = @At("RETURN"))
    public void onLoad(CompoundTag nbt, CallbackInfo ci) {
        if (nbt.contains("Link")) {
            CompoundTag link = nbt.getCompound("Link");
            if (link.contains("Mode")) {
                this.previousTransportMode = this.transportMode;
                this.transportMode = ChestTransportMode.valueOfOrDefault(link.getString("Mode"), ChestTransportMode.DISABLED);
            }

            if (link.contains("UUID")) {
                this.previousUniqueId = this.linkUniqueId;
                this.linkUniqueId = link.getUUID("UUID");
            }

            if (this.linkUniqueId != null) {
                LinkedChest linkedChest;
                if (LINKED_CHESTS.containsKey(this.linkUniqueId)) {
                    linkedChest = LINKED_CHESTS.get(this.linkUniqueId);
                } else {
                    linkedChest = new LinkedChest(new HashSet<>(), null);
                    LINKED_CHESTS.put(this.linkUniqueId, linkedChest);
                }

                if (this.transportMode != ChestTransportMode.DISABLED) {
                    switch (this.transportMode) {
                        case CLIENT -> linkedChest.addClientPos(new WrappedBlockPos(this.getLevel(), this.getBlockPos()));
                        case SOURCE -> {
                            this.getViewers().forEach(human -> human.closeInventory(InventoryCloseEvent.Reason.CANT_USE));
                            if (linkedChest.getSourcePos() != null) { // If already exists source chest, set it to client mode
                                BlockEntity entity = linkedChest.getSourcePos().getBlockEntity(true);
                                if (entity != null) {
                                    if (entity instanceof IMixinChestBlockEntity chestBlock && !entity.getBlockPos().equals(this.getBlockPos())) {
                                        chestBlock.setChestTransportMode(ChestTransportMode.CLIENT);
                                    }
                                }
                            }
                            linkedChest.setSourcePos(new WrappedBlockPos(this.getLevel(), this.getBlockPos()));
                        }
                    }
                }
            }

            if (this.previousUniqueId != null) {
                if (LINKED_CHESTS.containsKey(this.previousUniqueId)) {
                    LinkedChest lc = LINKED_CHESTS.get(this.previousUniqueId);

                    lc.getClientPos().removeIf(clientPos -> { // Remove client if not referenced anymore
                        BlockEntity entity = clientPos.getBlockEntity(true);
                        if (entity != null) {
                            if (entity instanceof IMixinChestBlockEntity chestBlock) {
                                return !chestBlock.getLinkUniqueId().equals(this.previousUniqueId);
                            }
                        }
                        return true;
                    });

                    WrappedBlockPos sourcePos = lc.getSourcePos();
                    if (sourcePos != null) { // Remove source if not referenced anymore
                        BlockEntity entity = sourcePos.getBlockEntity(true);
                        if (entity != null) {
                            if (entity instanceof IMixinChestBlockEntity chestBlock) {
                                if (!chestBlock.getLinkUniqueId().equals(this.previousUniqueId)) {
                                    lc.setSourcePos(null);
                                }
                            }
                        }
                    }

                    if (lc.getClientPos().size() == 0 && lc.getSourcePos() == null) {
                        LINKED_CHESTS.remove(this.previousUniqueId);
                    }
                }
            }
        }

        if (nbt.contains("VoidChest")) {
            this.isVoidChest = nbt.getBoolean("VoidChest");
        }
    }

    @Inject(method = "saveAdditional", at = @At("RETURN"))
    public void onSaveAdditional(CompoundTag nbt, CallbackInfo ci) {
        if (this.transportMode != ChestTransportMode.DISABLED && this.linkUniqueId != null) {
            CompoundTag link = new CompoundTag();

            link.putString("Mode", this.transportMode.name());
            link.putUUID("UUID", this.linkUniqueId);

            nbt.put("Link", link);
        }

        nbt.putBoolean("VoidChest", this.isVoidChest);
    }

    @Override
    public ChestTransportMode getChestTransportMode() {
        return this.transportMode;
    }

    @Override
    public void setChestTransportMode(ChestTransportMode transportMode) {
        this.transportMode = transportMode;
    }

    @Override
    public UUID getLinkUniqueId() {
        return this.linkUniqueId;
    }

    @Override
    public void setLevel(@NotNull Level level) {
        if (this.transportMode != ChestTransportMode.DISABLED && this.linkUniqueId != null) {
            if (LINKED_CHESTS.containsKey(this.linkUniqueId)) {
                LinkedChest lc = LINKED_CHESTS.get(this.linkUniqueId);
                if (this.transportMode == ChestTransportMode.CLIENT && lc.getClientPos().size() != 0) {
                    lc.getClientPos().removeIf(clientPos -> clientPos.level().equals(this.getLevel()) && clientPos.blockPos().equals(this.getBlockPos()));
                    lc.addClientPos(new WrappedBlockPos(level, this.getBlockPos()));
                }
                if (this.transportMode == ChestTransportMode.SOURCE && lc.getSourcePos() != null) {
                    lc.getSourcePos().level(level);
                }
            }
        }
        super.setLevel(level);
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
        if (this.transportMode == ChestTransportMode.CLIENT && this.linkUniqueId != null) {
            if (LINKED_CHESTS.containsKey(this.linkUniqueId)) {
                LinkedChest linkChest = LINKED_CHESTS.get(this.linkUniqueId);
                if (linkChest.getSourcePos() != null) {
                    BlockEntity be = linkChest.getSourcePos().getBlockEntity(true);
                    if (be instanceof MixinChestBlockEntity sourceChest) {
                        if (sourceChest.getLinkUniqueId() != null) {
                            if (sourceChest.getLinkUniqueId().equals(this.getLinkUniqueId())) {
                                if (sourceChest.getChestTransportMode() == ChestTransportMode.SOURCE) {
                                    return sourceChest.getItems();
                                }
                            }
                        }
                    }
                }
            }
        }
        return this.items;
    }

    @Inject(method = "setItems", at = @At("HEAD"), cancellable = true)
    public void onSetItems(NonNullList<ItemStack> list, CallbackInfo ci) {
        if (this.isVoidChest) {
            ci.cancel();
        }

        if (this.transportMode == ChestTransportMode.CLIENT && this.linkUniqueId != null) {
            if (LINKED_CHESTS.containsKey(this.linkUniqueId)) {
                LinkedChest linkChest = LINKED_CHESTS.get(this.linkUniqueId);
                if (linkChest.getSourcePos() != null) {
                    BlockEntity be = linkChest.getSourcePos().getBlockEntity(true);
                    if (be instanceof MixinChestBlockEntity sourceChest) {
                        if (sourceChest.getLinkUniqueId() != null) {
                            if (sourceChest.getLinkUniqueId().equals(this.getLinkUniqueId())) {
                                if (sourceChest.getChestTransportMode() == ChestTransportMode.SOURCE) {
                                    sourceChest.setItems(list);
                                    ci.cancel();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Shadow public abstract List<HumanEntity> getViewers();

    @Override
    public Map<UUID, LinkedChest> getLinks() {
        return LINKED_CHESTS;
    }
}
