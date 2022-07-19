package com.example;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Singleton
@Startup
public class TodoSamples {
    private static final Logger LOGGER = Logger.getLogger(TodoSamples.class.getName());
    @Inject
    TodoService todoService;

    @PostConstruct
    public void init() {
        var todos = Stream.of("What's new in JPA 3.1?", "What's new in Jaxrs 3.1", "Learn new features in Faces 4.0")
                .map(Todo::new)
                .map(it -> todoService.create(it))
                .toList();
        LOGGER.log(Level.INFO, "initialized todo samples: {0}", todos);
    }
}
