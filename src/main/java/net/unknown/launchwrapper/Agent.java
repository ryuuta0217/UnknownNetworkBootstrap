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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

public class Agent {
    private static Instrumentation INSTRUMENT = null;

    public static void addJar(File jarFile) throws IOException {
        if (!jarFile.exists()) throw new FileNotFoundException(jarFile.getAbsolutePath());
        if (jarFile.isDirectory() || !jarFile.getName().endsWith(".jar"))
            throw new IOException(jarFile.getName() + " is not a JarFile");
        if (INSTRUMENT != null) {
            INSTRUMENT.appendToSystemClassLoaderSearch(new JarFile(jarFile));
            System.out.println("Loaded Jar: " + jarFile.getPath());
            return;
        }
        throw new IllegalStateException("Failed to inject " + jarFile.getName() + " to SystemClassPath");
    }

    public static void premain(String args, Instrumentation instrumentation) {
        agentmain(args, instrumentation);
    }

    public static void agentmain(String args, Instrumentation instrumentation) {
        if (INSTRUMENT == null) INSTRUMENT = instrumentation;
        if (INSTRUMENT == null) throw new NullPointerException("WHY JAPANESE PEOPLE");
    }
}