package io.hhplus.concert.interfaces.api.config.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.concert.domain.support.error.CoreErrorType.WaitingQueue;
import io.hhplus.concert.interfaces.api.common.response.ApiErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class WaitingQueueTokenFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        String tokenValue = request.getHeader("X-Queue-Token");
        if(this.isTokenValid(tokenValue)) {
            filterChain.doFilter(request, response);
        } else {
            this.handleInvalidToken(response);
        }
    }

    private Boolean isTokenValid(String token) {
        return StringUtils.hasText(token);
    }

    private void handleInvalidToken(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiErrorResponse errorResponse = new ApiErrorResponse(WaitingQueue.QUEUE_TOKEN_MISSING.name(),
            WaitingQueue.QUEUE_TOKEN_MISSING.getMessage());
        try {
            String json = new ObjectMapper().writeValueAsString(errorResponse);
            response.getWriter().write(json);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
