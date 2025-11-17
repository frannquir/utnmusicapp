package com.musicspring.app.music_app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicspring.app.music_app.exception.ErrorDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        ErrorDetails errorDetails = ErrorDetails.from(
                "Access Denied: You do not have the required permissions to access this resource.",
                request.getRequestURI()
        );

        response.setStatus(HttpStatus.FORBIDDEN.value()); // 403
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }
}