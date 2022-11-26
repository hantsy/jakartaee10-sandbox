# Jakarta REST 3.1

Jakarta REST includes a dozen of small improvements, and also introduces two major new features.

* Java SE Bootstrap API
* The long-awaited standard Multipart API

Let's explore the features by examples.

## Bootstrap API

Like the CDI Bootstrap API to host a CDI container in Java SE environment, Jakarta REST Bootstrap API provides similar API to serve a Jaxrs application on embedded servers.

### Creating Java SE Project

Follow the steps in the [Jakarta Persistence - Example: Hibernate 6.1](./jpa/hibernate.md) to create a simple Java SE project.

We will use sl4j/logback as logging framework, and also use Lombok annotations to simplify the Java codes.

Add the following dependencies in the project *pom.xml*.

```xml
<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
    <version>1.18.24</version>
</dependency>

<!-- logging with logback -->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>2.0.4</version>
</dependency>
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>jcl-over-slf4j</artifactId>
    <version>2.0.4</version>
</dependency>
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-core</artifactId>
    <version>1.4.4</version>
</dependency>
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.4.4</version>
</dependency>
```

Create *src/main/resources/logback.xml* to set up Logback.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <property name="LOGS" value="./logs"/>

    <appender name="Console"
              class="ch.qos.logback.core.ConsoleAppender">
        <layout class="ch.qos.logback.classic.PatternLayout">
            <Pattern>
                %green(%d{ISO8601}) %highlight(%-5level) [%blue(%t)] %yellow(%C{1.}): %msg%n%throwable
            </Pattern>
        </layout>
    </appender>

    <appender name="RollingFile"
              class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOGS}/app.log</file>
        <encoder
                class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <Pattern>%d %p %C{1.} [%t] %m%n</Pattern>
        </encoder>

        <rollingPolicy
                class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!-- rollover daily and when the file reaches 10 MegaBytes -->
            <fileNamePattern>${LOGS}/archived/app-%d{yyyy-MM-dd}.%i.log
            </fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy
                    class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>10MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
        </rollingPolicy>
    </appender>

    <!-- LOG everything at INFO level -->
    <root level="info">
        <appender-ref ref="RollingFile"/>
        <appender-ref ref="Console"/>
    </root>
    <logger name="org.glassfish.jersey.server" level="DEBUG">
    </logger>
    <logger name="com.example" level="debug" additivity="false">
        <appender-ref ref="RollingFile"/>
        <appender-ref ref="Console"/>
    </logger>
</configuration>
```

Add the Jakarta REST API into the dependencies.

```xml
<dependencies>
    <dependency>
        <groupId>jakarta.ws.rs</groupId>
        <artifactId>jakarta.ws.rs-api</artifactId>
        <version>3.1.0</version>
    </dependency>
    //...
</dependencies>
```

Create a *main* class as the application entry.

```java
@Slf4j
public class Main {

    public static void main(String[] args) throws InterruptedException, IOException {
        SeBootstrap.Configuration configuration = SeBootstrap.Configuration.builder()
                .host("localhost")
                .port(8080)
                .protocol("http")
                .build();
        SeBootstrap.start(RestConfig.class, configuration)
            .thenAccept(instance -> {
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
            })
            .toCompletableFuture().join();

        // stop quit.
        System.in.read();
    }
}
```

The `@Slf4j` is from Lombok, which will add a `org.slf4j.Logger` declaration to `Main` class at compile time.

To customize `SeBootstrap`, you can use `SeBootstrap.Configuration.builder()` to produces a `SeBootstrap.Configuration` which can be used to start `SeBootstrap` instance.

The `SeBootstrap.start` accepts a Rest `Application` entry class and an optional `SeBootstrap.Configuration`, in `thenAccept` block, a Bootstrap server instance is available to consume. The `instance.stopOnShutdown` is used to setup a shutdown hook, then print the application startup information.

The `.toCompletableFuture().join()` will wait async execution to be completed.

Let's have a look at `RestConfig` - which is the REST Application entry class.

```java
@ApplicationPath("/api")
public class RestConfig extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(GreetingResource.class);
    }
}
```

Add a simple Jaxrs Resource - `GreetingResource`.

```java
@Path("greeting")
@RequestScoped
public class GreetingResource {

    @GET
    public String hello(@QueryParam("name") String name) {
        return "Say 'Hello' to " + (name == null ? "World" : name) + " at " + LocalDateTime.now();
    }
}
```

Although CDI *beans.xml* is optional in Jakarta EE environment. To run start SeBootstrap instance in a Java SE environment, you have to create an empty CDI *beans.xml*, put it into *src/main/resources/META-INFO*.

```xml
<beans xmlns="https://jakarta.ee/xml/ns/jakartaee"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/beans_4_0.xsd"
  bean-discovery-mode="annotated" version="4.0">
</beans>
```

To run this application, it requires a runtime embedded server. Both Jersey and Resteasy provides several options.

### Jersey

Add Jersey container related dependencies into the project *pom.xml*, create a standard Maven profiles.

```xml
<profiles>
    <profile>
        <id>jersey</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <dependencies>
            <dependency>
                <groupId>org.glassfish.jersey.core</groupId>
                <artifactId>jersey-server</artifactId>
            </dependency>
            <dependency>
                <groupId>org.glassfish.jersey.containers</groupId>
                <artifactId>jersey-container-jdk-http</artifactId>
            </dependency>
            <dependency>
                <groupId>org.glassfish.jersey.inject</groupId>
                <artifactId>jersey-cdi2-se</artifactId>
            </dependency>
        </dependencies>
    </profile>
    //...
```

There are [several Jersey containers](https://repo1.maven.org/maven2/org/glassfish/jersey/containers/) provided in the latest Jersey. Here we used the simplest one which is based on the JDK built-in HttpServer.

Now you can run `Main` in your IDEs directly by click the run button.

You will see the following info in the console window.

```bash
Nov 22, 2022 10:42:46 PM org.glassfish.jersey.message.internal.MessagingBinders$EnabledProvidersBinder bindToBinder
WARNING: A class jakarta.activation.DataSource for a default provider MessageBodyWriter<jakarta.activation.DataSource> was not found. The provider is not available.
Nov 22, 2022 10:42:46 PM org.glassfish.jersey.server.wadl.WadlFeature configure
WARNING: JAX-B API not found . WADL feature is disabled.
2022-11-22 22:42:46,667 INFO  [ForkJoinPool.commonPool-worker-1] org.jboss.weld.bootstrap.WeldStartup: WELD-000900: 5.0.1 (Final)
2022-11-22 22:42:46,935 INFO  [ForkJoinPool.commonPool-worker-1] org.jboss.weld.environment.deployment.discovery.ReflectionDiscoveryStrategy: WELD-ENV-000014: Falling back to Java Reflection for bean-discovery-mode="annotated" discovery. Add org.jboss:jandex to the classpath to speed-up startup.
2022-11-22 22:42:47,035 INFO  [ForkJoinPool.commonPool-worker-1] org.jboss.weld.bootstrap.WeldStartup: WELD-000101: Transactional services not available. Injection of @Inject UserTransaction not available. Transactional observers will be invoked synchronously.
2022-11-22 22:42:47,796 INFO  [ForkJoinPool.commonPool-worker-1] org.jboss.weld.environment.se.WeldContainer: WELD-ENV-002003: Weld SE container eb0f72e8-e3e1-4f72-bbae-045cc3791db4 initialized
2022-11-22 22:42:48,005 DEBUG [ForkJoinPool.commonPool-worker-1] com.example.Main: Instance org.glassfish.jersey.server.internal.RuntimeDelegateImpl$1@2c7c9fa9 running at http://localhost:8080/ [Native handle: org.glassfish.jersey.jdkhttp.JdkHttpServer@57458589].%n
2022-11-22 22:42:48,006 DEBUG [ForkJoinPool.commonPool-worker-1] com.example.Main: Send SIGKILL to shutdown.
```

Now open another terminal window, and use `curl` command to test the endpoint `/api/greeting`.

```bash
curl http://localhost:8080/api/greeting?name=Hantsy

Say 'Hello' to Hantsy at 2022-11-22T22:45:41.129167100
```

Utilize with maven-assemble-plugin, we can package the application classes with all dependencies into one archive.

```xml
 <!-- Maven Assembly Plugin -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-assembly-plugin</artifactId>
    <version>3.4.2</version>
    <configuration>
        <descriptorRefs>
            <descriptorRef>jar-with-dependencies</descriptorRef>
        </descriptorRefs>
        <!-- MainClass in mainfest make a executable jar -->
        <archive>
            <manifest>
                <addClasspath>true</addClasspath>
                <mainClass>com.example.Main</mainClass>
            </manifest>
        </archive>

    </configuration>
    <executions>
        <execution>
            <id>make-assembly</id>
            <phase>package</phase> <!-- bind to the packaging phase -->
            <goals>
                <goal>single</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Open a terminal, and switch to the project root, and run the following command to build and run the application in Jersey embedded container.

```xml
>mvn clean package -DskipTests -D"maven.test.skip=true"
...
[INFO]
[INFO] --- maven-assembly-plugin:3.4.2:single (make-assembly) @ rest-se-bootstrap-examples ---
[INFO] Building jar: D:\hantsylabs\jakartaee10-sandbox\rest-se-bootstrap\target\rest-se-bootstrap-examples-jar-with-dependencies.jar
...
>java -jar .\target\rest-se-bootstrap-examples-jar-with-dependencies.jar
...
WELD-000101: Transactional services not available. Injection of @Inject UserTransaction not available.
Transactional observers will be invoked synchronously.
2022-11-26 13:50:45,132 INFO  [ForkJoinPool.commonPool-worker-1] org.jboss.weld.environment.se.WeldContainer: WELD-ENV-002003: Weld SE container 4744564b-922c-4612-a88f-8095c4d7293b initialized
2022-11-26 13:50:45,257 DEBUG [ForkJoinPool.commonPool-worker-1] com.example.Main: Instance org.glassfish.jersey.server.internal.RuntimeDelegateImpl$1@2fa5468d running at http://localhost:8080/ [Native handle: org.glassfish.jersey.jdkhttp.JdkHttpServer@78879a1c].%n
2022-11-26 13:50:45,257 DEBUG [ForkJoinPool.commonPool-worker-1] com.example.Main: Send SIGKILL to shutdown.
```

### Resteasy

Let's switch to use Redhat [Resteasy](https://resteasy.dev/) as runtime.

Create a new Maven profile for Resteasy.

```xml
<profile>
    <id>resteasy</id>
    <properties>
        <jboss-logmanager.version>2.1.19.Final</jboss-logmanager.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.jboss.logmanager</groupId>
            <artifactId>jboss-logmanager</artifactId>
            <version>${jboss-logmanager.version}</version>
        </dependency>
        <dependency>
            <groupId>org.jboss.resteasy</groupId>
            <artifactId>resteasy-undertow-cdi</artifactId>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <artifactId>maven-surefire-plugin</artifactId>
                <configuration>
                    <systemPropertyVariables>
                        <java.util.logging.manager>org.jboss.logmanager.LogManager</java.util.logging.manager>
                    </systemPropertyVariables>
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>
```

There are [several Embedded containers](https://docs.jboss.org/resteasy/docs/6.2.1.Final/userguide/html_single/index.html#RESTEasy_Embedded_Container) existed in Resteasy to serve a Rest Application. Here we choose the one that based on Redhat Undertow with CDI support.

Open a terminal, switch to the project root, and run the following command to start the application in this Resteasy embedded server.

```bash
>mvn clean package -Presteasy  -DskipTests -D"maven.test.skip=true"
...
[INFO] --- maven-assembly-plugin:3.4.2:single (make-assembly) @ rest-se-bootstrap-examples ---
[INFO] Building jar: D:\hantsylabs\jakartaee10-sandbox\rest-se-bootstrap\target\rest-se-bootstrap-examples-jar-with-dependencies.jar
...
>java -jar .\target\rest-se-bootstrap-examples-jar-with-dependencies.jar

...
org.jboss.weld.environment.undertow.UndertowContainer: WELD-ENV-001302: Undertow detected, CDI injection will be available in Servlets, Filters and Listeners.
2022-11-26 13:44:37,430 DEBUG [ForkJoinPool.commonPool-worker-1] com.example.Main: Instance org.jboss.resteasy.core.se.ResteasySeInstance@56cb9a0d running at http://localhost:8080/ [Native handle: dev.resteasy.embedded.server.UndertowCdiEmbeddedServer@80a8d12].%n
2022-11-26 13:44:37,431 DEBUG [ForkJoinPool.commonPool-worker-1] com.example.Main: Send SIGKILL to shutdown.
```

### Testing REST Endpoint

With Bootstrap API, it is easy to start and stop the application in JUnit lifecycle hooks.

```java
@Slf4j
public class SeBootstrapTest {
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

// tests
}
```

Add a test to verify the functionality of `GreetingResource`.

```java
@Slf4j
public class SeBootstrapTest {

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .executor(executorService)
            .version(HttpClient.Version.HTTP_2)
            .build();

    // @BeforeEach and @AfterEach...

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
```

Here we use Java 11 built-in HttClient to shake hands with the `/api/greeting` endpoint.

Execute the following command to run tests.

```bash
>mvn clean test
...
2022-11-26 16:45:57,721 DEBUG [main] com.example.SeBootstrapTest: greetingUri: http://localhost:8080/api/greeting?name=Hantsy
2022-11-26 16:45:58,103 DEBUG [ForkJoinPool.commonPool-worker-1] com.example.SeBootstrapTest: Greeting: Say 'Hello' to Hantsy at 2022-11-26T16:45:58.022505600
2022-11-26 16:45:58,211 INFO  [ForkJoinPool.commonPool-worker-1] org.jboss.weld.environment.se.WeldContainer: WELD-ENV-002001: Weld SE
container e388f80d-f026-41cb-999d-6f2ed757a1b5 shut down
2022-11-26 16:45:58,213 DEBUG [ForkJoinPool.commonPool-worker-1] com.example.SeBootstrapTest: Stop result: org.glassfish.jersey.server.internal.RuntimeDelegateImpl$1$1@2df99c23 [Native stop result: null]
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.694 s - in com.example.SeBootstrapTest
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  15.225 s
[INFO] Finished at: 2022-11-26T16:45:58+08:00
[INFO] ------------------------------------------------------------------------
```
