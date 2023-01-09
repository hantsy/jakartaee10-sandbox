# What's New in Jakarta Security 3

In additional to the existing BASIC, FORM, and CUSTOM FORM authentication, Jakarta Security 3.0 adds OpenID Connect protocol support. But the original plan of support Client Cert and Digest is not available in this release, more details please read [Arjan Tijms's What's New In Jakarta Security 3](https://arjan-tijms.omnifaces.org/2022/04/whats-new-in-jakarta-security-3.html).

OpenID Connect(aka OIDC) 1.0 is a simple identity layer on top of the OAuth 2.0 protocol. It enables Clients to verify the identity of the End-User based on the authentication performed by an Authorization Server, as well as to obtain basic profile information about the End-User in an interoperable and REST-like manner.

Next we will explore how to configure OIDC client authentication in a Jakarta web application with the popular OIDC/OAuth2 authentication providers, such as self-host [KeyCloak](https://www.keycloak.org/) server, and cloud based identity providers, eg. [Okta](https://www.okta.com/) and [Auth0](https://auth0.com/).
