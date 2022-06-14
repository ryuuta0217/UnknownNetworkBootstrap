package net.unknown.launchwrapper.mixins;

import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
    /**
     * Unknown Network - Unknown Network > // Paper - Paper > // Spigot - Spigot > // CraftBukkit - cb > vanilla!
     *
     * @author ryuuta0217
     * @return Server Modded Name
     */
    @DontObfuscate
    @Overwrite
    public String getServerModName() {
        return "Unknown Network";
    }
}
