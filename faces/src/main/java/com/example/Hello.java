package com.example;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@RequestScoped
public class Hello {
    private static final Logger LOGGER = Logger.getLogger(Hello.class.getName());

    private String name;

    private String message;

    public Hello() {
    }

    public void createMessage() {
        message = "Hello, " + name;
        LOGGER.log(Level.INFO, "set message value:{0}", message);
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