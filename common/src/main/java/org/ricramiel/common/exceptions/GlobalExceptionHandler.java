package org.ricramiel.common.exceptions;

import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import org.ricramiel.common.exceptions.status_code_exceptions.*;
import org.springframework.security.authorization.AuthorizationDeniedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.common.util.EnvUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final EnvUtil envUtil;

    @ExceptionHandler(CreditAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleCreditAlreadyExistsException(CreditAlreadyExistsException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
    //Custom exceptions
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(ForbiddenException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ErrorResponse> handleInternalServerErrorException(InternalServerErrorException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //Feign exceptions
    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<ErrorResponse> handleFeignNotFoundException(FeignException.NotFound ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FeignException.BadRequest.class)
    public ResponseEntity<ErrorResponse> handleFeignBadRequestException(FeignException.BadRequest ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FeignException.Forbidden.class)
    public ResponseEntity<ErrorResponse> handleFeignBadRequestException(FeignException.Forbidden ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(FeignException.Unauthorized.class)
    public ResponseEntity<ErrorResponse> handleFeignUnauthorizedException(FeignException.Unauthorized ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(FeignException.InternalServerError.class)
    public ResponseEntity<ErrorResponse> handleFeignInternalServerErrorException(FeignException.InternalServerError ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    //Db exceptions
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleInternalServerErrorException(EntityNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    //Validation exceptions
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError ->
                        "Invalid argument: " + fieldError.getField() + " " + fieldError.getDefaultMessage())
                .toList();

        return new ResponseEntity<>(new ErrorResponse(errors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return new ResponseEntity<>(new
                ErrorResponse(
                "Invalid input: " + ex.getName()
                        + " should be of type "
                        + Objects.requireNonNull(ex.getRequiredType()).getSimpleName()
        ), HttpStatus.BAD_REQUEST);
    }

    //Auth exceptions
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        return new ResponseEntity<>(new
                ErrorResponse("Access denied"), HttpStatus.FORBIDDEN);
    }

    //Any other exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAnyOtherException(Exception ex, WebRequest request) {
        logErrorWithContext(ex, request);
        return new ResponseEntity<>(buildErrorResponse(ex, "Server error"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void logErrorWithContext(Exception ex, WebRequest request) {
        String requestPath = ((ServletWebRequest) request).getRequest().getRequestURI();
        String requestMethod = ((ServletWebRequest) request).getRequest().getMethod();

        log.error("""
                === UNHANDLED EXCEPTION ===
                Method: {} {}
                Stack Trace:""", requestMethod, requestPath, ex);
    }

    private Object buildErrorResponse(Exception ex, String prodMessage) {
        if (envUtil.isProduction()) {
            return new ErrorResponse(prodMessage);
        } else {
            return new DevelopmentServerErrorResponse(
                    prodMessage,
                    ex.getMessage(),
                    getStackTrace(ex)
            );
        }
    }

    public static List<String> getStackTrace(Exception ex) {
        return Arrays.stream(ex.getStackTrace()).map(
                el -> "at " + el.toString()
        ).collect(Collectors.toList());
    }
}