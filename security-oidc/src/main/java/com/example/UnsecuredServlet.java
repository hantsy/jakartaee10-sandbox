package com.example;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/Unsecured")
public class UnsecuredServlet extends HttpServlet {
    private static final long serialVersionUID = -417476984908088827L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, IOException {
        response.setContentType("text/html");
        response.getWriter().println("<h1>Unsecured Servlet</h1>");
    }
}

