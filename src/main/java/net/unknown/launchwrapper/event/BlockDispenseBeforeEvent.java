package net.unknown.launchwrapper.event;

import net.minecraft.core.BlockSource;
import net.minecraft.world.item.ItemStack;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlock;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BlockDispenseBeforeEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final BlockSource src;
    private boolean isCancelled = false;
    private ItemStack item;

    public BlockDispenseBeforeEvent(BlockSource src, ItemStack item) {
        this.src = src;
        this.item = item;
    }

    public BlockSource getBlockSource() {
        return this.src;
    }

    public Block getBukkitBlock() {
        return CraftBlock.at(this.src.getLevel(), this.src.getPos());
    }

    public ItemStack getItem() {
        return this.item;
    }

    public void setItem(ItemStack newItem) {
        this.item = newItem;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return BlockDispenseBeforeEvent.HANDLERS;
    }
}
