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
