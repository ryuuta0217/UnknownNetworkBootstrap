package org.spongepowered.asm.launch;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.asm.launch.platform.CommandLineOptions;

import java.io.File;
import java.util.List;

public class UnknownNetworkBootstrap implements ITweaker {
    private String[] launchArguments = new String[0];

    public UnknownNetworkBootstrap() {
        MixinBootstrap.start();
    }

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        if (args != null && !args.isEmpty()) {
            this.launchArguments = args.toArray(new String[0]);
        }

        MixinBootstrap.doInit(CommandLineOptions.ofArgs(args));
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        MixinBootstrap.inject();
    }

    @Override
    public String getLaunchTarget() {
        return "org.bukkit.craftbukkit.Main";
    }

    @Override
    public String[] getLaunchArguments() {
        return this.launchArguments;
    }
}
