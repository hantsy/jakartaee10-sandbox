package com.example.it;

import com.example.event.NotificationSender;
import com.example.event.TodoCompleted;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

@ExtendWith(ArquillianExtension.class)
public class CdiAsyncEventTest {

    private final static Logger LOGGER = Logger.getLogger(CdiAsyncEventTest.class.getName());

    @Deployment()
    public static WebArchive createDeployment() {
        File[] extraJars = Maven
                .resolver()
                .loadPomFromFile("pom.xml")
                .importCompileAndRuntimeDependencies()
                .resolve(
                        "org.assertj:assertj-core",
                        "org.awaitility:awaitility"
                )
                .withTransitivity()
                .asFile();
        var war = ShrinkWrap.create(WebArchive.class)
                .addAsLibraries(extraJars)
                .addPackage(TodoCompleted.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        LOGGER.log(Level.INFO, "war deployment: {0}", new Object[]{war.toString(true)});
        return war;
    }

    @Inject
    NotificationSender sender;

    @BeforeEach
    public void before() throws Exception {
    }

    @AfterEach
    public void after() throws Exception {
    }

    @Test
    public void testCdiAsyncEvent() throws Exception {
        LongStream.range(0, 10)
                .forEachOrdered(idx -> sender.send(idx));

        Awaitility.await()
                .atMost(Duration.ofMillis(1000))
                .untilAsserted(() -> Assertions.assertThat(sender.getSentCounter().get()).isEqualTo(10L));
    }
}
