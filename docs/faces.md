# Jakarta Faces

Jakarta Faces, formerly Jakarta Server Faces and JavaServer Faces (JSF) is a Java specification for building component-based user interfaces for web applications, including UI components, state management, event handing, input validation, page navigation, and support for internationalization and accessibility.

Jakarta Faces 4.0 includes a lot of small improvements and better alignment with CDI and other specifications.

BalusC's [What's new in Faces 4.0](https://balusc.omnifaces.org/2021/11/whats-new-in-faces-40.html) provides a comprehensive guide for those want to get know the detailed changes since Faces 3.0.

## Cleaning up Deprecated Stuff

### Namespace and Naming Changes

In Jakarta EE 8.0, we have accomplished the changes of the maven artifactId from `javaee` to `jakarta`.

In Jakarta EE 9.0, the main work is applying new **jakarta** namespace in the code level. Almost all packages use `jakarta` instead of `javax` in Java source codes, and XML namespace use `jakarta.ee` to replace `jcp.org` in the XML schema definition. But there is an exception, Faces 3.0 still use the legacy `jcp.org` in the Faceslet views.

In Faces 4.0, it introduces the shorter [URN(Uniform Resource Name)](https://en.wikipedia.org/wiki/Uniform_Resource_Name)s to replace the original URIs.

| declaration                 | Jakarta EE 8/9 URIs                             | Jakarta EE 10 URNs                 |
| --------------------------- | ----------------------------------------------- | ---------------------------------- |
| xmlns:faces                 | <http://xmlns.jcp.org/jsf>                      | jakarta.faces                      |
| xmlns:ui                    | <http://xmlns.jcp.org/jsf/facelets>             | jakarta.faces.facelets             |
| xmlns:f                     | <http://xmlns.jcp.org/jsf/core>                 | jakarta.faces.core                 |
| xmlns:h                     | <http://xmlns.jcp.org/jsf/html>                 | jakarta.faces.html                 |
| xmlns:p                     | <http://xmlns.jcp.org/jsf/passthrough>          | jakarta.faces.passthrough          |
| xmlns:cc                    | <http://xmlns.jcp.org/jsf/composite>            | jakarta.faces.composite            |
| xmlns:myCompositeComponents | <http://xmlns.jcp.org/jsf/composite/components> | jakarta.faces.composite/components |
| xmlns:myComponents          | <http://xmlns.jcp.org/jsf/component>            | jakarta.faces.component            |
| xmlns:c                     | <http://xmlns.jcp.org/jsp/jstl/core>            | jakarta.tags.core                  |
| xmlns:fn                    | <http://xmlns.jcp.org/jsp/jstl/function>        | jakarta.tags.functions             |

Additionally, all `JSF` related naming are updated to use `Faces`, including(a copy from BalusC's blog):

* `jsf.js` JavaScript file was renamed to `faces.js`
* `window.jsf` JavaScript global variable was renamed to `window.faces`
* `jsf/ClientSideSecretKey` JNDI variable was renamed to `faces/ClientSideSecretKey`
* `jsf/FlashSecretKey` JNDI variable was renamed to `faces/FlashSecretKey`
* `xmlns:jsf` default XML namespace prefix for passthrough elements was renamed to `xmlns:faces`
* `ResourceHandler.JSF_SCRIPT_LIBRARY_NAME` constant was renamed to `FACES_SCRIPT_LIBRARY_NAME`
* `ResourceHandler.JSF_SCRIPT_RESOURCE_NAME` constant was renamed to `FACES_SCRIPT_RESOURCE_NAME`

The specification name is also updated to **Jakarta Faces** in Faces 4.0 which was named **Jakarta Server Faces** in Jakarta EE 8/9, and  **Java Server Faces**(aka JSF) in Java EE 8 and previous versions.

> In the post, I used word `JSF` for the legacy versions before 3.0.

### Removal of JSP as View

JSP is a standalone specification for building Java web applications, it mainly acts as the **view** role in the traditional MVC frameworks. Nowadays there are still a lot of applications that use it to build web UIs. JSP is a view technology in the early JSF 1.x.

Since JSF 2.0, Facelets is the default view language. In Faces 4.0, it is physically removed.

### Better CDI Alignment

Since JSF 2.2, new CDI compatible scopes eg. `FlowScope` and `ViewScope` are added, CDI was suggested to replace the JSF built-in injection provider. In Faces 4.0, you have to use CDI specific `ApplicationScoped`, `SessionScoped`, `RequestScoped` annotations to replace the legacy ones in old JSF.

### Removal of MethodBinding and ValueBinding

Faces 4.0 removes the legacy `MethodBinding` and `ValueBinding` API which will confuse developers when devloping components, and switch to use Jakarta EL as built-in expressing language engine.

### Removal of Deprecated APIs

Some long-existed deprecated APIs are removed in Faces 4.0, including:

* `PreJsf2ExceptionHandlerFactory` class was just completely removed as that never proved to be useful
* The constant `CURRENT_COMPONENT` and `CURRENT_COMPOSITE_COMPONENT` was removed from `UIComponent` class, use new `UIComponent.getCurrentComponent` instead
* `StateManager` was removed.
* `ResourceResolver` was removed and replaced with `ResourceHandler`

## New Features and Improvements

Next let's explore the changes in Faces 4.0.

### Extensionless Mapping

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
