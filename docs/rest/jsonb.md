# Customizing Jsonb

In Jakarta REST 3.1, it is possible to customize Jsonb to tune the serialization and deserialization of the HTTP messages.

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

In the config, we apply `UPPER_CAMEL_CASE` strategy on the property name, and format the output result to make it more readable, and filter out the null nodes in JSON.

Let's create a simple Jakarta REST resource for test purpose.

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

Build and run the application on GlassFish or WildFly, and use `curl` to access the `/greeting` endpoint.

```bash
curl http://localhost:8080/rest-examples/api/greeting
{
    "Name": "Hantsy",
    "SentAt": "2022-12-04T15:06:10.2230204"
}
```

Let's create a simple Arquillian test to verify this functionality.

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

Run the test against the previous GlassFish managed adapter that defined in the `arq-glassfish-managed` Maven profile.

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

As you see, it works as expected.
