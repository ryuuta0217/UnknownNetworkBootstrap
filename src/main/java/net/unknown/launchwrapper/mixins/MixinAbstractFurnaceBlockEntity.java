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
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class MixinAbstractFurnaceBlockEntity extends BaseContainerBlockEntity {
    @Shadow @Final public RecipeType<? extends AbstractCookingRecipe> recipeType;

    protected MixinAbstractFurnaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private AbstractCookingRecipe currentRecipe;
    private ItemStack failedMatch = ItemStack.EMPTY;

    public AbstractCookingRecipe getRecipe() {
        ItemStack input = this.getItem(0);
        if (input.isEmpty() || ItemStack.isSame(this.failedMatch, input) && ItemStack.tagMatches(this.failedMatch, input)) return null;
        if (this.currentRecipe != null && this.currentRecipe.matches(this, this.level)) {
            return this.currentRecipe;
        } else {
            AbstractCookingRecipe recipe = this.level.getRecipeManager().getRecipeFor(this.recipeType, this, this.level).orElse(null);
            if (recipe == null) {
                this.failedMatch = input.copy();
            } else {
                this.failedMatch = ItemStack.EMPTY;
            }

            return this.currentRecipe = recipe;
        }
    }

    /**
     * @author ryuuta0217
     * @reason Increase server performance.
     */
    @Overwrite
    // Paper start
    public static int getTotalCookTime(@Nullable Level world, RecipeType<? extends AbstractCookingRecipe> recipeType, AbstractFurnaceBlockEntity furnace, double cookSpeedMultiplier) {
        /* Scale the recipe's cooking time to the current cookSpeedMultiplier */
        //int cookTime = world != null ? furnace.quickCheck.getRecipeFor(furnace, world).map(AbstractCookingRecipe::getCookingTime).orElse(200) : (net.minecraft.server.MinecraftServer.getServer().getRecipeManager().getRecipeFor(recipeType, furnace, world /* passing a null level here is safe. world is only used for map extending recipes which won't happen here */).map(AbstractCookingRecipe::getCookingTime).orElse(200));
        //return (int) Math.ceil (cookTime / cookSpeedMultiplier);
        AbstractCookingRecipe recipe = ((MixinAbstractFurnaceBlockEntity) (Object) furnace).getRecipe();
        return recipe == null ? 200 : (int) Math.ceil(recipe.getCookingTime() / cookSpeedMultiplier);
    }
    // Paper end

    @Redirect(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeManager$CachedCheck;getRecipeFor(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;"), require = 1)
    private static Optional<AbstractCookingRecipe> getRecipe(RecipeManager.CachedCheck<?, ?> instance, Container container, Level level) {
        return Optional.ofNullable(((MixinAbstractFurnaceBlockEntity) container).getRecipe());
    }
}
