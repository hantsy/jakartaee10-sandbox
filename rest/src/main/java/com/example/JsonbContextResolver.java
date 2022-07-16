package com.example;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.config.PropertyNamingStrategy;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JsonbContextResolver implements ContextResolver<Jsonb> {
    @Override
    public Jsonb getContext(Class<?> type) {
        JsonbConfig config = new JsonbConfig()
                .withPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE_WITH_UNDERSCORES)
                .withNullValues(false);
        return JsonbBuilder.newBuilder().
                withConfig(config).
                build();
    }
}
