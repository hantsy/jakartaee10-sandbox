package com.example;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
@Named("openIdConfig")
public class Auth0OpenIdConfig {
    private static final Logger LOGGER = Logger.getLogger(Auth0OpenIdConfig.class.getName());

    private String domain;
    private String clientId;
    private String clientSecret;
    private String issuerUri;

    @PostConstruct
    void init() {
        LOGGER.config("OpenIdConfig.init()");
        try {
            var properties = new Properties();
            properties.load(getClass().getResourceAsStream("/openid.properties"));
            domain = properties.getProperty("domain");
            clientId = properties.getProperty("clientId");
            clientSecret = properties.getProperty("clientSecret");
            issuerUri = "https://" + this.domain + "/";
            LOGGER.log(
                    Level.INFO,
                    "domain: {0}, clientId: {1}, clientSecret:{2}, issuerUri: {3}",
                    new Object[]{
                            domain,
                            clientId,
                            clientSecret,
                            issuerUri
                    }
            );
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load openid.properties", e);
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
