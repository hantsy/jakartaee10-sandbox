package com.example;

import jakarta.annotation.security.DeclareRoles;
import jakarta.inject.Inject;
import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import jakarta.security.enterprise.identitystore.openid.OpenIdContext;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/")
@OpenIdAuthenticationMechanismDefinition(
        providerURI = "https://dev-due1wofp.us.auth0.com/",
        clientId = "uXNJPxVo1XgtMpZ8K97lvdEUUAFaa85u",
        clientSecret = "PAqXSI5jh26T7QjB7lYyD9KKKv_nNrtB35s3F1iUTYfQ1VVBoyptXIA-99lCD-GN",
        redirectURI = "${baseURL}/callback")
@DeclareRoles("all")
@ServletSecurity(@HttpConstraint(rolesAllowed = "all"))
public class HomeServlet extends HttpServlet {

    @Inject
    private OpenIdContext context;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.getWriter().println("<h1>Secured Servlet</h1>");
        response.getWriter().println("<p>access token:" + context.getAccessToken() + "</p>");
        response.getWriter().println("<p>token type:" + context.getTokenType() + "</p>");
        response.getWriter().println("<p>subject:" + context.getSubject() + "</p>");
        response.getWriter().println("<p>expires in:" + context.getExpiresIn() + "</p>");
        response.getWriter().println("<p>refresh token:" + context.getRefreshToken() + "</p>");
        response.getWriter().println("<p>claims json:" + context.getClaimsJson() + "</p>");
    }
}

