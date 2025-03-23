/*
 * Copyright (c) 2023 Unknown Network Developers and contributors.
 *
 * All rights reserved.
 *
 * NOTICE: This license is subject to change without prior notice.
 *
 * Redistribution and use in source and binary forms, *without modification*,
 *     are permitted provided that the following conditions are met:
 *
 * I. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 * II. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 * III. Neither the name of Unknown Network nor the names of its contributors may be used to
 *     endorse or promote products derived from this software without specific prior written permission.
 *
 * IV. This source code and binaries is provided by the copyright holders and contributors "AS-IS" and
 *     any express or implied warranties, including, but not limited to, the implied warranties of
 *     merchantability and fitness for a particular purpose are disclaimed.
 *     In not event shall the copyright owner or contributors be liable for
 *     any direct, indirect, incidental, special, exemplary, or consequential damages
 *     (including but not limited to procurement of substitute goods or services;
 *     loss of use data or profits; or business interruption) however caused and on any theory of liability,
 *     whether in contract, strict liability, or tort (including negligence or otherwise)
 *     arising in any way out of the use of this source code, event if advised of the possibility of such damage.
 */

package net.unknown.launchwrapper;

import net.minecraft.launchwrapper.Launch;
import org.spongepowered.asm.util.asm.ASM;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class Main {
    public static boolean FORCE_ALLOW_TRADE_REBALANCE_FEATURES = System.getProperty("UnknownNetworkMagic") != null && System.getProperty("UnknownNetworkMagic").contains("trade-rebalance");
    public static boolean FORCE_ALLOW_REDSTONE_EXPERIMENTS_FEATURES = System.getProperty("UnknownNetworkMagic") != null && System.getProperty("UnknownNetworkMagic").contains("redstone-experiments");
    public static boolean FORCE_ALLOW_MINECART_IMPROVEMENTS_FEATURES = System.getProperty("UnknownNetworkMagic") != null && System.getProperty("UnknownNetworkMagic").contains("minecart-improvements");
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static Path PAPERCLIP_JAR = System.getProperties().contains("unknown.bootstrap.paperclip_jar_path") ? Path.of(System.getProperty("unknown.bootstrap.paperclip_jar_path")) : Path.of("paper.jar");
    public static Path SERVER_JAR = System.getProperties().contains("unknown.bootstrap.server_jar_path") ? Path.of(System.getProperty("unknown.bootstrap.server_jar_path")) : null;

    public static void main(String[] args) throws IOException, InterruptedException {
        if (FORCE_ALLOW_TRADE_REBALANCE_FEATURES || FORCE_ALLOW_REDSTONE_EXPERIMENTS_FEATURES || FORCE_ALLOW_MINECART_IMPROVEMENTS_FEATURES) {
            String activatedFeatureFlags = String.join(",", Stream.of((FORCE_ALLOW_TRADE_REBALANCE_FEATURES ? "trade_rebalance" : ""), (FORCE_ALLOW_REDSTONE_EXPERIMENTS_FEATURES ? "redstone_experiments" : ""), (FORCE_ALLOW_MINECART_IMPROVEMENTS_FEATURES ? "minecart_improvements" : "")).filter(s -> !s.isEmpty()).toList());
            System.out.println("\n" +
                               "\n" +
                               "                           Unknown Network Bootstrap\n" +
                               "Hey! You're activated \"" + activatedFeatureFlags + "\" feature, but this is contains bugs or crashes. Be careful!\n" +
                               "Server will start after 3 seconds...\n" +
                               "\n" +
                               "\n");
            Thread.sleep(TimeUnit.SECONDS.toMillis(3));
        }

        File paperJar = PAPERCLIP_JAR.toFile();

        URL[] classpathUrls;
        if (paperJar.exists()) {
            try {
                ClassLoader cl = new URLClassLoader(new URL[]{ paperJar.toURL() }, null);
                Class<?> paperClipClass = cl.loadClass("io.papermc.paperclip.Paperclip");
                Method setupClasspathField = paperClipClass.getDeclaredMethod("setupClasspath");
                if (Modifier.isStatic(setupClasspathField.getModifiers())) {
                    if (setupClasspathField.trySetAccessible()) {
                        classpathUrls = (URL[]) setupClasspathField.invoke(null);
                        if (classpathUrls.length == 0) {
                            throw new IllegalStateException("Failed to setup classpath");
                        }
                    } else {
                        throw new IllegalStateException("Failed to invoke Paperclip#setupClasspath");
                    }
                } else {
                    throw new IllegalStateException("Failed to lookup Paperclip#setupClasspath");
                }
            } catch(Throwable t) {
                t.printStackTrace();
                return;
            }
        } else {
            throw new FileNotFoundException("paper.jar が見つかりませんでした");
        }

        Path workingPath = new File(System.getProperty("user.dir")).toPath();

        for (URL classpathUrl : classpathUrls) {
            try {
                Agent.addJar(new File(".", workingPath.relativize(Paths.get(classpathUrl.toURI())).toString()));
            } catch (URISyntaxException ignored) {
                System.out.println("Failed to load " + classpathUrl + ", but proceeding...");
            }
        }

        try {
            SERVER_JAR = Path.of(Class.forName("io.papermc.paper.pluginremap.ReobfServer").getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        Launch.main(args);
    }

    private static String filePathToDependencyName(File file) throws IOException {
        if (file.isDirectory()) throw new IllegalArgumentException("It is directory.");

        String group = file.getParentFile().getParentFile().getParentFile().getPath()
                .replace("." + FILE_SEPARATOR + "libraries" + FILE_SEPARATOR, "")
                .replace(FILE_SEPARATOR, ".");
        String artifact = file.getParentFile().getParentFile().getName();
        String version = file.getParentFile().getName();

        return group + ":" + artifact + ":" + version;
    }
}
