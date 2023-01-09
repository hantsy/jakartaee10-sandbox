# Summary

The new OIDC client support is a great addon to the existing security spec, unfortunately the original plans of Digest and Client-Cert authentications are not available in this release.

For my opinion, `OpenIdAuthenticationMechanismDefinition` is not a good naming, I would like use `OidcClient` or `OpenIdConnectClient` instead of the word `OpenId`, because `OpenId` itself is really a deprecated protocol by Google. But the newer `OpenIdConnect` related facilities are just addons on the top of existing OAuth2 protocol.

Additionally, I am eager there is a fluent API provided to assemble security configuration(authentication and authorization) through producing standard CDI beans instead of the annotations, like the Spring Security configuration. Especially, when RESTful API is becoming more and more popular, path pattern based security constraints is easier than the annotations applied on classes or methods.
