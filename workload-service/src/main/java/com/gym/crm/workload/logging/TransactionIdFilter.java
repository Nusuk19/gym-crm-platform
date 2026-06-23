package com.gym.crm.workload.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class TransactionIdFilter extends OncePerRequestFilter {

    public static final String TRANSACTION_ID_KEY = "transactionId";
    public static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String transactionId = resolveTransactionId(request);

        bindTransactionId(response, transactionId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRANSACTION_ID_KEY);
        }
    }

    private void bindTransactionId(HttpServletResponse response, String transactionId) {
        MDC.put(TRANSACTION_ID_KEY, transactionId);
        response.setHeader(TRANSACTION_ID_HEADER, transactionId);
    }

    private String resolveTransactionId(HttpServletRequest request) {
        String incoming = request.getHeader(TRANSACTION_ID_HEADER);
        return isValidTransactionId(incoming)
                ? incoming
                : UUID.randomUUID().toString();
    }

    private boolean isValidTransactionId(String value) {
        return value != null && !value.isBlank();
    }
}
