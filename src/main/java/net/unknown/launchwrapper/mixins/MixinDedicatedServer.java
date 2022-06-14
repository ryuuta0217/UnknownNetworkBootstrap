package net.unknown.launchwrapper.mixins;

import net.minecraft.server.dedicated.DedicatedServer;
import org.bukkit.Bukkit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public abstract class MixinDedicatedServer {
    @Inject(method = "initServer", at = @At("RETURN"))
    private void onInit(CallbackInfoReturnable<Boolean> cir) {
        Bukkit.getLogger().info("""
                

                ██╗   ██╗       ███╗   ██╗
                ██║   ██║       ████╗  ██║
                ██║   ██║       ██╔██╗ ██║
                ██║   ██║       ██║╚██╗██║
                ╚██████╔╝██╗    ██║ ╚████║██╗
                 ╚═════╝ ╚═╝    ╚═╝  ╚═══╝╚═╝
                 """);
    }
}
