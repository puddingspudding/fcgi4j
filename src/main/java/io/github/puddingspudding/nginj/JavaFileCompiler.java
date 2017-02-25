/*
 * Copyright 2017 Philip Strecker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.puddingspudding.nginj;

import com.sun.nio.file.SensitivityWatchEventModifier;
import org.apache.logging.log4j.Logger;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by pudding on 18.02.17.
 */
class JavaFileCompiler {

    private final Path rootDir;

    private final Path tmpDir;

    private final Logger errorLogger;

    private final Logger traceLogger;

    private final WatchService watchService;

    public JavaFileCompiler(
        final Path rootDir,
        final Path tmpDir,
        final Logger errorLogger,
        final Logger traceogger
    ) {
        this.rootDir = rootDir;
        this.tmpDir = tmpDir;
        this.errorLogger = errorLogger;
        this.traceLogger = traceogger;

        try {
            this.watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Class compile(Path javaFile) {
        try {

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

            JavaFileObject fileObject = fileManager.getJavaFileObjects(javaFile.toFile()).iterator().next();

            compiler.getTask(
                null,
                fileManager,
                null,
                Arrays.asList("-d", this.tmpDir.toString()),
                null,
                Arrays.asList(fileObject)
            ).call();

            Path file = Files.walk(this.tmpDir, FileVisitOption.FOLLOW_LINKS)
                .filter(path -> path.toFile().toString().endsWith(".class"))
                .peek(p -> p.toFile().deleteOnExit())
                .findFirst()
                .orElseThrow(RuntimeException::new);

            String className = file.toString()
                .replace(".class", "")
                .replace(this.tmpDir.toString() + "/", "")
                .replace("/", ".");

            ClassLoader classLoader = new MyClassLoader(this.tmpDir, ClassLoader.getSystemClassLoader());

            Class cl = classLoader.loadClass(className);

            this.traceLogger.trace("compiled {}", javaFile);

            Files.delete(file);

            return cl;
        } catch (Exception e) {
            this.errorLogger.catching(e);
            return null;
        }
    }

    public void watch(Map<String, Class> compiledClasses) throws Exception {


        Files.walk(this.rootDir, FileVisitOption.FOLLOW_LINKS)
            .filter(Files::isDirectory)
            .forEach(dir -> {
                try {
                    dir.register(
                        watchService,
                        new WatchEvent.Kind[] {
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_MODIFY,
                            StandardWatchEventKinds.ENTRY_DELETE
                        },
                        SensitivityWatchEventModifier.HIGH
                    );
                } catch (IOException e) {
                    this.errorLogger.catching(e);
                }
            });

        while (!Thread.interrupted()) {
            try {
                WatchKey watchKey = watchService.take();
                watchKey.pollEvents().forEach(watchEvent -> {
                    Path path = ((Path) watchKey.watchable()).resolve((Path) watchEvent.context());
                    if (path.toFile().isFile()) {
                        WatchEvent.Kind kind = watchEvent.kind();
                        if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                            compiledClasses.remove(path.toString());
                        } else {
                            compiledClasses.put(path.toString(), compile(path));
                        }
                    } else {
                        try {
                            path.register(
                                watchService,
                                new WatchEvent.Kind[] {
                                        StandardWatchEventKinds.ENTRY_CREATE,
                                        StandardWatchEventKinds.ENTRY_MODIFY,
                                        StandardWatchEventKinds.ENTRY_DELETE
                                },
                                SensitivityWatchEventModifier.HIGH
                            );
                        } catch (IOException e) {
                            this.errorLogger.catching(e);
                        }
                    }
                });
                watchKey.reset();
            } catch (Exception e) {
                this.errorLogger.catching(e);
            }
        }
    }
}
