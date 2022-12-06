# Jakarta REST 3.1

Jakarta REST includes a dozen of small improvements, and also introduces two major new features.

* Java SE Bootstrap API
* The long-awaited standard Multipart API

Let's explore these features by examples.

## Bootstrap API

Like the CDI Bootstrap API to host a CDI container in Java SE environment, Jakarta REST Bootstrap API provides similar API to serve a Jaxrs application in embedded servers.

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

The `.toCompletableFuture().join()` will wait asynchronous execution to be completed.

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

Although CDI *beans.xml* is optional in Jakarta EE environment. To start a SeBootstrap instance in a Java SE environment, you have to create an empty CDI *beans.xml*, put it into *src/main/resources/META-INFO*.

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

Open a terminal, and switch to the project root, and run the following command to build the application into a jar archive.


```xml
>mvn clean package -DskipTests -D"maven.test.skip=true"
...
[INFO]
[INFO] --- maven-assembly-plugin:3.4.2:single (make-assembly) @ rest-se-bootstrap-examples ---
[INFO] Building jar: D:\hantsylabs\jakartaee10-sandbox\rest-se-bootstrap\target\rest-se-bootstrap-examples-jar-with-dependencies.jar
...
```

Then run the application using the following command.

```xml
>java -jar .\target\rest-se-bootstrap-examples-jar-with-dependencies.jar
...
WELD-000101: Transactional services not available. Injection of @Inject UserTransaction not available.
Transactional observers will be invoked synchronously.
2022-11-26 13:50:45,132 INFO  [ForkJoinPool.commonPool-worker-1] org.jboss.weld.environment.se.WeldContainer: WELD-ENV-002003: Weld SE container 4744564b-922c-4612-a88f-8095c4d7293b initialized
2022-11-26 13:50:45,257 DEBUG [ForkJoinPool.commonPool-worker-1] com.example.Main: Instance org.glassfish.jersey.server.internal.RuntimeDelegateImpl$1@2fa5468d running at http://localhost:8080/ [Native handle: org.glassfish.jersey.jdkhttp.JdkHttpServer@78879a1c].%n
2022-11-26 13:50:45,257 DEBUG [ForkJoinPool.commonPool-worker-1] com.example.Main: Send SIGKILL to shutdown.
```

Similarly you can use the above `curl` command to verify the `/greeting` endpoint.

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

There are [several Embedded containers](https://docs.jboss.org/resteasy/docs/6.2.1.Final/userguide/html_single/index.html#RESTEasy_Embedded_Container) existed in the latest Resteasy. Here we choose the one that based on Redhat Undertow with CDI support.

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

## Multipart APIs

Both Jersey and Resteasy have their own Multipart implementations. In Jakarta REST 3.1, it finally brings the standardized Multipart APIs support.

Follow the steps in [Jakarta Persistence - Jakarta EE](./jpa/jakartaee.md) and create a simple Jakarta EE project.

Firstly we create a simple Jaxrs resource to consume Multipart request and produce Multipart response.

```java
@Path("multiparts")
@RequestScoped
public class MultipartResource {
    private static final Logger LOGGER = Logger.getLogger(MultipartResource.class.getName());

    java.nio.file.Path uploadedPath;

    @PostConstruct
    public void init() {
        try {
            uploadedPath = Files.createTempDirectory(Paths.get("/temp"), "uploads_");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Path("simple")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@FormParam("name") String name,
                               @FormParam("part") EntityPart part) {
        LOGGER.log(Level.INFO, "name: {0} ", name);
        LOGGER.log(
                Level.INFO,
                "uploading file: {0},{1},{2},{3}",
                new Object[]{
                        part.getMediaType(),
                        part.getName(),
                        part.getFileName(),
                        part.getHeaders()
                }
        );
        try {
            Files.copy(
                    part.getContent(),
                    Paths.get(uploadedPath.toString(), part.getFileName().orElse(generateFileName(UUID.randomUUID().toString(), mediaTypeToFileExtension(part.getMediaType())))),
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Response.ok().build();

    }

    @Path("list")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadMultiFiles(List<EntityPart> parts) {
        LOGGER.log(Level.INFO, "Uploading files: {0}", parts.size());
        parts.forEach(
                part -> {
                    LOGGER.log(
                            Level.INFO,
                            "uploading multifiles: {0},{1},{2},{3}",
                            new Object[]{
                                    part.getMediaType(),
                                    part.getName(),
                                    part.getFileName(),
                                    part.getHeaders()
                            }
                    );
                    try {
                        Files.copy(
                                part.getContent(),
                                Paths.get(uploadedPath.toString(), part.getFileName().orElse(generateFileName(UUID.randomUUID().toString(), mediaTypeToFileExtension(part.getMediaType())))),
                                StandardCopyOption.REPLACE_EXISTING
                        );
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        return Response.ok().build();
    }

    @GET
    public List<EntityPart> getFiles() throws IOException {
        List<EntityPart> parts = new ArrayList<>();
        parts.add(EntityPart.withName("abd")
                .fileName("abc.text").content("this is a text content")
                .mediaType(MediaType.TEXT_PLAIN_TYPE)
                .build()
        );
        try (var files = Files.list(uploadedPath)) {
            var partsInUploaded = files
                    .map(path -> {
                                var file = path.toFile();
                                LOGGER.log(Level.INFO, "found uploaded file: {0}", file.getName());
                                try {
                                    return EntityPart.withName(file.getName())
                                            .fileName(file.getName())
                                            .content(new FileInputStream(file))
                                            .mediaType(fileExtensionToMediaType(getFileExt(file.getName())))
                                            .build();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                    )
                    .toList();
            parts.addAll(partsInUploaded);
        }

        return parts;
    }

    private String getFileExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private MediaType fileExtensionToMediaType(String extension) {
        return switch (extension.toLowerCase()) {
            case "txt" -> MediaType.TEXT_PLAIN_TYPE;
            case "svg" -> MediaType.APPLICATION_SVG_XML_TYPE;
            default -> MediaType.APPLICATION_OCTET_STREAM_TYPE;
        };
    }

    private String generateFileName(String fileName, String extension) {
        return fileName + "." + extension;
    }

    private String mediaTypeToFileExtension(MediaType mediaType) {
        return switch (mediaType.toString()) {
            case "text/plain" -> "txt";
            case "application/svg+xml" -> "svg";
            default -> "bin";
        };
    }
}
```

To handle a `multipart/form-data` request, add `@Consumes(MediaType.MULTIPART_FORM_DATA)` on the Jaxrs resource. A Jaxrs resource can consume a single `EntityPart` or a collection of `EntityPart` .

In the above example codes, the `uploadFile` method demonstrates how to handle a regular form post which includes a simple form value and a `EntityPart`, and `uploadMultiFiles` method is used to process a list of `EntityPart`. The `getFiles` method is used to produce Multipart entities to the client.

Create a REST `Application` to activate Jakarta REST.

```java
@ApplicationPath("api")
public class RestConfig extends Application {
}
```

Run the following to build the project and deploy to GlassFish.

```bash
> mvn clean package cargo:run
```

Or deploy to WildFly if you prefer WildFly.

```bash
> mvn clean wildfly:run
```

Open a terminal, and let's test our endpoints with `curl` command.

```bash
> curl -i -X POST  http://localhost:8080/rest-examples/api/multiparts/simple -F "name=Hantsy" -F "part=@D:\temp\test.txt" -H "Content-Type: multipart/form-data"
HTTP/1.1 200 OK
Server: Eclipse GlassFish  7.0.0
```

In the GlassFish server.log, it appends the following new info.

```bash
[2022-12-03T20:52:36.002626+08:00] [GlassFish 7.0] [INFO] [] [com.example.MultipartResource] [tid: _ThreadID=61 _ThreadName=http-listener-1(1)] [levelValue: 800] [[
  name: Hantsy ]]

[2022-12-03T20:52:36.003632+08:00] [GlassFish 7.0] [INFO] [] [com.example.MultipartResource] [tid: _ThreadID=61 _ThreadName=http-listener-1(1)] [levelValue: 800] [[
  uploading file: text/plain,part,Optional[test.txt],{Content-Disposition=[form-data; name="part"; filename="test.txt"], Content-Type=[text/plain]}]]
```

Let's try to upload multi files at the same time using `/list` endpoints.

```bash
> curl -i -X POST  http://localhost:8080/rest-examples/api/multiparts/list -F "test=@D:\temp\test.txt" -F "test2=@D:\temp\test2.txt" -H "Content-Type: multipart/form-data"
HTTP/1.1 200 OK
Server: Eclipse GlassFish  7.0.0
```

In the GlassFish server.log file, we got the following new log.

```bash
[2022-12-03T20:58:54.140995+08:00] [GlassFish 7.0] [INFO] [] [com.example.MultipartResource] [tid: _ThreadID=65 _ThreadName=http-listener-1(5)] [levelValue: 800] [[
  Uploading files: 2]]

[2022-12-03T20:58:54.142996+08:00] [GlassFish 7.0] [INFO] [] [com.example.MultipartResource] [tid: _ThreadID=65 _ThreadName=http-listener-1(5)] [levelValue: 800] [[
  uploading multifiles: text/plain,test,Optional[test.txt],{Content-Disposition=[form-data; name="test"; filename="test.txt"], Content-Type=[text/plain]}]]

[2022-12-03T20:58:54.145995+08:00] [GlassFish 7.0] [INFO] [] [com.example.MultipartResource] [tid: _ThreadID=65 _ThreadName=http-listener-1(5)] [levelValue: 800] [[
  uploading multifiles: text/plain,test2,Optional[test2.txt],{Content-Disposition=[form-data; name="test2"; filename="test2.txt"], Content-Type=[text/plain]}]]
```

Let's try to send a `GET` request to fetch the multipart data.

```bash
>curl -v http://localhost:8080/rest-examples/api/multiparts -H "Accept: multipart/form-data"

* Connected to localhost (127.0.0.1) port 8080 (#0)
> GET /rest-examples/api/multiparts HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.83.1
> Accept: multipart/form-data
>
* Mark bundle as not supporting multiuse
< HTTP/1.1 200 OK
< Server: Eclipse GlassFish  7.0.0
< X-Powered-By: Servlet/6.0 JSP/3.1(Eclipse GlassFish  7.0.0  Java/Oracle Corporation/17)
< MIME-Version: 1.0
< Content-Type: multipart/form-data;boundary=Boundary_1_1941664842_1670072479638
< Content-Length: 197
<
--Boundary_1_1941664842_1670072479638
Content-Type: text/plain
Content-Disposition: form-data; filename="abc.text"; name="abd"

this is a text content
--Boundary_1_1941664842_1670072479638--
```

Jaxrs Client also includes API to build Multipart and read Multipart entities.

Let's create an Arquillian test to verify the above endpoints.

```java
@ExtendWith(ArquillianExtension.class)
public class MultipartResourceTest {

    private final static Logger LOGGER = Logger.getLogger(MultipartResourceTest.class.getName());

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
                .addClasses(MultipartResource.class, RestConfig.class)
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
        client.register(MultiPartFeature.class);
    }

    @AfterEach
    public void after() throws Exception {
        client.close();
    }

    @Test
    @RunAsClient
    public void testUploadSingleFile() throws Exception {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/multiparts/simple"));
        var part = EntityPart.withName("part").fileName("test.txt")
                .content(this.getClass().getResourceAsStream("/test.txt"))
                .mediaType(MediaType.TEXT_PLAIN_TYPE)
                .build();
        var name = EntityPart.withName( "name").content("test").build();
        var genericEntity = new GenericEntity<List<EntityPart>>(List.of(name, part)) {};
        var entity = Entity.entity(genericEntity, MediaType.MULTIPART_FORM_DATA);
        Response r = target.request(MediaType.MULTIPART_FORM_DATA).post(entity);
        LOGGER.log(Level.INFO, "response status: {0}", r.getStatus());
        assertEquals(200, r.getStatus());
    }

    @Test
    @RunAsClient
    public void testUploadMultiFiles() throws Exception {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/multiparts/list"));
        List<EntityPart> parts = List.of(
                EntityPart.withName("textFile").fileName("test.txt")
                        .content(this.getClass().getResourceAsStream("/test.txt"))
                        .mediaType(MediaType.TEXT_PLAIN_TYPE)
                        .build(),
                EntityPart.withName("imageFile").fileName("test.svg")
                        .content(this.getClass().getResourceAsStream("/test.svg"))
                        .mediaType(MediaType.APPLICATION_SVG_XML_TYPE)
                        .build()
        );
        var genericEntity = new GenericEntity<List<EntityPart>>(parts) {};
        var entity = Entity.entity(genericEntity, MediaType.MULTIPART_FORM_DATA);
        Response r = target.request().post(entity);
        assertEquals(200, r.getStatus());
        LOGGER.log(Level.INFO, "Upload multiple files response status: {0}", r.getStatus());
    }

    @Test
    @RunAsClient
    public void testGetFiles() {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/multiparts"));
        Response response = target.request().accept(MediaType.MULTIPART_FORM_DATA).get();

        assertEquals(200, response.getStatus());
        LOGGER.log(Level.INFO, "GetFiles response status: {0}", response.getStatus());
        List<EntityPart> parts = response.readEntity(new GenericType<List<EntityPart>>() {});
        parts.forEach(part -> LOGGER.log(
                Level.INFO,
                "Get file: {0},{1},{2},{3}",
                new Object[]{
                        part.getMediaType(),
                        part.getName(),
                        part.getFileName(),
                        part.getHeaders()
                }
        ));
    }
}
```

In this test, we have 3 methods to verify the functionality of uploading a single file, uploading a collection of files, and reading the files from server side.

Run the following command to execute the test.

```bash
>  mvn clean verify -Parq-glassfish-managed -D"it.test=MultipartResourceTest"
...

[INFO] --- maven-failsafe-plugin:3.0.0-M7:integration-test (integration-test) @ rest-examples ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO]
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.example.it.MultipartResourceTest
Starting database using command: [java, -jar, D:\hantsylabs\jakartaee10-sandbox\rest\target\glassfish7\glassfish\modules\admin-cli.jar, start-database, -t]
Starting database in the background.
Log redirected to D:\hantsylabs\jakartaee10-sandbox\rest\target\glassfish7\glassfish\databases\derby.log.
Starting container using command: [java, -jar, D:\hantsylabs\jakartaee10-sandbox\rest\target\glassfish7\glassfish\modules\admin-cli.jar, start-domain, -t]
Attempting to start domain1.... Please look at the server log for more details.....
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Dec 03, 2022 9:21:19 PM com.example.it.MultipartResourceTest createDeployment
INFO: war deployment: 3ca3304d-1523-4473-bd4d-a7074b2ae48b.war:
/WEB-INF/
/WEB-INF/lib/
/WEB-INF/lib/assertj-core-3.23.1.jar
/WEB-INF/lib/byte-buddy-1.12.10.jar
/WEB-INF/classes/
/WEB-INF/classes/com/
/WEB-INF/classes/com/example/
/WEB-INF/classes/com/example/MultipartResource.class
/WEB-INF/classes/com/example/RestConfig.class
/WEB-INF/beans.xml
Dec 03, 2022 9:21:28 PM com.example.it.MultipartResourceTest before
INFO: baseURL: http://localhost:8080/3ca3304d-1523-4473-bd4d-a7074b2ae48b/
Dec 03, 2022 9:21:28 PM com.example.it.MultipartResourceTest testUploadSingleFile
INFO: response status: 200
Dec 03, 2022 9:21:28 PM com.example.it.MultipartResourceTest before
INFO: baseURL: http://localhost:8080/3ca3304d-1523-4473-bd4d-a7074b2ae48b/
Dec 03, 2022 9:21:28 PM com.example.it.MultipartResourceTest testGetFiles
INFO: GetFiles response status: 200
Dec 03, 2022 9:21:28 PM com.example.it.MultipartResourceTest lambda$testGetFiles$0
INFO: Get file: text/plain,abd,Optional[abc.text],{Content-Type=[text/plain], Content-Disposition=[form-data; filename="abc.text"; name="abd"]}
Dec 03, 2022 9:21:28 PM com.example.it.MultipartResourceTest lambda$testGetFiles$0
INFO: Get file: text/plain,test.txt,Optional[test.txt],{Content-Type=[text/plain], Content-Disposition=[form-data; filename="test.txt"; name="test.txt"]}
Dec 03, 2022 9:21:28 PM com.example.it.MultipartResourceTest before
INFO: baseURL: http://localhost:8080/3ca3304d-1523-4473-bd4d-a7074b2ae48b/
Dec 03, 2022 9:21:28 PM com.example.it.MultipartResourceTest testUploadMultiFiles
INFO: Upload multiple files response status: 200
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 74.084 s - in com.example.it.MultipartResourceTest
Stopping container using command: [java, -jar, D:\hantsylabs\jakartaee10-sandbox\rest\target\glassfish7\glassfish\modules\admin-cli.jar, stop-domain, --kill, -t]
Stopping database using command: [java, -jar, D:\hantsylabs\jakartaee10-sandbox\rest\target\glassfish7\glassfish\modules\admin-cli.jar, stop-database, -t]
Sat Dec 03 21:21:33 CST 2022 : Connection obtained for host: 0.0.0.0, port number 1527.
Sat Dec 03 21:21:33 CST 2022 : Apache Derby Network Server - 10.15.2.0 - (1873585) shutdown
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO]
[INFO] --- maven-failsafe-plugin:3.0.0-M7:verify (integration-test) @ rest-examples ---
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  01:44 min
[INFO] Finished at: 2022-12-03T21:21:34+08:00
[INFO] ------------------------------------------------------------------------
```

## Customizing Jsonb

In Jaxrs 3.1, you can customize Jsonb if it uses Jsonb to serialize and deserialize the HTTP messages.

```java
@Provider
public class JsonbContextResolver implements ContextResolver<Jsonb> {
    @Override
    public Jsonb getContext(Class<?> type) {
        JsonbConfig config = new JsonbConfig()
                .withPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE)
                .withFormatting(true)
                .withNullValues(false);
        return JsonbBuilder.newBuilder().withConfig(config).build();
    }
}
```

In the config, we apply `UPPER_CAMEL_CASE` strategy on the property name, and format the output result, and filter out the null nodes in JSON.

Create a simple resource class for test purpose.

```java
@Path("greeting")
@RequestScoped
public class GreetingResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response sayHello() {
        var person = new GreetingRecord("Hantsy", LocalDateTime.now());
        return Response.ok(person).build();
    }
}

// GreetingRecord
public record GreetingRecord(String name, LocalDateTime sentAt){}
```

Build and run the application, and use `curl` to access the `/greeting` endpoint.

```bash
curl http://localhost:8080/rest-examples/api/greeting
{
    "Name": "Hantsy",
    "SentAt": "2022-12-04T15:06:10.2230204"
}
```

Let's create an Arquillian test to verify it.

```java
@ExtendWith(ArquillianExtension.class)
public class GreetingResourceTest {

    private final static Logger LOGGER = Logger.getLogger(GreetingResourceTest.class.getName());

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
                .addClasses(GreetingResource.class, GreetingRecord.class, JsonbContextResolver.class, RestConfig.class)
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
        //client.register(JsonbContextResolver.class);
    }

    @AfterEach
    public void after() throws Exception {
        client.close();
    }

    @Test
    @RunAsClient
    public void testGetPerson() throws Exception {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/greeting"));
        Response r = target.request().accept(MediaType.APPLICATION_JSON_TYPE).get();
        LOGGER.log(Level.INFO, "Get greeting response status: {0}", r.getStatus());
        assertEquals(200, r.getStatus());
        String jsonString = r.readEntity(String.class);
        LOGGER.log(Level.INFO, "Get greeting result string: {0}", jsonString);
        assertThat(jsonString).doesNotContain("email");
        assertThat(jsonString).contains("Name");
    }

}
```

Run the test against the previous GlassFish managed adapter.

```bash
> mvn clean verify -Parq-glassfish-managed -D"it.test=GreetingResourceTest"
...
[INFO] --- maven-failsafe-plugin:3.0.0-M7:integration-test (integration-test) @ rest-examples ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO]
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.example.it.GreetingResourceTest
Starting database using command: [java, -jar, D:\hantsylabs\jakartaee10-sandbox\rest\target\glassfish7\glassfish\modules\admin-cli.jar, start-database, -t]
Starting database in the background.
Log redirected to D:\hantsylabs\jakartaee10-sandbox\rest\target\glassfish7\glassfish\databases\derby.log.
Starting container using command: [java, -jar, D:\hantsylabs\jakartaee10-sandbox\rest\target\glassfish7\glassfish\modules\admin-cli.jar, start-domain, -t]
Attempting to start domain1.... Please look at the server log for more details.....
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Dec 04, 2022 3:12:58 PM com.example.it.GreetingResourceTest createDeployment
INFO: war deployment: d8b12d32-3bac-4092-b30b-612bb887509f.war:
/WEB-INF/
/WEB-INF/lib/
/WEB-INF/lib/assertj-core-3.23.1.jar
/WEB-INF/lib/byte-buddy-1.12.10.jar
/WEB-INF/classes/
/WEB-INF/classes/com/
/WEB-INF/classes/com/example/
/WEB-INF/classes/com/example/GreetingResource.class
/WEB-INF/classes/com/example/GreetingRecord.class
/WEB-INF/classes/com/example/JsonbContextResolver.class
/WEB-INF/classes/com/example/RestConfig.class
/WEB-INF/beans.xml
Dec 04, 2022 3:13:08 PM com.example.it.GreetingResourceTest before
INFO: baseURL: http://localhost:8080/d8b12d32-3bac-4092-b30b-612bb887509f/
Dec 04, 2022 3:13:08 PM com.example.it.GreetingResourceTest testGetPerson
INFO: Get greeting response status: 200
Dec 04, 2022 3:13:08 PM com.example.it.GreetingResourceTest testGetPerson
INFO: Get greeting result string: {
    "Name": "Hantsy",
    "SentAt": "2022-12-04T15:13:08.4011684"
}
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 66.233 s - in com.example.it.GreetingResourceTest
Stopping container using command: [java, -jar, D:\hantsylabs\jakartaee10-sandbox\rest\target\glassfish7\glassfish\modules\admin-cli.jar, stop-domain, --kill, -t]
Stopping database using command: [java, -jar, D:\hantsylabs\jakartaee10-sandbox\rest\target\glassfish7\glassfish\modules\admin-cli.jar, stop-database, -t]
Sun Dec 04 15:13:13 CST 2022 : Connection obtained for host: 0.0.0.0, port number 1527.
Sun Dec 04 15:13:14 CST 2022 : Apache Derby Network Server - 10.15.2.0 - (1873585) shutdown
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO]
[INFO] --- maven-failsafe-plugin:3.0.0-M7:verify (integration-test) @ rest-examples ---
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  01:37 min
[INFO] Finished at: 2022-12-04T15:13:14+08:00
[INFO] ------------------------------------------------------------------------
```

## CDI as Injection Provider

In the initial Jaxrs 3.1 proposal, it planned to use CDI as default injection provider to replace the existing one, that means you can use `Inject` to replace Jaxrs specific `Context` to inject a Jaxrs resource. But this is not included in the final version.

Jersey itself provides an extra module to implement this feature.

Let's create a simple TODO application to expose resources at the `/todos` endpoint.

Firstly create a JPA entity - `Todo`.

```java
@Entity
@Table(name = "todos")
public class Todo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    String title;
    boolean completed = false;

    // setters and getters, hashCode and equals
}
```

Create a simple EJB `@Stateless` bean to create a Todo and retrieve todos.

```java
@Stateless
public class TodoService {

    @PersistenceContext
    EntityManager entityManager;

   @Transactional
    public Todo create(Todo data) {
        entityManager.persist(data);
        return data;
    }

    public Todo findById(UUID id) {
        return entityManager.find(Todo.class, id);
    }

    public List<Todo> findAll() {
        return entityManager.createQuery("select t from Todo t", Todo.class).getResultList();
    }
}
```

Create a EJB `@Singleton` bean to initialize some datas.

```java
@Singleton
@Startup
public class TodoSamples {
    private static final Logger LOGGER = Logger.getLogger(TodoSamples.class.getName());
    @Inject
    TodoService todoService;

    @PostConstruct
    public void init() {
        var todos = Stream.of("What's new in JPA 3.1?", "What's new in Jaxrs 3.1", "Learn new features in Faces 4.0")
                .map(Todo::new)
                .map(it -> todoService.create(it))
                .toList();
        LOGGER.log(Level.INFO, "initialized todo samples: {0}", todos);
    }
}
```

Now create Jaxrs resource to expose `/todos` endpoints.

```java
@Path("todos")
@RequestScoped
public class TodoResources {

     @Inject
    //@Context
    ResourceContext resourceContext;

    @Inject
    // @Context
    UriInfo uriInfo;

    @Inject
    TodoService todoService;

    @GET
    public Response getAllTodos() {
        var todos = todoService.findAll();
        return Response.ok(todos).build();
    }

    @POST
    public Response createTodo(Todo todo) throws Exception {
        var saved = todoService.create(todo);
        return Response.created(uriInfo.getBaseUriBuilder().path("todos/{id}").build(saved.getId())).build();
    }

    @GET
    @Path("{id}")
    public TodoResource subResource() {
        return resourceContext.getResource(TodoResource.class);
    }
}


// TodoResource for single resource.
@RequestScoped
public class TodoResource {

    @Inject
    TodoService todoService;

    @PathParam("id")
    UUID id;

    @GET
    public Response getById() {
        var todos = todoService.findById(id);
        return Response.ok(todos).build();
    }

}
```

To make sure it works with Jersey and GlassFish, we have to copy `jersey-cdi-rs-inject` to the GlassFish *GlassFish_installdir/glassfish/modules* folder.

Simply add the following to `glassfish` profile and utilize `maven-dependency-plugin` to download a copy of `jersey-cdi-rs-inject` to the cargo managed GlassFish location.

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <version>${maven-dependency-plugin.version}</version>
    <executions>
        <execution>
            <id>copy</id>
            <phase>process-classes</phase>
            <goals>
                <goal>copy</goal>
            </goals>
            <configuration>
                <artifactItems>
                    <artifactItem>
                        <groupId>org.glassfish.jersey.ext.cdi</groupId>
                        <artifactId>jersey-cdi-rs-inject</artifactId>
                        <version>${jersey.version}</version>
                        <type>jar</type>
                        <overWrite>false</overWrite>
                    </artifactItem>
                </artifactItems>
                <outputDirectory>${project.build.directory}/cargo/installs/glassfish-${glassfish.version}/glassfish7/glassfish/modules</outputDirectory>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Build and run the application, and use `curl` to test our endpoint `/todos`.

```bash
> curl http://localhost:8080/rest-examples/api/todos
[
    {
        "Completed": false,
        "Id": "7db8cb74-6ec6-4b3b-8930-e6a29a9c363a",
        "Title": "Learn new features in Faces 4.0"
    },
    {
        "Completed": false,
        "Id": "0dba2afd-943f-42a2-b1bd-2cd9fea3a140",
        "Title": "What's new in JPA 3.1?"
    },
    {
        "Completed": false,
        "Id": "ff7ac837-fe68-4d47-b79d-f11fd87fd43a",
        "Title": "What's new in Jaxrs 3.1"
    }
]
```

Create an Arquillian test for verify.

```java
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
                        TodoResource.class,
                        TodoResources.class,
                        TodoService.class,
                        Todo.class,
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
        Response r = target.request().accept(MediaType.APPLICATION_JSON_TYPE).get();
        LOGGER.log(Level.INFO, "Get /todos response status: {0}", r.getStatus());
        assertEquals(200, r.getStatus());
        String jsonString = r.readEntity(String.class);
        LOGGER.log(Level.INFO, "Get /todos result string: {0}", jsonString);
    }
}
```

To make test this test can be run successfully on GlassFish, copy `maven-dependency-plugin` to the target GlassFish server.

Add the following to the end of `configuration` section of `dependency-maven-plugin` in the `arq-glassfish-managed` profile.

```xml
<execution>
    <id>copy</id>
    <phase>pre-integration-test</phase>
    <goals>
        <goal>copy</goal>
    </goals>
    <configuration>
        <artifactItems>
            <artifactItem>
                <groupId>org.glassfish.jersey.ext.cdi</groupId>
                <artifactId>jersey-cdi-rs-inject</artifactId>
                <version>${jersey.version}</version>
                <type>jar</type>
                <overWrite>false</overWrite>
            </artifactItem>
        </artifactItems>
        <outputDirectory>${project.build.directory}/glassfish7/glassfish/modules</outputDirectory>
    </configuration>
</execution>
```

Run the following to execute this test.

```bash
> mvn clean verify -Parq-glassfish-managed -D"it.test=TodoResourceTest"
...
[INFO] --- maven-failsafe-plugin:3.0.0-M7:integration-test (integration-test) @ rest-examples ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO]
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.example.it.TodoResourceTest
Starting database using command: [java, -jar, D:\hantsylabs\jakartaee10-sandbox\rest\target\glassfish7\glassfish\modules\admin-cli.jar, start-database, -t]
Starting database in the background.
Log redirected to D:\hantsylabs\jakartaee10-sandbox\rest\target\glassfish7\glassfish\databases\derby.log.
Starting container using command: [java, -jar, D:\hantsylabs\jakartaee10-sandbox\rest\target\glassfish7\glassfish\modules\admin-cli.jar, start-domain, -t]
Attempting to start domain1.... Please look at the server log for more details.....
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Dec 04, 2022 5:09:02 PM com.example.it.TodoResourceTest createDeployment
INFO: war deployment: 4a77aebc-2b83-44e6-9ab0-f93e656e1b2c.war:
/WEB-INF/
/WEB-INF/lib/
/WEB-INF/lib/assertj-core-3.23.1.jar
/WEB-INF/lib/byte-buddy-1.12.10.jar
/WEB-INF/classes/
/WEB-INF/classes/com/
/WEB-INF/classes/com/example/
/WEB-INF/classes/com/example/TodoResource.class
/WEB-INF/classes/com/example/TodoResources.class
/WEB-INF/classes/com/example/TodoService.class
/WEB-INF/classes/com/example/Todo.class
/WEB-INF/classes/com/example/TodoSamples.class
/WEB-INF/classes/com/example/RestConfig.class
/WEB-INF/classes/META-INF/
/WEB-INF/classes/META-INF/persistence.xml
/WEB-INF/beans.xml
Dec 04, 2022 5:09:15 PM com.example.it.TodoResourceTest before
INFO: baseURL: http://localhost:8080/4a77aebc-2b83-44e6-9ab0-f93e656e1b2c/
Dec 04, 2022 5:09:16 PM com.example.it.TodoResourceTest testGetTodos
INFO: Get /todos response status: 200
Dec 04, 2022 5:09:16 PM com.example.it.TodoResourceTest testGetTodos
INFO: Get /todos result string: [{"completed":false,"id":"6686c811-71cb-40aa-a38a-24d775c679ba","title":"Learn new features in Faces 4.0"},{"completed":false,"id":"8efb7123-0c43-46aa-aabc-0777494be620","title":"What's new in JPA 3.1?"},{"completed":false,"id":"0478a20e-b8c1-4577-91e4-cc0362ab14d5","title":"What's new in Jaxrs 3.1"}]
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 78.438 s - in com.example.it.TodoResourceTest
Stopping container using command: [java, -jar, D:\hantsylabs\jakartaee10-sandbox\rest\target\glassfish7\glassfish\modules\admin-cli.jar, stop-domain, --kill, -t]
Stopping database using command: [java, -jar, D:\hantsylabs\jakartaee10-sandbox\rest\target\glassfish7\glassfish\modules\admin-cli.jar, stop-database, -t]
Sun Dec 04 17:09:20 CST 2022 : Connection obtained for host: 0.0.0.0, port number 1527.
Sun Dec 04 17:09:20 CST 2022 : Apache Derby Network Server - 10.15.2.0 - (1873585) shutdown
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO]
[INFO] --- maven-failsafe-plugin:3.0.0-M7:verify (integration-test) @ rest-examples ---
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  01:45 min
[INFO] Finished at: 2022-12-04T17:09:21+08:00
[INFO] ------------------------------------------------------------------------
```

Get the [sample codes](https://github.com/hantsy/jakartaee10-sandbox/blob/master/rest/) from my Github account.
