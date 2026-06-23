package com.gym.crm.workload.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String LOG_MESSAGE =
            "REST call completed. transactionId={}, method={}, endpoint={}, query={}, requestBody={}, responseStatus={}, duration={}ms";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(wrappedRequest, response);
        } finally {
            logRestCall(wrappedRequest, response, System.currentTimeMillis() - startTime);
        }
    }

    private void logRestCall(ContentCachingRequestWrapper request, HttpServletResponse response, long durationMs) {
        Object[] args = {
                MDC.get(TransactionIdFilter.TRANSACTION_ID_KEY),
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                getRequestBody(request),
                response.getStatus(),
                durationMs
        };

        int status = response.getStatus();
        if (status >= 500) {
            log.error(LOG_MESSAGE, args);
        } else if (status >= 400) {
            log.warn(LOG_MESSAGE, args);
        } else {
            log.info(LOG_MESSAGE, args);
        }
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();

        return content.length == 0 ? "" : maskSensitiveData(new String(content, StandardCharsets.UTF_8));
    }

    String maskSensitiveData(String body) {
        if (body == null || body.isBlank()) return "";

        return body.replaceAll("(\"(?:password|oldPassword|newPassword)\"\\s*:\\s*)\"[^\"]*\"", "$1\"***\"");
    }
}
