package net.unknown.launchwrapper.mixins;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BigDripleafBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.unknown.launchwrapper.hopper.IMixinHopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collections;
import java.util.List;

@Mixin(HopperBlock.class)
public abstract class MixinHopperBlock extends BlockBehaviour {
    public MixinHopperBlock(Properties settings) {
        super(settings);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof IMixinHopperBlockEntity hopper) {
            if(hopper.getFilterMode() != null) {
                if(hopper.getFilters().size() > 0) {
                    ItemStack is = new ItemStack(state.getBlock().asItem());
                    CompoundTag tag = is.getOrCreateTag();
                    tag.put("BlockEntityTag", ((BlockEntity) hopper).saveWithoutMetadata());
                    CompoundTag display = new CompoundTag();
                    ListTag lore = new ListTag();
                    lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent("フィルターモード: " + hopper.getFilterMode().getLocalizedName()).withStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.AQUA)))));
                    lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent("登録フィルター数: " + hopper.getFilters().size()).withStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.AQUA)))));
                    display.put("Lore", lore);
                    tag.put("display", display);
                    return Collections.singletonList(is);
                }
            }
        }

        return super.getDrops(state, builder);
    }
}
