package com.example.event;

import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class TodoCompletedHandler {
    private static final Logger LOGGER = Logger.getLogger(TodoCompletedHandler.class.getName());

    @Asynchronous
    public void handleTodoCompletedEvent(TodoCompleted event) {
        LOGGER.log(Level.INFO, "handling TodoCompleted event:{0}", event);
    }
}
