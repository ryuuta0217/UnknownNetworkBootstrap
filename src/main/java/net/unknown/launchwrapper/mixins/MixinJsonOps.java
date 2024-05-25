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
