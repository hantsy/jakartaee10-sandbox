package com.example;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static jakarta.security.enterprise.identitystore.IdentityStore.ValidationType.PROVIDE_GROUPS;

@ApplicationScoped
public class AuthorizationIdentityStore implements IdentityStore {

    private final Map<String, Set<String>> authorization = Map.of("user", Set.of("foo", "bar"));

    @Override
    public Set<ValidationType> validationTypes() {
        return EnumSet.of(PROVIDE_GROUPS);
    }

    @Override
    public Set<String> getCallerGroups(CredentialValidationResult validationResult) {
        return authorization.get(validationResult.getCallerPrincipal().getName());
    }

}