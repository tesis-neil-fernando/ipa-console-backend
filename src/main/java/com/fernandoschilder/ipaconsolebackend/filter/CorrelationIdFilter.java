package com.fernandoschilder.ipaconsolebackend.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that ensures each request has a correlation id. The id is added to the response
 * as the `X-Correlation-Id` header and also pushed into the SLF4J MDC under key `correlationId`.
 */
@Component
public class CorrelationIdFilter extends HttpFilter {

    public static final String HEADER_NAME = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String cid = request.getHeader(HEADER_NAME);
        boolean generated = false;
        if (cid == null || cid.isBlank()) {
            cid = UUID.randomUUID().toString();
            generated = true;
        }

        MDC.put(MDC_KEY, cid);
        // ensure header is present in response
        response.setHeader(HEADER_NAME, cid);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
            // no need to remove header from response
        }
    }
}
