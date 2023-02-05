package com.example;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

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