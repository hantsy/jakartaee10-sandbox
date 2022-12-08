package com.example.event;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.NotificationOptions;
import jakarta.inject.Inject;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class NotificationSender {
    private static final Logger LOGGER = Logger.getLogger(NotificationSender.class.getName());

    @Inject
    Event<TodoCompleted> todoCompletedEvent;

    @Resource
    ManagedExecutorService executorService;
    AtomicLong sentCounter = new AtomicLong(0L);

    public AtomicLong getSentCounter() {
        return sentCounter;
    }

    public void send(Long index) {
        var event = new TodoCompleted(index);
        LOGGER.log(Level.INFO, "sending event:{0}", new Object[]{event});
        todoCompletedEvent
                .fireAsync(
                        event,
                        NotificationOptions.builder()
                                .setExecutor(executorService)
                                //.set("weld.async.notification.mode", "SERIAL")//SERIAL (default), PARALLEL.
                                .set("weld.async.notification.timeout", 1000)
                                .build()
                )
                .thenAccept(this::afterSent);

    }

    private void afterSent(TodoCompleted event) {
        var count = sentCounter.incrementAndGet();
        LOGGER.log(Level.INFO, "after sent: index:{0}, event: {1}", new Object[]{count, event});
    }
}
