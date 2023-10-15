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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main {
    public static boolean FORCE_ALLOW_BUNDLE_FEATURES = System.getProperty("UnknownNetworkMagic") != null && System.getProperty("UnknownNetworkMagic").contains("bundle");
    public static boolean FORCE_ALLOW_TRADE_REBALANCE_FEATURES = System.getProperty("UnknownNetworkMagic") != null && System.getProperty("UnknownNetworkMagic").contains("trade-rebalance");
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    public static void main(String[] args) throws IOException, InterruptedException {
        if (FORCE_ALLOW_BUNDLE_FEATURES || FORCE_ALLOW_TRADE_REBALANCE_FEATURES) {
            String activatedFeatureFlags = String.join(",", (FORCE_ALLOW_BUNDLE_FEATURES ? "bundle" : ""), (FORCE_ALLOW_TRADE_REBALANCE_FEATURES ? "trade_rebalance" : ""));
            System.out.println("\n" +
                               "\n" +
                               "                           Unknown Network Bootstrap\n" +
                               "Hey! You're activated \"" + activatedFeatureFlags + "\" feature, but this is contains bugs or crashes. Be careful!\n" +
                               "Server will start after 3 seconds...\n" +
                               "\n" +
                               "\n");
            Thread.sleep(TimeUnit.SECONDS.toMillis(3));
        }
        Agent.addJar(new File("./versions/1.20.2/paper-1.20.2.jar"));

        Map<String, File> toLoadLibraries = new HashMap<>();

        Files.walk(Paths.get("./libraries")).forEach(file -> {
            File f = file.toFile();
            if (f.isDirectory()) return;
            if (f.getName().endsWith(".jar")) {
                try {
                    String[] g = filePathToDependencyName(f).split(":", 3);
                    toLoadLibraries.put(g[0] + ":" + g[1], f);
                } catch (IOException ignored) {
                }
            }
        });

        for (File file : toLoadLibraries.values()) {
            Agent.addJar(file);
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
