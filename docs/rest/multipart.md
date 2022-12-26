# Support for Multipart Media Type

Both Jersey and Resteasy have their own Multipart implementations. In Jakarta REST 3.1, it finally brings the standardized Multipart APIs support.

Follow the steps in [Jakarta Persistence - Jakarta EE](../jpa/jakartaee.md) and create a simple Jakarta EE project.

Firstly let's a simple Jakarta REST resource to consume Multipart request and produce Multipart response.

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

To handle a `multipart/form-data` request, add `@Consumes(MediaType.MULTIPART_FORM_DATA)` on the Jaxrs resource methods. A Jaxrs resource can consume a single `EntityPart` or a collection of `EntityPart` .

In the above example codes, the `uploadFile` method demonstrates how to handle a regular form post which includes a simple form value and a `EntityPart`, and `uploadMultiFiles` method is used to process a list of `EntityPart`. The `getFiles` method is used to produce a collection of Multipart entities to the client.

Create a REST `Application` to activate Jakarta REST.

```java
@ApplicationPath("api")
public class RestConfig extends Application {
}
```

Run the following command, it will build the project and package it into a war archive, and then start a GlassFish instance and deploy the war archive to GlassFish.

```bash
> mvn clean package cargo:run
```

Alternatively, run the following command to deploy to WildFly if you prefer WildFly.

```bash
> mvn clean wildfly:run
```

When the deployment work is done, open another terminal, and let's test our endpoints with `curl` command.

```bash
> curl -i -X POST  http://localhost:8080/rest-examples/api/multiparts/simple -F "name=Hantsy" -F "part=@D:\temp\test.txt" -H "Content-Type: multipart/form-data"
HTTP/1.1 200 OK
Server: Eclipse GlassFish  7.0.0
```

Open the GlassFish server.log file, it appends the following new info.

```bash
[2022-12-03T20:52:36.002626+08:00] [GlassFish 7.0] [INFO] [] [com.example.MultipartResource] [tid: _ThreadID=61 _ThreadName=http-listener-1(1)] [levelValue: 800] [[
  name: Hantsy ]]

[2022-12-03T20:52:36.003632+08:00] [GlassFish 7.0] [INFO] [] [com.example.MultipartResource] [tid: _ThreadID=61 _ThreadName=http-listener-1(1)] [levelValue: 800] [[
  uploading file: text/plain,part,Optional[test.txt],{Content-Disposition=[form-data; name="part"; filename="test.txt"], Content-Type=[text/plain]}]]
```

Let's try to upload multiple files using `/list` endpoints.

```bash
> curl -i -X POST  http://localhost:8080/rest-examples/api/multiparts/list -F "test=@D:\temp\test.txt" -F "test2=@D:\temp\test2.txt" -H "Content-Type: multipart/form-data"
HTTP/1.1 200 OK
Server: Eclipse GlassFish  7.0.0
```

In the GlassFish server.log file, the following new log is newly added.

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

Jaxrs also includes Client API to shake hands with the Multipart endpoints. With the Client API, it is easy to upload or download the Multipart entities.

Let's create an Arquillian test and use Jaxrs Client API to verify the above endpoints.

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

In this test, there are three methods to verify the functionality of uploading a single file, uploading a collection of files, and reading the files from server side.

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
