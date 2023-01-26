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

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.WetSpongeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.unknown.launchwrapper.SpongeState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nonnull;
import java.util.Optional;

@Mixin(WetSpongeBlock.class)
public abstract class MixinWetSpongeBlock extends Block implements ChangeOverTimeBlock<SpongeState> {
    public MixinWetSpongeBlock(Properties settings) {
        super(settings);
    }

    @Override
    public void randomTick(@Nonnull BlockState state, @Nonnull ServerLevel world, @Nonnull BlockPos pos, @Nonnull RandomSource random) {
        this.onRandomTick(state, world, pos, random);
    }

    @Override
    public void onRandomTick(@Nonnull BlockState state, ServerLevel world, @Nonnull BlockPos pos, @Nonnull RandomSource random) {
        if (!world.isRaining() && !world.isThundering() && world.getBlockStates(AABB.ofSize(new Vec3(pos.getX(), pos.getY(), pos.getZ()), 1, 1, 1)).noneMatch(blockState -> blockState.getBlock() == Blocks.WATER)) {
            if (world.getBlockState(pos.below()).is(Blocks.CAMPFIRE)) {
                this.applyChangeOverTime(state, world, pos, random);
            } else if (world.getDayTime() < 12000 && random.nextInt(1, 3) == 2) {
                this.applyChangeOverTime(state, world, pos, random);
            }
        }
    }



    @Override
    public boolean isRandomlyTicking(@Nonnull BlockState state) {
        return true;
    }

    @Nonnull
    @Override
    public Optional<BlockState> getNext(@Nonnull BlockState state) {
        return Optional.of(Blocks.SPONGE.defaultBlockState());
    }

    @Override
    public float getChanceModifier() {
        return 1;
    }

    @Nonnull
    @Override
    public SpongeState getAge() {
        return SpongeState.WET;
    }
}
