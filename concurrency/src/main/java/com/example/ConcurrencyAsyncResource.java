package com.example;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Response;

import java.util.logging.Level;
import java.util.logging.Logger;

@Path("concurrencyAsync")
public class ConcurrencyAsyncResource {
    private static final Logger LOGGER = Logger.getLogger(ConcurrencyAsyncResource.class.getName());

    @GET
    @jakarta.enterprise.concurrent.Asynchronous
    public void getAsync(final @Suspended AsyncResponse res) {
        //perform long run operations.
        try {
            LOGGER.log(Level.INFO, " execute long run task in EjbAsyncResource");
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "error :" + ex.getMessage());
        }

        res.resume(Response.ok("Concurrency Async resource").build());
    }

}