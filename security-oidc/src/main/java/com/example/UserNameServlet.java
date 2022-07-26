package com.example;

import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/username")
@ServletSecurity(@HttpConstraint(rolesAllowed = {"foo", "bar"}))
public class UserNameServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(UserNameServlet.class.getName());

    @Inject
    SecurityContext securityContext;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //String name = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "";
        var principal = securityContext.getCallerPrincipal();
        LOGGER.log(Level.INFO, "Principal: {0}", principal);
        var name = principal.getName();
        response.getWriter().print(name);
    }
}