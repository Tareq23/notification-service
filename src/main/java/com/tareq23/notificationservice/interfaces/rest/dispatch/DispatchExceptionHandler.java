package com.tareq23.notificationservice.interfaces.rest.dispatch;

import com.tareq23.notificationservice.domain.dispatch.port.NotificationSendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;


@RestControllerAdvice(basePackages = "com.notification.interfaces.rest.dispatch")
public class DispatchExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(DispatchExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail onValidationError(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Validation Failed");
        problem.setType(URI.create("https://api.notification.com/errors/validation-failed"));
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail onIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Bad Request");
        problem.setType(URI.create("https://api.notification.com/errors/bad-request"));
        return problem;
    }

    @ExceptionHandler(NotificationSendException.class)
    public ProblemDetail onSendError(NotificationSendException ex) {
        log.error("Provider send error: provider={} retryable={} message={}",
                ex.providerName(), ex.isRetryable(), ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY,
                "Provider error from " + ex.providerName() + ": " + ex.getMessage());
        problem.setTitle("Provider Send Failed");
        problem.setProperty("retryable", ex.isRetryable());
        problem.setType(URI.create("https://api.notification.com/errors/provider-error"));
        return problem;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail onIllegalState(IllegalStateException ex) {
        log.error("Aggregate invariant violation: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Conflict");
        problem.setType(URI.create("https://api.notification.com/errors/conflict"));
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail onUnexpected(Exception ex) {
        log.error("Unexpected error in dispatch handler", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("https://api.notification.com/errors/internal"));
        return problem;
    }
}
