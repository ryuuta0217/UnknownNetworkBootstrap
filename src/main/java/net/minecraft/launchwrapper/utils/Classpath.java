package net.minecraft.launchwrapper.utils;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;

public class Classpath {
    public static URL[] getClasspath() {
        ClassLoader loader = Classpath.class.getClassLoader();

        try {
            if (loader.getClass().getName().startsWith("jdk.internal.loader.ClassLoaders$")) {
                Field field = Unsafe.class.getDeclaredField("theUnsafe");
                field.setAccessible(true);
                Unsafe unsafe = (Unsafe) field.get(null);

                // jdk.internal.loader.ClassLoaders.AppClassLoader.ucp
                Field ucpField;
                try {
                    ucpField = loader.getClass().getDeclaredField("ucp");
                } catch (NoSuchFieldException | SecurityException e) {
                    ucpField = loader.getClass().getSuperclass().getDeclaredField("ucp");
                }

                long ucpFieldOffset = unsafe.objectFieldOffset(ucpField);
                Object ucpObject = unsafe.getObject(loader, ucpFieldOffset);

                // jdk.internal.loader.URLClassPath.path
                Field pathField = ucpField.getType().getDeclaredField("path");
                long pathFieldOffset = unsafe.objectFieldOffset(pathField);
                ArrayList<URL> path = (ArrayList<URL>) unsafe.getObject(ucpObject, pathFieldOffset);

                return path.toArray(new URL[0]);
            }
        } catch(Throwable t) {
            throw new RuntimeException(t);
        }
        throw new RuntimeException("Failed to load Classpath");
    }
}