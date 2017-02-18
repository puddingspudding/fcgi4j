package io.github.puddingspudding.fcgi.server;

import javax.tools.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Exchanger;

/**
 * Created by pudding on 19.03.16.
 */
public class Main {

    public static void main(String[] args) throws Exception {

        DefaultServer fcgiServer = new DefaultServer(new InetSocketAddress("192.168.1.140", 8080));

        Map<String, Class> classMap = new ConcurrentHashMap<>();

        fcgiServer
            .setMap(classMap)
            .setPoolSize(25)
            .start();


        System.out.println("watcher start");
        WatchService watchService = FileSystems.getDefault().newWatchService();

        Map<WatchKey, Path> keys = new HashMap<>();
        Files.walkFileTree(Paths.get("/tmp/www/"), new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                WatchKey key = dir.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE
                );
                keys.put(key, dir);
                return FileVisitResult.CONTINUE;
            }
        });

        while (!Thread.interrupted()) {
            Thread.sleep(1);
            for (Map.Entry<WatchKey, Path> keyPaths : keys.entrySet()) {
                keyPaths.getKey().pollEvents().forEach(event -> {
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path file = keyPaths.getValue().resolve(ev.context());
                    if (!Files.isDirectory(file, LinkOption.NOFOLLOW_LINKS) && file.toString().endsWith(".java")) {

                        System.out.println("comile file: " + file);
                        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

                        JavaFileObject fileObject = fileManager.getJavaFileObjects(file.toFile()).iterator().next();

                        compiler.getTask(
                            null,
                            fileManager,
                            null,
                            Arrays.asList("-d", "/tmp/classes"),
                            null,
                            Arrays.asList(fileObject)
                        ).call();

                        fileManager.getLocation(StandardLocation.CLASS_OUTPUT).forEach(t -> {
                            try {
                                Arrays.asList(t.listFiles()).forEach(f -> {
                                    try {
                                        Files.walkFileTree(f.toPath(), new SimpleFileVisitor<Path>() {
                                            @Override
                                            public FileVisitResult visitFile(Path p, BasicFileAttributes attrs) throws IOException {
                                                if (p.toFile().isFile() && p.toString().endsWith(".class")) {
                                                    try {
                                                        String className = p.toString().replace(".class", "").replace("/tmp/classes/", "").replace("/", ".");
                                                        Class cl = URLClassLoader.newInstance(new URL[]{new URL("file:/tmp/classes/")})
                                                                .loadClass(className);

                                                        p.toFile().delete();

                                                        classMap.put(file.toString(), cl);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                return FileVisitResult.CONTINUE;
                                            }
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });


                        //compiler.run(null, null, null, "-d", "/tmp/www", file.toString());

                        /*DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
                        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

                        ClassLoader classLoader = ToolProvider.getSystemToolClassLoader();



                        try {
                            fileManager.setLocation(StandardLocation.locationFor("/tmp/"), null);
                            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(file.toFile());
                            compiler.getTask(null, fileManager, null, null, null, compilationUnits).call();

                            diagnostics.getDiagnostics();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
*/
                    }
                });
            }
        }


    }
}
