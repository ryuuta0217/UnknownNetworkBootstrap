package net.unknown.launchwrapper.mixins;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.unknown.launchwrapper.mixininterfaces.IMixinBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.UUID;

@Mixin(BlockEntity.class)
public class MixinBlockEntity implements IMixinBlockEntity {
    private UUID placer = null;

    @Inject(method = "load", at = @At("RETURN"))
    public void onLoad(CompoundTag nbt, CallbackInfo ci) {
        if (nbt.contains("Placer", Tag.TAG_INT_ARRAY)) {
            this.placer = nbt.getUUID("Placer");
        }
    }

    @Inject(method = "saveAdditional", at = @At("RETURN"))
    public void onSaveAdditional(CompoundTag nbt, CallbackInfo ci) {
        if (this.placer != null) nbt.putUUID("Placer", this.placer);
    }

    @Nullable
    @Override
    public UUID getPlacer() {
        return this.placer;
    }

    @Override
    public void setPlacer(@Nullable UUID placer) {
        this.placer = placer;
    }
}
