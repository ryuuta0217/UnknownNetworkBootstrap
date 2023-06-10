package net.unknown.launchwrapper.mixins;

import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.unknown.launchwrapper.mixininterfaces.IMixinBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.UUID;

@Mixin(SignBlockEntity.class)
public class MixinSignBlockEntity {
    @Shadow @Nullable public UUID playerWhoMayEdit;

    @Overwrite
    @Nullable
    public UUID getPlayerWhoMayEdit() {
        if (this instanceof IMixinBlockEntity mixinBlockEntity) {
            if (mixinBlockEntity.getPlacer() != null) return mixinBlockEntity.getPlacer();
        }
        return this.playerWhoMayEdit;
    }
}
