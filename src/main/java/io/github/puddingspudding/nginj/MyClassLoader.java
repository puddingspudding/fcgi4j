package io.github.puddingspudding.nginj;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Created by pudding on 25.02.17.
 */
public class MyClassLoader extends ClassLoader {

    private final Path dir;

    public MyClassLoader(Path dir, ClassLoader parent) {
        super(parent);
        this.dir = dir;
    }

    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        try {
            Path path = Paths.get(this.dir
                + File.separator
                + name.replace(".", File.separator)
                + ".class");

            if (!path.toFile().exists()) {
                return super.loadClass(name);
            }

            byte[] classData = Files.readAllBytes(path);

            Class<?> cl = defineClass(name, classData, 0, classData.length);

            return cl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
