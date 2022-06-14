package net.unknown.launchwrapper.mixins;

import net.minecraft.core.NonNullList;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SimpleContainer.class)
public class MixinSimpleContainer {
    @Shadow @Final @Mutable private int size;

    @Shadow @Final @Mutable public NonNullList<ItemStack> items;

    @Inject(method = "<init>(ILorg/bukkit/inventory/InventoryHolder;)V", at = @At("RETURN"))
    public void onInit(int i, InventoryHolder owner, CallbackInfo ci) {
        if((Object) this instanceof PlayerEnderChestContainer) {
            this.size = 54; // force overwrite EnderChest size
            this.items = NonNullList.withSize(this.size, ItemStack.EMPTY); // re-create items list
        }
    }

    @Inject(method = "getContainerSize", at = @At("RETURN"), cancellable = true)
    public void getContainerSize(CallbackInfoReturnable<Integer> cir) {
        if((Object) this instanceof PlayerEnderChestContainer enderChest) {
            if(enderChest.getBukkitOwner() instanceof CraftPlayer p) {
                // TODO FEATURE: Reference UNC PlayerData#isBuyedSixRowsEnderChest ?
                // but requires a MultiProject
                if(p.isOp() || p.hasPermission("unknown.mixin.feature.ender_chest.six_rows")) cir.setReturnValue(54);
                else if(p.hasPermission("unknown.mixin.feature.ender_chest.five_rows")) cir.setReturnValue(45);
                else if(p.hasPermission("unknown.mixin.feature.ender_chest.four_rows")) cir.setReturnValue(36);
                else cir.setReturnValue(27);
            }
        }
    }
}
