package com.gym.crm.workload.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    @Test
    void doFilterInternal_shouldContinueFilterChain() throws ServletException, IOException {
        MockHttpServletRequest request = buildRequest("PUT", "/trainer-workloads", null);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(any(ContentCachingRequestWrapper.class), any(HttpServletResponse.class));
    }

    @Test
    void doFilterInternal_whenEmptyBody_shouldNotThrow() {
        MockHttpServletRequest request = buildRequest("GET", "/trainer-workloads/status", null);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        assertThatCode(() -> filter.doFilterInternal(request, response, chain)).doesNotThrowAnyException();
    }

    @Test
    void doFilterInternal_whenRequestHasJsonBody_shouldNotThrow() {
        MockHttpServletRequest request = buildRequest("PUT", "/trainer-workloads",
                "{\"trainerUsername\":\"john.doe\",\"actionType\":\"ADD\"}");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        assertThatCode(() -> filter.doFilterInternal(request, response, chain)).doesNotThrowAnyException();
    }

    @Test
    void doFilterInternal_shouldFlushResponseBodyToClient() throws ServletException, IOException {
        MockHttpServletRequest request = buildRequest("GET", "/trainer-workloads/status", null);
        MockHttpServletResponse response = new MockHttpServletResponse();
        String responseBody = "{\"status\":\"ok\"}";

        FilterChain chain = (req, res) -> {
            res.getWriter().write(responseBody);
            res.getWriter().flush();
        };

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getContentAsString()).isEqualTo(responseBody);
    }

    @Test
    void doFilterInternal_whenResponseIs4xx_shouldNotThrow() throws ServletException, IOException {
        MockHttpServletRequest request = buildRequest("PUT", "/trainer-workloads", null);
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(400);
        FilterChain chain = mock(FilterChain.class);

        assertThatCode(() -> filter.doFilterInternal(request, response, chain)).doesNotThrowAnyException();
    }

    @Test
    void doFilterInternal_whenResponseIs5xx_shouldNotThrow() throws ServletException, IOException {
        MockHttpServletRequest request = buildRequest("PUT", "/trainer-workloads", null);
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(500);
        FilterChain chain = mock(FilterChain.class);

        assertThatCode(() -> filter.doFilterInternal(request, response, chain)).doesNotThrowAnyException();
    }

    private MockHttpServletRequest buildRequest(String method, String uri, String body) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(method);
        request.setRequestURI(uri);

        if (body != null) {
            request.setContent(body.getBytes(StandardCharsets.UTF_8));
            request.setContentType("application/json");
        }

        return request;
    }
}
