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

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.unknown.launchwrapper.BlockEventCapture;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_19_R2.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_19_R2.event.CraftEventFactory;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(CraftEventFactory.class)
public class MixinCraftEventFactory {
    @Inject(method = "callBlockPlaceEvent", at = @At("HEAD"))
    private static void onCalledBlockPlaceEvent(ServerLevel world, net.minecraft.world.entity.player.Player who, InteractionHand hand, BlockState replacedBlockState, int clickedX, int clickedY, int clickedZ, CallbackInfoReturnable<BlockBreakEvent> cir) {
        CraftBlockState state = ((CraftBlockState) replacedBlockState);
        if (state.getHandle().hasBlockEntity()) {
            BlockEventCapture.capture(state.getPosition(), who.getUUID());
        }
    }

    @Inject(method = "callBlockMultiPlaceEvent", at = @At("HEAD"))
    private static void onCalledBlockMultiPlaceEvent(ServerLevel world, Player who, InteractionHand hand, List<BlockState> blockStates, int clickedX, int clickedY, int clickedZ, CallbackInfoReturnable<BlockMultiPlaceEvent> cir) {
        blockStates.stream()
                .map(state -> (CraftBlockState) state)
                .forEach(blockState -> {
                    if (blockState.getHandle().hasBlockEntity()) {
                        BlockEventCapture.capture(blockState.getPosition(), who.getUUID());
                    }
                });
    }
}
