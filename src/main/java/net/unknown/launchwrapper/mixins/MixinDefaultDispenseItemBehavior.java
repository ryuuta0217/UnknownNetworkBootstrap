package net.unknown.launchwrapper.mixins;

import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.unknown.launchwrapper.event.BlockDispenseBeforeEvent;
import org.bukkit.Bukkit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DefaultDispenseItemBehavior.class)
public abstract class MixinDefaultDispenseItemBehavior implements DispenseItemBehavior {
    @Shadow
    private Direction enumdirection;

    @Shadow
    protected abstract ItemStack execute(BlockSource pointer, ItemStack stack);

    @Shadow
    protected abstract void playSound(BlockSource pointer);

    @Shadow
    protected abstract void playAnimation(BlockSource pointer, Direction side);

    /**
     * Add BlockDispenseBeforeEvent
     *
     * @author ryuuta0217
     */
    @Overwrite
    @Override
    public final ItemStack dispense(BlockSource pointer, ItemStack stack) {
        this.enumdirection = pointer.getBlockState().getValue(DispenserBlock.FACING); // Paper - cache facing direction
        // Unknown Network Start - Add BlockDispenseBeforeEvent
        BlockDispenseBeforeEvent event = new BlockDispenseBeforeEvent(pointer, stack);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            event.setItem(this.execute(event.getBlockSource(), event.getItem()));
            this.playSound(event.getBlockSource());
            this.playAnimation(event.getBlockSource(), enumdirection); // Paper - cache facing direction
        }
        return event.getItem();
        // Unknown network End
    }
}
