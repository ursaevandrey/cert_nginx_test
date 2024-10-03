package com.example.app;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ResponseUtils {

    public static void setResponseError(HttpServletResponse response, int status, String message)
            throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(new Response(message)));
    }

    public static void setBadRequestError(HttpServletResponse response, String message)
            throws IOException {
        setResponseError(response, HttpServletResponse.SC_BAD_REQUEST, message);
    }

}
