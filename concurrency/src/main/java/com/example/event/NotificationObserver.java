package com.example.event;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;

import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class NotificationObserver {
    private static final Logger LOGGER = Logger.getLogger(NotificationObserver.class.getName());
    @Inject
    TodoCompletedHandler todoCompletedHandler;

    public void onTodoCompletedEvent(@ObservesAsync TodoCompleted event) {
        LOGGER.log(Level.INFO, "observes TodoCompleted event:{0}", event);
        todoCompletedHandler.handleTodoCompletedEvent(event);
    }
}
