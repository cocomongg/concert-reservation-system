package io.hhplus.concert.config.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Slf4j
public class ApiLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        // Request log
        log.info("[{}: {}] st\nclient: [{}, {}]",
            request.getMethod(),
            request.getRequestURI(),
            request.getRemoteAddr(),
            request.getHeader("User-Agent"));

        long startMillis = System.currentTimeMillis();
        filterChain.doFilter(requestWrapper, responseWrapper);
        long endMillis = System.currentTimeMillis();

        // Response log
        log.info("[{}: {}] ed({}ms\nRequest:[params: {}, body: {}]\nResponse:[status: {}, body: {}]",
            request.getMethod(),
            request.getRequestURI(),
            endMillis - startMillis,
            requestWrapper.getParameterMap(),
            this.parseRequestBody(requestWrapper),
            responseWrapper.getStatus(),
            this.parseResponseBody(responseWrapper)
        );

        responseWrapper.copyBodyToResponse();
    }

    private String parseRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) {
            return "";
        }
        try {
            return new String(content, request.getCharacterEncoding());
        } catch (Exception e) {
            return "parse error";
        }
    }

    private String parseResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length == 0) {
            return "";
        }
        try {
            return new String(content, response.getCharacterEncoding());
        } catch (Exception e) {
            return "parse error";
        }
    }
}