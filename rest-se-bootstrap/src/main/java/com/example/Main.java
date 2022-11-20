package com.example;

import jakarta.ws.rs.SeBootstrap;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

@Slf4j
public class Main {
        private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

        public static void main(String[] args) throws InterruptedException, IOException {
                // mirroprofile config
                // final Config config = ConfigProvider.getConfig();
                // SeBootstrap.Configuration configuration = SeBootstrap.Configuration.builder()
                // .from(config)
                // .host("localhost")
                // .port(8080)
                // .protocol("http")
                // .property("context.path", "/api")
                // .rootPath("/")
                // .sslContext(null)
                // .sslClientAuthentication(null)
                // .build();
                SeBootstrap.Configuration configuration = SeBootstrap.Configuration.builder()
                                .host("localhost")
                                .port(8080)
                                .protocol("http")
                                .build();
                SeBootstrap.start(RestConfig.class, configuration).thenAccept(instance -> {
                        instance.stopOnShutdown(stopResult -> log.debug(
                                        "Stop result: {} [Native stop result: {}]",
                                        stopResult,
                                        stopResult.unwrap(Object.class)));
                        final URI uri = instance.configuration().baseUri();

                        log.debug(
                                        "Instance {} running at {} [Native handle: {}].%n",
                                        instance, uri,
                                        instance.unwrap(Object.class));
                        log.debug("Send SIGKILL to shutdown.");
                }

                ).toCompletableFuture().join();

                // stop quit.
                System.in.read();
        }
}
