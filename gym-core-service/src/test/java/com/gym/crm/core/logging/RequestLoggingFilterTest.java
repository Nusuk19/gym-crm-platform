package com.gym.crm.core.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    @Test
    void doFilterInternal_shouldContinueFilterChain() throws ServletException, IOException {
        MockHttpServletRequest request = buildRequest("GET", "/api/v1/trainees/John.Doe", null);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(any(ContentCachingRequestWrapper.class), any(ContentCachingResponseWrapper.class));
    }

    @Test
    void doFilterInternal_whenEmptyBody_shouldNotThrow() {
        MockHttpServletRequest request = buildRequest("GET", "/api/v1/trainees/John.Doe", null);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        assertThatCode(() -> filter.doFilterInternal(request, response, chain)).doesNotThrowAnyException();
    }

    @Test
    void doFilterInternal_whenRequestHasJsonBody_shouldNotThrow() {
        MockHttpServletRequest request = buildRequest("POST", "/api/v1/auth/login",
                "{\"username\":\"john.doe\",\"password\":\"password123\"}");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        assertDoesNotThrow(() -> filter.doFilterInternal(request, response, chain));
    }

    @Test
    void doFilterInternal_shouldFlushResponseBodyToClient() throws ServletException, IOException {
        MockHttpServletRequest request = buildRequest("GET", "/api/v1/trainees", null);
        MockHttpServletResponse response = new MockHttpServletResponse();
        String responseBody = "{\"id\":1}";

        FilterChain chain = (req, res) -> {
            res.getWriter().write(responseBody);
            res.getWriter().flush();
        };

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getContentAsString()).isEqualTo(responseBody);
    }

    @Test
    void doFilterInternal_whenResponseIs4xx_shouldNotThrow() {
        MockHttpServletRequest request = buildRequest("PUT", "/api/v1/trainees", null);
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(400);
        FilterChain chain = mock(FilterChain.class);

        assertThatCode(() -> filter.doFilterInternal(request, response, chain)).doesNotThrowAnyException();
    }

    @Test
    void doFilterInternal_whenResponseIs5xx_shouldNotThrow() {
        MockHttpServletRequest request = buildRequest("PUT", "/api/v1/trainees", null);
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(500);
        FilterChain chain = mock(FilterChain.class);

        assertThatCode(() -> filter.doFilterInternal(request, response, chain)).doesNotThrowAnyException();
    }

    @Test
    void maskSensitiveData_shouldMaskPasswordField() {
        String body = "{\"username\":\"john.doe\",\"password\":\"secret123\"}";
        String actual = filter.maskSensitiveData(body);
        assertEquals("{\"username\":\"john.doe\",\"password\":\"***\"}", actual);
    }

    @Test
    void maskSensitiveData_shouldMaskOldAndNewPassword() {
        String body = """
                {
                  "username": "john.doe",
                  "oldPassword": "oldSecret",
                  "newPassword": "newSecret"
                }
                """;
        String expected = """
                {
                  "username": "john.doe",
                  "oldPassword": "***",
                  "newPassword": "***"
                }
                """;
        assertEquals(expected, filter.maskSensitiveData(body));
    }

    @Test
    void maskSensitiveData_whenNull_shouldReturnEmptyString() {
        assertEquals("", filter.maskSensitiveData(null));
    }

    @Test
    void maskSensitiveData_whenBlank_shouldReturnEmptyString() {
        assertEquals("", filter.maskSensitiveData("   "));
    }

    @Test
    void maskSensitiveData_shouldNotMaskNonPasswordFields() {
        String body = "{\"username\":\"john.doe\",\"firstName\":\"John\"}";
        assertEquals(body, filter.maskSensitiveData(body));
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
