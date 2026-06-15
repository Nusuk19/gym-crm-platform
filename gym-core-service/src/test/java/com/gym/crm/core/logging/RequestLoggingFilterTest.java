package com.gym.crm.core.logging;

import com.gym.crm.core.logging.RequestLoggingFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

        verify(chain).doFilter(any(ContentCachingRequestWrapper.class), eq(response));
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
    void getRequestBody_whenContentIsEmpty_shouldReturnEmptyString() throws Exception {
        ContentCachingRequestWrapper request = new ContentCachingRequestWrapper(new MockHttpServletRequest());

        String actual = invokeGetRequestBody(request);

        assertEquals("", actual);
    }

    @Test
    void maskSensitiveData_shouldMaskPasswordField() throws Exception {
        String body = "{\"username\":\"john.doe\",\"password\":\"secret123\"}";

        String actual = invokeMaskSensitiveData(body);

        assertEquals("{\"username\":\"john.doe\",\"password\":\"***\"}", actual);
    }

    @Test
    void maskSensitiveData_shouldMaskOldAndNewPassword() throws Exception {
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

        String actual = invokeMaskSensitiveData(body);

        assertEquals(expected, actual);
    }

    @Test
    void maskSensitiveData_whenNull_shouldReturnEmptyString() throws Exception {
        assertEquals("", invokeMaskSensitiveData(null));
    }

    @Test
    void maskSensitiveData_whenBlank_shouldReturnEmptyString() throws Exception {
        assertEquals("", invokeMaskSensitiveData("   "));
    }

    @Test
    void maskSensitiveData_shouldNotMaskNonPasswordFields() throws Exception {
        String body = "{\"username\":\"john.doe\",\"firstName\":\"John\"}";

        String actual = invokeMaskSensitiveData(body);

        assertEquals(body, actual);
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

    private String invokeGetRequestBody(ContentCachingRequestWrapper request) throws Exception {
        Method method = RequestLoggingFilter.class.getDeclaredMethod("getRequestBody", ContentCachingRequestWrapper.class);
        method.setAccessible(true);

        return (String) method.invoke(filter, request);
    }

    private String invokeMaskSensitiveData(String body) throws Exception {
        Method method = RequestLoggingFilter.class.getDeclaredMethod("maskSensitiveData", String.class);
        method.setAccessible(true);

        return (String) method.invoke(filter, body);
    }
}
