



# New Features and Improvements

Next let's explore the changes in Faces 4.0.

## Extensionless Mapping

In the previous versions, `FacesServlet` is registered to handle mapping of *&lt;conext path>/myFacelets.xhtml*, *&lt;context path>/faces/myFacelets*, *&lt;context path>/myFacelets.faces*.

In Faces 4.0, it allows to map a URI without extension, eg. *&lt;conext path>/myFacelets*.

[Create a Jakarta EE web project](./jpa/jakartaee.md), add the following configuration in the *src/webapp/web.xml* file.

```xml
<context-param>
    <param-name>jakarta.faces.AUTOMATIC_EXTENSIONLESS_MAPPING</param-name>
    <param-value>true</param-value>
</context-param>
```

Add a simple CDI bean to activate Faces.

```java
@FacesConfig()
@ApplicationScoped
public class FacesCdiActivator {
}
```

> Note, the version attribute in annotation `@FacesConfig` is deprecated since Faces 4.0.

Create a Facelets view *src/webapp/hello.xhtml*.

```xml
<!DOCTYPE html>
<html lang="en"
      xmlns="http://www.w3.org/1999/xhtml"
      xmlns:f="jakarta.faces.core"
      xmlns:jsf="jakarta.faces"
      xmlns:h="jakarta.faces.html">
<f:view>
    <h:head>
        <title>Hello, Faces 4.0!</title>
    </h:head>
    <h:body>
        <h1>Say Hello to Faces 4.0</h1>
        <h:form prependId="false">
            <label jsf:for="name" jsf:required="true">Enter your name:</label>
            <input type="text"
                   jsf:id="name"
                   jsf:value="#{hello.name}"
                   jsf:required="true"
                   jsf:requiredMessage="Name is required."
                   placeholder="Type your name here..."
            />
            <h:message for="name"/>
            <br/>
            <input type="submit" jsf:id="submit" value="Say Hello"  jsf:action="#{hello.createMessage()}">
                <f:ajax execute="@form" render="@form"/>
            </input>
            <br/>
            <p id="message">#{hello.message}</p>
        </h:form>
    </h:body>
</f:view>
</html>
```

And create a backing bean to process the submission.

```java
@Named
@RequestScoped
public class Hello {
    private String name;
    private String message;

    public Hello() {
    }

    public void createMessage() {
        message = "Hello, " + name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }
}
```

Now execute the following command to run the applicaion on GlassFish.

```bash
> mvn clean package cargo:run
...
[INFO] Building war: D:\hantsylabs\jakartaee10-sandbox\faces\target\faces-examples.war
[INFO]
[INFO] --- cargo-maven3-plugin:1.10.4:run (default-cli) @ faces-examples ---
[INFO] [en3.ContainerRunMojo] Resolved container artifact org.codehaus.cargo:cargo-core-container-glassfish:jar:1.10.4 for container glassfish7x
[INFO] [talledLocalContainer] Parsed GlassFish version = [7.0.1]
[INFO] [talledLocalContainer] GlassFish 7.0.1 starting...
[INFO] [talledLocalContainer] Attempting to start cargo-domain.... Please look at the server log for more details.....
[INFO] [talledLocalContainer] GlassFish 7.0.1 started on port [8080]
[INFO] Press Ctrl-C to stop the container...
```

Open a web browser and navigate to <http://localhost:8080/faces-examples/hello>.

> NOTE: Here we do not append any extension on the URL.

![Faces hello](./faces-hello.png)

Input anything in the text input field and click the **Say Hello** button. You will see a greeting message displayed as the above image.

## Writing Facelets in Java
