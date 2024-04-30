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

import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.unknown.launchwrapper.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

import java.util.ArrayList;
import java.util.List;

@Mixin(FeatureFlags.class)
public class MixinFeatureFlags {
    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/flag/FeatureFlagSet;of(Lnet/minecraft/world/flag/FeatureFlag;)Lnet/minecraft/world/flag/FeatureFlagSet;"))
    private static FeatureFlagSet onClInit(FeatureFlag feature) {
        List<FeatureFlag> additionalFlags = new ArrayList<>() {{
            if (Main.FORCE_ALLOW_BUNDLE_FEATURES) add(FeatureFlags.BUNDLE);
            if (Main.FORCE_ALLOW_TRADE_REBALANCE_FEATURES) add(FeatureFlags.TRADE_REBALANCE);
            if (Main.FORCE_ALLOW_UPDATE_1_21) add(FeatureFlags.UPDATE_1_21);
        }};

        return !additionalFlags.isEmpty() ? FeatureFlagSet.of(feature, additionalFlags.toArray(FeatureFlag[]::new)) : FeatureFlagSet.of(feature);
    }
}
