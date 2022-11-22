# Jakarta REST 3.1

Jakarta REST includes a dozen of small improvements, and also introduces two major new features.

* Java SE Bootstrap API
* The long-awaited standard Multipart API

Let's explore the features by examples.

## Bootstrap API

Like the CDI Bootstrap API to host a CDI container in Java SE environment, Jakarta REST Bootstrap API provides similar API to serve a Jaxrs application on embedded servers.

Let's create a simple Java SE application to experience it.

Follow the steps in the [Jakarta Persistence - Example: Hibernate 6.1](./jpa/hibernate.md) to setup a Java SE project.

We use sl4j/logback as logging framework, and also Lombok annotations to simplify the source codes.

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

Create *src/main/resources/logback.xml*.

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

The `@Slf4j` is from Lombok, which will add a `org.slf4j.Logger` declaration at compile time.

To customize the bootstrap parameters, you can use `SeBootstrap.Configuration.builder()` to setup the embedded server properties.

The `SeBootstrap.start` accepts a Rest `Application` entry class and configuration, in `thenAccept` block, a Bootstrap server instance is available to consume. The `instance.stopOnShutdown` is used to setup a shutdown hook, then print the application startup information.

The `.toCompletableFuture().join()` will wait async execution is completed.

Let's have a look at `RestConfig`.

```java
@ApplicationPath("/api")
public class RestConfig extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(GreetingResource.class);
    }
}
```

Create a simple Jaxrs Resource - `GreetingResource`.

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

You have to create an empty CDI *bean.xml*, put it into *src/main/resources/META-INFO*.

```xml
<beans xmlns="https://jakarta.ee/xml/ns/jakartaee"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/beans_4_0.xsd"
  bean-discovery-mode="annotated" version="4.0">
</beans>
```

To run the application, we should the runtime embedded server. Both Jersey and Resteasy provides several options.

### Jersey

Add the following dependencies into the project *pom.xml*, we use a standard Maven profile to category them.

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

Now you can run `Main` in IDE directly by click the run button.

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

Now open a terminal from system, and use `curl` command to verify your resource endpoint.

```bash
curl http://localhost:8080/api/greeting?name=Hantsy

Say 'Hello' to Hantsy at 2022-11-22T22:45:41.129167100
```
