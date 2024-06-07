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

package net.unknown.launchwrapper.hopper;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TagFilter implements Filter {
    private final TagKey<Item> itemTag;

    @Nullable
    private final DataComponentPatch dataPatch;

    @Nullable
    private final TransportType transportType;

    public TagFilter(TagKey<Item> itemTag, @Nullable DataComponentPatch dataPatch) {
        this(itemTag, dataPatch, null);
    }

    public TagFilter(TagKey<Item> itemTag, @Nullable DataComponentPatch dataPatch, @Nullable TransportType transportType) {
        this.itemTag = itemTag;
        this.dataPatch = dataPatch;
        this.transportType = transportType;
    }

    public TagKey<Item> getTag() {
        return this.itemTag;
    }

    @Nullable
    @Override
    public DataComponentPatch getDataPatch() {
        return this.dataPatch;
    }

    @Override
    public boolean matches(@Nullable ItemStack stack, @Nonnull TransportType transportType) {
        if ((this.getTransportType() == null || transportType.equals(this.getTransportType())) && stack != null && stack.is(this.itemTag)) {
            return this.dataPatch == null || this.dataPatch.equals(stack.getComponentsPatch());
        }
        return false;
    }

    @Override
    public TransportType getTransportType() {
        return this.transportType;
    }

    @Override
    public CompoundTag toTag(HolderLookup.Provider registryLookup) {
        CompoundTag tag = new CompoundTag();
        tag.putString("tag", this.getTag().location().toString());
        if (this.getDataPatch() != null) {
            tag.put("components", DataComponentPatch.CODEC.encodeStart(registryLookup.createSerializationContext(NbtOps.INSTANCE), this.getDataPatch()).getOrThrow());
        }
        return tag;
    }
}
