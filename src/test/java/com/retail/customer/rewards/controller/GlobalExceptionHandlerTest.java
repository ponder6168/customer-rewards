package com.retail.customer.rewards.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Method;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    @Test
    void constraintViolationException_isHandledAndReturnsErrorsArray() throws Exception {
        ConstraintViolation<?> v1 = mockConstraintViolation("field1", "must not be null");
        ConstraintViolation<?> v2 = mockConstraintViolation("field2", "must be a valid email");
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(v1, v2));
        MockMvc mvc = mvcFor(ex);

        mvc.perform(get("/throw"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").value(org.hamcrest.Matchers.containsInAnyOrder(
                        "field1: must not be null",
                        "field2: must be a valid email"
                )));
    }

    @Test
    void methodArgumentNotValidException_isHandledAndReturnsFieldErrors() throws Exception {
        MethodParameter methodParameter = createDummyMethodParameter();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "orderRequest");
        bindingResult.addError(new FieldError("orderRequest", "customerId", "must not be blank"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        MockMvc mvc = mvcFor(ex);

        mvc.perform(get("/throw"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value("customerId: must not be blank"));
    }
    @Test
    void methodArgumentNotValidException_typeMismatchDate_returnsInvalidDateMessage() throws Exception {
        MethodParameter methodParameter = createDummyMethodParameter();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "orderRequest");
        FieldError fe = new FieldError("orderRequest", "orderDate", "01-02-2020", false, new String[]{"typeMismatch"}, null, "Invalid date");
        bindingResult.addError(fe);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        MockMvc mvc = mvcFor(ex);

        mvc.perform(get("/throw"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value("orderDate: Invalid date format or invalid date '01-02-2020'. Expected format yyyy-MM-dd"));
    }


    @Test
    void methodArgumentNotValidException_nullCode_doesNotThrowAndReturnsDefaultMessage() throws Exception {
        MethodParameter methodParameter = createDummyMethodParameter();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "orderRequest");
        // include a null entry in the codes array to exercise the c != null check
        FieldError fe = new FieldError("orderRequest", "orderDate", null, false, new String[]{null, "otherCode"}, null, "Invalid date");
        bindingResult.addError(fe);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        MockMvc mvc = mvcFor(ex);

        mvc.perform(get("/throw"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value("orderDate: Invalid date"));
    }
    @Test
    void methodArgumentNotValidException_nullRejectedValue_typeMismatch_returnsNullInMessage() throws Exception {
        MethodParameter methodParameter = createDummyMethodParameter();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "orderRequest");
        FieldError fe = new FieldError("orderRequest", "orderDate", null, false, new String[]{"typeMismatch"}, null, "Invalid date");
        bindingResult.addError(fe);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        MockMvc mvc = mvcFor(ex);

        mvc.perform(get("/throw"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value("orderDate: Invalid date format or invalid date 'null'. Expected format yyyy-MM-dd"));
    }

    @Test
    void handlerMethodValidationException_withConstraintViolationCause_returnsErrorsFromViolations() throws Exception {
        ConstraintViolation<?> v1 = mockConstraintViolation("param", "must not be blank");
        ConstraintViolationException cve = new ConstraintViolationException(Set.of(v1));

        org.springframework.web.method.annotation.HandlerMethodValidationException ex =
                mock(org.springframework.web.method.annotation.HandlerMethodValidationException.class);
        when(ex.getCause()).thenReturn(cve);

        MockMvc mvc = mvcFor(ex);

        mvc.perform(get("/throw"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value("param: must not be blank"));
    }

    @Test
    void handlerMethodValidationException_withoutConstraintViolationCause_returnsMessage() throws Exception {
        org.springframework.web.method.annotation.HandlerMethodValidationException ex =
                mock(org.springframework.web.method.annotation.HandlerMethodValidationException.class);
        when(ex.getCause()).thenReturn(new RuntimeException("inner"));
        when(ex.getMessage()).thenReturn("validation failed at method level");

        MockMvc mvc = mvcFor(ex);

        mvc.perform(get("/throw"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value("validation failed at method level"));
    }

    @Test
    void handlerMethodValidationException_withoutMessage_returnsExceptionClassSimpleName() throws Exception {
        org.springframework.web.method.annotation.HandlerMethodValidationException ex =
                mock(org.springframework.web.method.annotation.HandlerMethodValidationException.class);
        when(ex.getCause()).thenReturn(new RuntimeException("inner"));
        when(ex.getMessage()).thenReturn(null); // explicit: exercise the null-message branch

        String expected = "HandlerMethodValidationException";

        MockMvc mvc = mvcFor(ex);

        mvc.perform(get("/throw"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value(expected));
    }

    @Test
    void genericException_returnsInternalServerError_withExceptionMessage() throws Exception {
        Exception ex = new Exception("boom");
        MockMvc mvc = mvcFor(ex);

        mvc.perform(get("/throw"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value("boom"));
    }

    @Test
    void genericException_withoutMessage_returnsExceptionClassSimpleName() throws Exception {
        Exception ex = new Exception(); // message is null
        MockMvc mvc = mvcFor(ex);

        mvc.perform(get("/throw"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value("Exception"));
    }

    // helper to build a MockMvc instance for a given exception by injecting an instance-based controller
    private MockMvc mvcFor(Exception ex) {
        return MockMvcBuilders
                .standaloneSetup(new TestController(ex))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ConstraintViolation<?> mockConstraintViolation(String propertyPath, String message) {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn(propertyPath);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn(message);
        return violation;
    }

    private MethodParameter createDummyMethodParameter() throws NoSuchMethodException {
        Method m = TestController.class.getMethod("dummyMethod", String.class);
        return new MethodParameter(m, 0);
    }

    /**
     * Instance-based test controller implemented as a record to remove boilerplate.
     * Annotated with @RestController so standalone MockMvc will register request mappings.
     */
    @RestController
    public record TestController(Exception toThrow) {
        // declare throws Exception so we can rethrow checked exceptions (e.g. MethodArgumentNotValidException)
        @GetMapping("/throw")
        public void thrower() throws Exception {
            throw toThrow;
        }

        // dummy method used only to create a MethodParameter for MethodArgumentNotValidException
        public void dummyMethod(String ignored) {
            // no-op
        }
    }
}