package net.unknown.launchwrapper;

import net.minecraft.launchwrapper.LogWrapper;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.util.jar.JarFile;

public class Agent {
    private static Instrumentation INSTRUMENT = null;

    public static void addJar(File jarFile) throws IOException {
        if(!jarFile.exists()) throw new FileNotFoundException(jarFile.getAbsolutePath());
        if(jarFile.isDirectory() || !jarFile.getName().endsWith(".jar")) throw new IOException(jarFile.getName() + " is not a JarFile");
        if(INSTRUMENT != null) {
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