# Migrating to Jakarta EE 10

Jakarta EE 10 was released finally on September 2022, it is a big update since Eclipse Foundation hands over the development work from Oracle.

Let's have a look at what's new in Jakarta EE 10.

## What's New In Jakarta EE 10

Jakarta EE 9/9.1 cleans up the new Jakarta namespace in the source codes, and Jakarta EE 10 updates a collection of specifictions to align with the new runtime requirement. Jakarta EE 10 APIs requires Java 11 as the minimal and also supports Java 17 at runtime.

CDI is the kernel of Jakarta EE ecosystem, Faces, REST, Concurreny etc. add more alignments to the new CDI.

Faces finally removes its own dependency injection solution, and use CDI instead. And JSP(Jakarta Pages) is not a view option of Faces.

The long-awaited Multipart mediatype support is available in the new REST spec.

Security 3.0 introduces OpenIDConnect protocol support.

Cocurrency adds a new annotaion `@Asynchronous` to repalce the existing one in the EJB sepcifiction.

CDI itself introduces a new functionality - CDI Lite, which provides build time compatible extensions.

There is no updates in EJB and SOAP related specifications. If you are new a project, you should avoid to use them. They will be depcrated and removed finally.

To align with MicroProfile and satisfy the increasing requirements of Microservices, Jakarta EE 10 introduces a new profile: **Core Profile**, which includes the following specfictions.

* RESTful Web Services 3.1
* JSON Processing 2.1
* JSON Binding 3.0
* Annotations 2.1
* Inteceptors 2.1
* Dependency Injection 2.0
* CDI Lite 4.0
