# Jakarta Faces

Jakarta Faces, formerly Jakarta Server Faces and JavaServer Faces (JSF) is a Java specification for building component-based user interfaces for web applications, including UI components, state management, event handing, input validation, page navigation, and support for internationalization and accessibility.

Jakarta Faces 4.0 includes a lot of small improvements and better alignment with CDI and other specifications.

BalusC's [What's new in Faces 4.0](https://balusc.omnifaces.org/2021/11/whats-new-in-faces-40.html) provides a comprehensive guide if you want to get know the detailed changes since Faces 3.0.

## Cleaning up Deprecated Stuff

### Namespace and Naming Changes

In Jakarta EE 8.0, we accomplished the maven artifactId from `javaee` to `jakarta`.
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
