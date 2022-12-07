package com.example.it;

import com.example.EjbTodoService;
import com.example.Todo;
import com.example.TodoSamples;
import com.example.TodoService;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(ArquillianExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TodoServiceTest {

    private final static Logger LOGGER = Logger.getLogger(TodoServiceTest.class.getName());

    @Deployment()
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
                        Todo.class,
                        TodoService.class,
                        EjbTodoService.class,
                        TodoSamples.class
                )
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        LOGGER.log(Level.INFO, "war deployment: {0}", new Object[]{war.toString(true)});
        return war;
    }

    @Inject
    TodoService todoService;

    @EJB
    EjbTodoService ejbTodoService;

    @BeforeEach
    public void before() throws Exception {
    }

    @AfterEach
    public void after() throws Exception {
    }

    @Test
    @Order(1)
    public void testGetAllTodosEjbAsync() throws Exception {
        var todos = ejbTodoService.getAllTodosEjbAsync().get(1000, TimeUnit.MILLISECONDS);

        LOGGER.log(Level.INFO, "getAllTodosAsync result: {0}", new Object[]{todos});
        assertNotNull(todos);
        assertEquals(3, todos.size());
    }

    @Test
    @Order(2)
    public void testGetTodos() throws Exception {
        var todos = todoService.getAllTodos();
        LOGGER.log(Level.INFO, "getAllTodos:{}", new Object[]{todos});
        assertNotNull(todos);
        assertEquals(3, todos.size());
    }

    @Test
    @Order(3)
    public void testGetAllTodosAsync() throws Exception {
        todoService.getAllTodosAsync()
                .thenAccept(
                        todos -> {
                            LOGGER.log(Level.INFO, "getAllTodosAsync result: {0}", new Object[]{todos});
                            assertNotNull(todos);
                            assertEquals(3, todos.size());
                        }
                )
                .join();

    }

    @Test
    @Order(4)
    public void testCreateTodo() throws Exception {
        var title = "Testing Jakarta Components with Arquillian";
        var todo = todoService.create(Todo.of(title));
        LOGGER.log(Level.INFO, "create result: {0}", new Object[]{todo});
        assertNotNull(todo.getId());
        assertEquals(title, todo.getTitle());
    }

    @Test
    @Order(5)
    public void testCreateTodoAsync() throws Exception {
        var title = "Testing Jakarta Components with Arquillian";
        todoService.createAsync(Todo.of(title))
                .thenAccept(
                        todo -> {
                            LOGGER.log(Level.INFO, "createAsync result: {0}", new Object[]{todo});
                            assertNotNull(todo.getId());
                            assertEquals(title, todo.getTitle());
                        }
                )
                .toCompletableFuture()
                .join();
    }

}
