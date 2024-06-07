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
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;

public interface Filter {
    static Filter fromTag(CompoundTag filterData, HolderLookup.Provider registryLookup) {
        if (filterData.contains("id")) {
            ResourceLocation id = ResourceLocation.tryParse(filterData.getString("id"));
            Optional<Item> item = BuiltInRegistries.ITEM.getOptional(id);
            if (item.isPresent()) {
                DataComponentPatch componentPatch = filterData.contains("components") ? DataComponentPatch.CODEC.parse(registryLookup.createSerializationContext(NbtOps.INSTANCE), filterData.get("components")).getOrThrow() : null;
                return new ItemFilter(item.get(), componentPatch);
            }
        }

        if (filterData.contains("tag")) {
            TagKey<Item> itemTag = TagKey.create(Registries.ITEM, new ResourceLocation(filterData.getString("tag")));
            DataComponentPatch componentPatch = filterData.contains("nbt") ? DataComponentPatch.CODEC.parse(registryLookup.createSerializationContext(NbtOps.INSTANCE), filterData.get("components")).getOrThrow() : null;
            return new TagFilter(itemTag, componentPatch);
        }

        return null;
    }

    @Nullable
    DataComponentPatch getDataPatch();

    boolean matches(@Nullable ItemStack stack, TransportType transportType);

    TransportType getTransportType();

    CompoundTag toTag(HolderLookup.Provider registryLookup);
}
