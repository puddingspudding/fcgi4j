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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.tools.JavaCompiler;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by pudding on 18.02.17.
 */
public class Main {

    private static final String configPath = "/etc/nginj/config.properties";

    public static void main(String[] args) throws Exception {

        Logger errorLogger = LogManager.getLogger("error");
        Logger traceLogger = LogManager.getLogger("trace");

        try {
            Properties properties = new Properties();

            try (
                FileInputStream fileInputStream = new FileInputStream(configPath)
            ) {
                properties.load(fileInputStream);
            }

            Path rootDir = Paths.get(properties.getProperty("rootDirectory"));
            rootDir.toFile().mkdirs();

            Path tmpDir = Paths.get(properties.getProperty("tmpDirectory"));
            tmpDir.toFile().mkdirs();

            JavaFileCompiler javaFileCompiler = new JavaFileCompiler(
                rootDir,
                tmpDir,
                errorLogger,
                traceLogger
            );

            SocketChannelConsumer socketChannelConsumer = new SocketChannelConsumer(
                rootDir,
                javaFileCompiler,
                errorLogger,
                traceLogger
            );

            InetSocketAddress address = new InetSocketAddress(
                properties.getProperty("host"),
                Integer.valueOf(properties.getProperty("port"))
            );

            new Server(
                rootDir,
                address,
                Integer.valueOf(properties.getProperty("threads")),
                errorLogger,
                traceLogger
            ).start(socketChannelConsumer);

        } catch (Exception e) {
            errorLogger.catching(e);
        }

    }

}
