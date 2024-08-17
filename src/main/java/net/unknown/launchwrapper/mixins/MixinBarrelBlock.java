package net.unknown.launchwrapper.mixins;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.unknown.launchwrapper.mixininterfaces.IMixinBarrelBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(BarrelBlock.class)
public abstract class MixinBarrelBlock extends BaseEntityBlock {
    protected MixinBarrelBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof IMixinBarrelBlockEntity barrel) {
            if (barrel.isLarge()) {
                List<ItemStack> originalDrops = super.getDrops(state, builder);
                List<ItemStack> modifiedDrops = originalDrops.stream().map(ItemStack::copy).toList();
                modifiedDrops.forEach(stack -> {
                    if (stack.getItem().equals(state.getBlock().asItem())) {
                        stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(blockEntity.saveWithId(blockEntity.getLevel().registryAccess())));
                    }
                });
                return modifiedDrops;
            }
        }
        return super.getDrops(state, builder);
    }
}
