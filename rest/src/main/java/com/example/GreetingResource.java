package com.example;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;

@Path("greeting")
@RequestScoped
public class GreetingResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response sayHello() {
        var person = new GreetingRecord("Hantsy", LocalDateTime.now());
        return Response.ok(person).build();
    }
}
