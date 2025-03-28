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

import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.unknown.launchwrapper.event.BlockDispenseBeforeEvent;
import org.bukkit.Bukkit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DefaultDispenseItemBehavior.class)
public abstract class MixinDefaultDispenseItemBehavior implements DispenseItemBehavior {
    @Shadow
    protected abstract ItemStack execute(BlockSource pointer, ItemStack stack);

    @Shadow
    protected abstract void playSound(BlockSource pointer);

    @Shadow
    protected abstract void playAnimation(BlockSource pointer, Direction side);

    @Shadow private Direction direction;

    /**
     * Add BlockDispenseBeforeEvent
     *
     * @author ryuuta0217
     * @reason Add BlockDispenseBeforeEvent
     */
    @Overwrite
    @Override
    public final ItemStack dispense(BlockSource pointer, ItemStack stack) {
        this.direction = pointer.state().getValue(DispenserBlock.FACING); // Paper - cache facing direction
        // Unknown Network Start - Add BlockDispenseBeforeEvent
        BlockDispenseBeforeEvent event = new BlockDispenseBeforeEvent(pointer, stack);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            event.setItem(this.execute(event.getBlockSource(), event.getItem()));
            this.playSound(event.getBlockSource());
            this.playAnimation(event.getBlockSource(), this.direction); // Paper - cache facing direction
        }
        return event.getItem();
        // Unknown network End
    }
}
