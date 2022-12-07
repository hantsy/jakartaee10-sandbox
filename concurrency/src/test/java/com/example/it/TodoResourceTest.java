package com.example.it;

import com.example.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(ArquillianExtension.class)
public class TodoResourceTest {

    private final static Logger LOGGER = Logger.getLogger(TodoResourceTest.class.getName());

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
                        TodoResources.class,
                        TodoResource.class,
                        Todo.class,
                        TodoService.class,
                        EjbTodoService.class,
                        TodoSamples.class,
                        RestConfig.class
                )
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
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
    public void testGetTodos() throws Exception {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/todos"));
        CompletionStage<Response> responseCompletionStage = target
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .rx().get();

        responseCompletionStage
                .thenAccept(
                        r -> {
                            LOGGER.log(Level.INFO, "Get /todos response status: {0}", r.getStatus());
                            assertEquals(200, r.getStatus());
                            String jsonString = r.readEntity(String.class);
                            LOGGER.log(Level.INFO, "Get /todos result string: {0}", jsonString);
                        }
                )
                .toCompletableFuture()
                .join();

    }

    @Test
    @RunAsClient
    public void testGetAllTodosEjbFuture() throws Exception {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/todos/getAllTodosEjbFuture"));
        CompletionStage<Response> responseCompletionStage = target
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .rx().get();

        responseCompletionStage
                .thenAccept(
                        r -> {
                            LOGGER.log(Level.INFO, "Get /todos/getAllTodosEjbFuture response status: {0}", r.getStatus());
                            assertEquals(200, r.getStatus());
                            String jsonString = r.readEntity(String.class);
                            LOGGER.log(Level.INFO, "Get todos result string: {0}", jsonString);
                        }
                )
                .toCompletableFuture()
                .join();

    }

    @Test
    @RunAsClient
    public void testGetAllTodosAsync() throws Exception {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/todos/getAllTodosAsync"));
        CompletionStage<Response> responseCompletionStage = target
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .rx().get();

        responseCompletionStage
                .thenAccept(
                        r -> {
                            LOGGER.log(Level.INFO, "Get /todos/getAllTodosAsync response status: {0}", r.getStatus());
                            assertEquals(200, r.getStatus());
                            String jsonString = r.readEntity(String.class);
                            LOGGER.log(Level.INFO, "Get todos result string: {0}", jsonString);
                        }
                )
                .toCompletableFuture()
                .join();

    }

    @Test
    @RunAsClient
    public void testGetAllTodosAndAsync() throws Exception {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/todos/getAllTodosAndAsync"));
        CompletionStage<Response> responseCompletionStage = target
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .rx().get();

        responseCompletionStage
                .thenAccept(
                        r -> {
                            LOGGER.log(Level.INFO, "Get /todos/getAllTodosAndAsync response status: {0}", r.getStatus());
                            assertEquals(200, r.getStatus());
                            String jsonString = r.readEntity(String.class);
                            LOGGER.log(Level.INFO, "Get todos result string: {0}", jsonString);
                        }
                )
                .toCompletableFuture()
                .join();

    }

    @Test
    @RunAsClient
    public void testGetAllTodosAndConcurrencyAsync() throws Exception {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/todos/getAllTodosAndConcurrencyAsync"));
        CompletionStage<Response> responseCompletionStage = target
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .rx().get();

        responseCompletionStage
                .thenAccept(
                        r -> {
                            LOGGER.log(Level.INFO, "Get /todos/getAllTodosAndConcurrencyAsync response status: {0}", r.getStatus());
                            assertEquals(200, r.getStatus());
                            String jsonString = r.readEntity(String.class);
                            LOGGER.log(Level.INFO, "Get todos result string: {0}", jsonString);
                        }
                )
                .toCompletableFuture()
                .join();

    }

    @Test
    @RunAsClient
    public void testCreateTodo() throws Exception {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/todos"));
        CompletionStage<Response> responseCompletionStage = target
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .rx().post(Entity.json(Todo.of("test")));

        responseCompletionStage
                .thenAccept(
                        r -> {
                            LOGGER.log(Level.INFO, "Post /todos response status: {0}", r.getStatus());
                            assertEquals(201, r.getStatus());
                            var location = r.getHeaderString("Location");
                            LOGGER.log(Level.INFO, "Create todo response header Location: {0}", location);
                            assertNotNull(location);
                        }
                )
                .toCompletableFuture()
                .join();

    }

    @Test
    @RunAsClient
    public void testCreateTodoAsync() throws Exception {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/todos/async"));
        CompletionStage<Response> responseCompletionStage = target
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .rx().post(Entity.json(Todo.of("test")));

        responseCompletionStage
                .thenAccept(
                        r -> {
                            LOGGER.log(Level.INFO, "Post /todos/async response status: {0}", r.getStatus());
                            assertEquals(201, r.getStatus());
                            var location = r.getHeaderString("Location");
                            LOGGER.log(Level.INFO, "Create todo response header Location: {0}", location);
                            assertNotNull(location);
                        }
                )
                .toCompletableFuture()
                .join();

    }

}
