package com.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/Callback")
public class CallbackServlet extends HttpServlet {

//    @Inject
//    private OpenIdContext context;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //response.getWriter().println(context.getAccessToken());

        String referer = (String) request.getSession().getAttribute("Referer");
        String redirectTo = referer != null ? referer : "/";

        response.sendRedirect(redirectTo);
    }

}