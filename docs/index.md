# Migrating to Jakarta EE 10

Jakarta EE 10 was released finally on September 2022, it is a big update since Eclipse Foundation hands over the development work from Oracle.

Let's have a look at what's new in Jakarta EE 10.

## What's New In Jakarta EE 10

Jakarta EE 9/9.1 cleans up the new Jakarta namespace in the API source codes, and Jakarta EE 10 updates a collection of specifications to align with the new Java runtime requirement. Jakarta EE 10 APIs requires Java 11 as the minimal and also supports Java 17 at runtime.

CDI is the kernel of Jakarta EE ecosystem, Faces, REST, Concurrency etc. add more alignments to the new CDI.

Faces finally removes its own dependency injection solution, and use CDI instead. And JSP(Jakarta Pages) is not a view option of Faces.

The long-awaited Multipart mediatype support is available in the new REST spec.

Security 3.0 introduces OpenIDConnect protocol support.

Concurrency adds a new annotation `@Asynchronous` to replace the existing one in the EJB specification.

CDI itself introduces a new functionality - CDI Lite, which provides build time compatible extensions.

There is no updates in EJB and SOAP Web Services related specifications. If you are starting a new project, you should avoid to use these specifications. They will be deprecated and removed from Jakarta EE finally.

To align with MicroProfile and satisfy the increasing requirements of Microservices architecture, Jakarta EE 10 introduces a new profile: **Core Profile**, which includes the following specifications.

* RESTful Web Services 3.1
* JSON Processing 2.1
* JSON Binding 3.0
* Annotations 2.1
* Interceptors 2.1
* Dependency Injection 2.0
* CDI Lite 4.0

Unfortunately, Cache, NoSQL and MVC are mature, but still miss the Jakarta EE 10 release train.

## The Future of Jakarta EE

Now Jakarta EE is a community-leaded specification, everybody can join and participate into the progress of the definition of the specifications.

There are a few proposals are submitted, for example.

* Jakarta RPC  - standardizes the gRPC in Jakarta EE ecosystem.
* Jakarta Data - introduces a general-purpose `Repository` pattern for SQL database and NoSQL database.
* ...

More info about Jakarta EE, please go to the [official Jakarta EE homepage](https://jakarta.ee).
