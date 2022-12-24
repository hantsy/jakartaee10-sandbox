package com.example;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@OpenIdAuthenticationMechanismDefinition(
        providerURI = "${oidcConfig.issuerUri}",
        clientId = "${oidcConfig.clientId}",
        clientSecret = "${oidcConfig.clientSecret}",
        redirectURI = "${baseURL}/callback",
        // redirectToOriginalResource = true
//        providerMetadata = @OpenIdProviderMetadata(
//                //issuer = "${openIdConfig.issuerUri}",
//                jwksURI = "https://${openIdConfig.domain}/.well-known/jwks.json"
//        ),
        jwksReadTimeout = 5000
)
@ApplicationScoped
@Named("oidcConfig")
public class OidcConfig {
    private static final Logger LOGGER = Logger.getLogger(OidcConfig.class.getName());

    private String domain;
    private String clientId;
    private String clientSecret;
    private String issuerUri;

    @PostConstruct
    void init() {
        LOGGER.config("OidcConfig.init()");
        try {
            var properties = new Properties();
            properties.load(getClass().getResourceAsStream("/oidc.properties"));
            domain = properties.getProperty("domain");
            clientId = properties.getProperty("clientId");
            clientSecret = properties.getProperty("clientSecret");
            issuerUri = properties.getProperty("issuerUri");

            if (issuerUri == null && domain != null) {
                issuerUri = "https://" + this.domain + "/";
            }

            LOGGER.log(
                    Level.INFO,
                    "domain: {0}, clientId: {1}, clientSecret:{2}, issuerUri: {3}",
                    new Object[] {
                            domain,
                            clientId,
                            clientSecret,
                            issuerUri
                    });
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load oidc.properties from classpath", e);
        }
    }

    public String getDomain() {
        return domain;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getIssuerUri() {
        return issuerUri;
    }
}
