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

import org.apache.logging.log4j.Logger;

import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * Created by pudding on 18.02.17.
 */
class Server {

    private final Path rootDirectory;

    private final SocketAddress address;

    private final int threads;

    private final Logger errorLogger;

    private final Logger traceLogger;

    public Server(
        Path rootDirectory,
        SocketAddress address,
        int threads,
        Logger errorLogger,
        Logger traceLogger
    ) {
        this.rootDirectory = rootDirectory;
        this.address = address;
        this.threads = threads;
        this.errorLogger = errorLogger;
        this.traceLogger = traceLogger;
    }

    public void start(Consumer<SocketChannel> consumer) throws Exception {

        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open().bind(this.address);

            ExecutorService executor = Executors.newFixedThreadPool(this.threads);
            IntStream.rangeClosed(0, this.threads).forEach(i -> {
                executor.execute(() -> {
                    while (!Thread.interrupted()) {
                        try (SocketChannel socketChannel = serverSocketChannel.accept()) {
                            consumer.accept(socketChannel);
                            socketChannel.shutdownInput();
                            socketChannel.shutdownOutput();
                        } catch (Exception e) {
                            this.errorLogger.catching(e);
                        }
                    }
                });
            });

            this.traceLogger.trace("listen {}", this.address);
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (Exception e) {
            this.errorLogger.catching(e);
        }
    }




}
