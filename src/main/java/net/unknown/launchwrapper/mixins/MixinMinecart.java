package net.unknown.launchwrapper.mixins;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Minecart.class)
public abstract class MixinMinecart extends AbstractMinecart {
    public double isInWaterMaxSpeed = this.maxSpeed / 2.0;

    protected MixinMinecart(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("MaxSpeed")) {
            this.maxSpeed = nbt.getDouble("MaxSpeed");
        }

        if (nbt.contains("IsInWaterMaxSpeed")) {
            this.isInWaterMaxSpeed = nbt.getDouble("IsInWaterMaxSpeed");
        } else {
            this.isInWaterMaxSpeed = this.maxSpeed / 2.0D;
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        if (this.maxSpeed != 0.4D) nbt.putDouble("MaxSpeed", this.maxSpeed);
        if (this.isInWaterMaxSpeed != (this.maxSpeed / 2.0D)) nbt.putDouble("IsInWaterMaxSpeed", this.isInWaterMaxSpeed);
    }

    @Override
    protected double getMaxSpeed() {
        return (this.isInWater() ? this.isInWaterMaxSpeed : this.maxSpeed);
    }
}
