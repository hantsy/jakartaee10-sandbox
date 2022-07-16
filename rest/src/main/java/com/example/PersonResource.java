package com.example;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("person")
@RequestScoped
public class PersonResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPerson() {
        var person = new Person("Hantsy", 40);
        return Response.ok(person).build();
    }
}
