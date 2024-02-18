package net.unknown.launchwrapper.mixins;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.unknown.launchwrapper.mixininterfaces.IMixinItemStackWhoCrafted;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(ItemStack.class)
public abstract class MixinItemStack implements IMixinItemStackWhoCrafted {
    @Shadow public abstract CompoundTag getOrCreateTag();

    @Unique
    private static final String TAG_WHO_CRAFTED = "WhoCrafted";

    private UUID whoCrafted;

    @Override
    public UUID getWhoCrafted() {
        return this.whoCrafted;
    }

    @Override
    public void setWhoCrafted(UUID whoCrafted) {
        this.whoCrafted = whoCrafted;
    }

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;processEnchantOrder(Lnet/minecraft/nbt/CompoundTag;)V"))
    private void onLoad(CompoundTag nbt, CallbackInfo ci) {
        if (this.getOrCreateTag().hasUUID(TAG_WHO_CRAFTED)) {
            this.whoCrafted = this.getOrCreateTag().getUUID(TAG_WHO_CRAFTED);
        }
    }


    @Inject(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;"))
    private void onSave(CompoundTag nbt, CallbackInfoReturnable<CompoundTag> cir) {
        if (this.whoCrafted != null) this.getOrCreateTag().putUUID(TAG_WHO_CRAFTED, this.whoCrafted);
    }
}
