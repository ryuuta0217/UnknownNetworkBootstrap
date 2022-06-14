package net.unknown.launchwrapper.mixins;

import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CraftServer.class)
public class MixinCraftServer {
    /**
     * Gets the name of server implementation.
     *
     * @return Server Software Name
     * @author ryuuta0217
     */
    @Overwrite
    public String getName() {
        return "Unknown Network";
    }
}
