package com.example.it;

import com.example.ConcurrencyAsyncResource;
import com.example.RestConfig;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ArquillianExtension.class)
public class ConcurrencyAsyncResourceTest {

    private final static Logger LOGGER = Logger.getLogger(ConcurrencyAsyncResourceTest.class.getName());

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        File[] extraJars = Maven
                .resolver()
                .loadPomFromFile("pom.xml")
                .importCompileAndRuntimeDependencies()
                .resolve("org.assertj:assertj-core")
                .withTransitivity()
                .asFile();
        var war = ShrinkWrap.create(WebArchive.class)
                .addAsLibraries(extraJars)
                .addClasses(
                        ConcurrencyAsyncResource.class,
                        RestConfig.class
                )
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        LOGGER.log(Level.INFO, "war deployment: {0}", new Object[]{war.toString(true)});
        return war;
    }

    @ArquillianResource
    private URL baseUrl;

    Client client;

    @BeforeEach
    public void before() throws Exception {
        LOGGER.log(Level.INFO, "baseURL: {0}", new Object[]{baseUrl.toExternalForm()});
        client = ClientBuilder.newClient();
    }

    @AfterEach
    public void after() throws Exception {
        client.close();
    }

    @Test
    @RunAsClient
    public void testConcurrencyAsyncResource() throws Exception {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/concurrencyAsync"));
        CompletionStage<Response> responseCompletionStage = target
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .rx().get();

        responseCompletionStage
                .thenAccept(
                        r -> {
                            LOGGER.log(Level.INFO, "Get /concurrencyAsync response status: {0}", r.getStatus());
                            assertEquals(200, r.getStatus());
                            String jsonString = r.readEntity(String.class);
                            LOGGER.log(Level.INFO, "Get /concurrencyAsync result string: {0}", jsonString);
                            assertEquals("Concurrency Async resource", jsonString);
                        }
                )
                .toCompletableFuture()
                .join();

    }


}
