package net.unknown.launchwrapper.mixins;

import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import org.bukkit.Bukkit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.jar.Attributes;

@Mixin(DedicatedServer.class)
public abstract class MixinDedicatedServer {
    @Shadow public abstract DedicatedServerProperties getProperties();

    @Accessor("settings")
    public abstract DedicatedServerSettings getSettings();

    @Inject(method = "initServer", at = @At("RETURN"))
    private void onInit(CallbackInfoReturnable<Boolean> cir) {
        Bukkit.getLogger().info("Hello from MixinDedicatedServer");
        Bukkit.getLogger().info("Settings test: " + getProperties().gamemode);
    }
}
