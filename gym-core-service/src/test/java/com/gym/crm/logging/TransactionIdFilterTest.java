package com.gym.crm.logging;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static com.gym.crm.logging.TransactionIdFilter.TRANSACTION_ID_HEADER;
import static com.gym.crm.logging.TransactionIdFilter.TRANSACTION_ID_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TransactionIdFilterTest {

    private final TransactionIdFilter filter = new TransactionIdFilter();

    @AfterEach
    void cleanMdc() {
        MDC.clear();
    }

    @Test
    void doFilterInternal_whenNoIncomingHeader_shouldGenerateTransactionId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        String header = response.getHeader(TRANSACTION_ID_HEADER);
        assertThat(header).isNotNull().isNotBlank();
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenIncomingHeaderPresent_shouldReuseIt() throws Exception {
        String existingId = "upstream-tx-abc-123";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TRANSACTION_ID_HEADER, existingId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getHeader(TRANSACTION_ID_HEADER)).isEqualTo(existingId);
    }

    @Test
    void doFilterInternal_shouldClearMdcAfterCompletion() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertThat(MDC.get(TRANSACTION_ID_KEY)).isNull();
    }

    @Test
    void doFilterInternal_shouldClearMdcEvenOnException() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        org.mockito.Mockito.doThrow(new RuntimeException("chain error")).when(chain).doFilter(request, response);

        try {
            filter.doFilterInternal(request, response, chain);
        } catch (RuntimeException ignored) {
        }

        assertThat(MDC.get(TRANSACTION_ID_KEY)).isNull();
    }

    @Test
    void doFilterInternal_shouldGenerateUniqueTransactionIdPerRequest() throws Exception {
        MockHttpServletResponse response1 = new MockHttpServletResponse();
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(new MockHttpServletRequest(), response1, chain);
        filter.doFilterInternal(new MockHttpServletRequest(), response2, chain);

        String id1 = response1.getHeader(TRANSACTION_ID_HEADER);
        String id2 = response2.getHeader(TRANSACTION_ID_HEADER);

        assertThat(id1).isNotEqualTo(id2);
    }
}