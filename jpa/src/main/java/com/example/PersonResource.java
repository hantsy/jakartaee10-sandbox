package com.example;


import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Path("/persons")
public class PersonResource {

    @Inject
    PersonRepository personRepository;

    @Path("")
    @GET
    public Response allPersons() {
        var data = personRepository.getAllPersons();
        return Response.ok(data).build();
    }
}
