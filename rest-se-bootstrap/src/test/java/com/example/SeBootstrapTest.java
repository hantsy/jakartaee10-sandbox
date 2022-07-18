package com.example;

import jakarta.ws.rs.SeBootstrap;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class SeBootstrapTest {

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .executor(executorService)
            .version(HttpClient.Version.HTTP_2)
            .build();

    SeBootstrap.Instance instance;

    @SneakyThrows
    @BeforeEach
    public void setup() {
        var latch = new CountDownLatch(1);
        SeBootstrap.start(RestConfig.class)
                .thenAccept(it -> {
                    instance = it;
                    latch.countDown();
                })
                .toCompletableFuture().join();

        latch.await(1000, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    @AfterEach
    public void teardown() {
        instance.stop()
                .thenAccept(
                        stopResult -> log.debug(
                                "Stop result: {} [Native stop result: {}]",
                                stopResult,
                                stopResult.unwrap(Object.class)
                        )
                ).toCompletableFuture().join();

    }

    @Test
    public void testGreetingEndpoints() {
        var greetingUri = instance.configuration().baseUriBuilder().path("/api/greeting").queryParam("name", "Hantsy").build();
        log.debug("greetingUri: {}", greetingUri);
        this.httpClient
                .sendAsync(
                        HttpRequest.newBuilder()
                                .GET()
                                .uri(greetingUri)
                                .header("Accept", "application/json")
                                .build()
                        ,
                        HttpResponse.BodyHandlers.ofString()
                )
                .thenApply(HttpResponse::body)
                .thenAccept(body -> {
                    log.debug("Greeting: {}", body);
                    assertThat(body).contains("Say 'Hello' to Hantsy at");
                })
                .join();
    }
}
