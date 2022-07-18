package com.example;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Response;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("async")
@Stateless
public class AsyncResource {
    private static final Logger LOGGER = Logger.getLogger(AsyncResource.class.getName());

    @Resource
    private ManagedExecutorService executor;

    @GET
    public void getAsync(final @Suspended AsyncResponse res) {
        res.setTimeoutHandler(
                (ar) -> {
                    ar.resume(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                            .entity("Operation timed out --- please try again.").build());
                }
        );
        res.setTimeout(1000, TimeUnit.MILLISECONDS);
        executor.submit(() -> {
            //do long run operations.
            try {
                LOGGER.log(Level.INFO, " execute long run task in AsyncResource");
                //Thread.sleep(new Random().nextInt(1005));
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                LOGGER.log(Level.SEVERE, "error :" + ex.getMessage());
            }
            res.resume(Response.ok("asynchronous resource").build());
        });
    }

}
