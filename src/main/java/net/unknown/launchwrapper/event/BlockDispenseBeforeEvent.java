/*
 * Copyright (c) 2022 Unknown Network Developers and contributors.
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

package net.unknown.launchwrapper.event;

import net.minecraft.core.BlockSource;
import net.minecraft.world.item.ItemStack;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlock;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BlockDispenseBeforeEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final BlockSource src;
    private boolean isCancelled = false;
    private ItemStack item;

    public BlockDispenseBeforeEvent(BlockSource src, ItemStack item) {
        this.src = src;
        this.item = item;
    }

    public BlockSource getBlockSource() {
        return this.src;
    }

    public Block getBukkitBlock() {
        return CraftBlock.at(this.src.getLevel(), this.src.getPos());
    }

    public ItemStack getItem() {
        return this.item;
    }

    public void setItem(ItemStack newItem) {
        this.item = newItem;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
