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

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.DataResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

// com.mojang.serialization.JsonOps#getBooleanValue
@Mixin(com.mojang.serialization.JsonOps.class)
public class MixinJsonOps {
    /**
     * Original Code:
     * <p>return input instanceof JsonPrimitive && input.getAsJsonPrimitive().isBoolean() ? DataResult.success(input.getAsBoolean()) : DataResult.error(() -> "Not a boolean: " + input);</p>
     *
     * @author ryuuta0217
     * @reason Supports string and number format boolean value input, for TextComponent (ex: {"text": "Hello, World!", "bold": "true"})
     */
    @Overwrite
    public DataResult<Boolean> getBooleanValue(JsonElement input) {
        if (input instanceof JsonPrimitive primitive) {
            if (primitive.isBoolean()) return DataResult.success(primitive.getAsBoolean());
            if (primitive.isNumber()) return DataResult.success(primitive.getAsNumber().intValue() == 1);
        }
        try {
            String str = input.getAsString();
            if (str != null && (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false"))) {
                return DataResult.success(str.equalsIgnoreCase("true"));
            }
        } catch(Throwable ignored) {}

        return DataResult.error(() -> "Not a boolean: " + input);
    }
}
