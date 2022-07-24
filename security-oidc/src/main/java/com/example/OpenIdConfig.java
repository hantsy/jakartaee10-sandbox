package com.example;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class OpenIdConfig {
    private static final Logger LOGGER = Logger.getLogger(OpenIdConfig.class.getName());

    private String domain;
    private String clientId;
    private String clientSecret;

    @PostConstruct
    void init() {
        LOGGER.config("OpenIdConfig.init()");
        try {
            var properties = new Properties();
            properties.load(getClass().getResourceAsStream("/openid.properties"));
            domain = properties.getProperty("domain");
            clientId = properties.getProperty("clientId");
            clientSecret = properties.getProperty("clientSecret");
            LOGGER.log(
                    Level.INFO,
                    "domain: {0}, clientId: {1}, clientSecret:{2}",
                    new Object[]{
                            domain,
                            clientId,
                            clientSecret
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
}
