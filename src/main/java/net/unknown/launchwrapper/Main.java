package net.unknown.launchwrapper;

import net.minecraft.launchwrapper.Launch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    public static void main(String[] args) throws IOException {
        Agent.addJar(new File("./versions/1.18.1/paper-1.18.1.jar"));

        Map<String, File> toLoadLibraries = new HashMap<>();

        Files.walk(Paths.get("./libraries")).forEach(file -> {
            File f = file.toFile();
            if(f.isDirectory()) return;
            if(f.getName().endsWith(".jar")) {
                try {
                    String[] g = filePathToDependencyName(f).split(":", 3);
                    toLoadLibraries.put(g[0] + ":" + g[1], f);
                } catch (IOException ignored) {}
            }
        });

        for (File file : toLoadLibraries.values()) {
            Agent.addJar(file);
        }

        Launch.main(args);
    }

    private static String filePathToDependencyName(File file) throws IOException {
        if(file.isDirectory()) throw new IllegalArgumentException("It is directory.");

        String group = file.getParentFile().getParentFile().getParentFile().getPath()
                .replace("." + FILE_SEPARATOR + "libraries" + FILE_SEPARATOR, "")
                .replace(FILE_SEPARATOR, ".");
        String artifact = file.getParentFile().getParentFile().getName();
        String version = file.getParentFile().getName();

        return group + ":" + artifact + ":" + version;
    }
}
