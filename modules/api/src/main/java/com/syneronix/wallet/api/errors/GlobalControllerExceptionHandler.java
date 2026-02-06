package com.syneronix.wallet.api.errors;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalControllerExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request
    ) {
        logApi(ex, false);
        return buildResponse(HttpStatus.BAD_REQUEST, ex, request);
    }

    @ExceptionHandler(RequestTamperingException.class)
    public ResponseEntity<ErrorResponse> handleTampering(
            RequestTamperingException ex,
            HttpServletRequest request
    ) {
        logApi(ex, true);
        return buildResponse(HttpStatus.valueOf(ex.getStatus()), ex, request);
    }

    @ExceptionHandler(PreviousRequestFailedException.class)
    public ResponseEntity<ErrorResponse> handlePreviousFailure(
            PreviousRequestFailedException ex,
            HttpServletRequest request
    ) {
        logApi(ex, false);
        return buildResponse(HttpStatus.valueOf(ex.getStatus()), ex, request);
    }

    @ExceptionHandler({NotFoundException.class, WalletNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(
            BaseApiExceptionModel ex,
            HttpServletRequest request
    ) {
        logApi(ex, false);
        return buildResponse(HttpStatus.NOT_FOUND, ex, request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException ex,
            HttpServletRequest request
    ) {
        logApi(ex, true);
        return buildResponse(HttpStatus.CONFLICT, ex, request);
    }

    @ExceptionHandler(CurrencyMismatchException.class)
    public ResponseEntity<ErrorResponse> handleCurrencyMismatch(
            CurrencyMismatchException ex,
            HttpServletRequest request
    ) {
        logApi(ex, false);
        return buildResponse(HttpStatus.BAD_REQUEST, ex, request);
    }

    @ExceptionHandler(WalletLockedException.class)
    public ResponseEntity<ErrorResponse> handleWalletLocked(
            WalletLockedException ex,
            HttpServletRequest request
    ) {
        logApi(ex, false);
        return buildResponse(HttpStatus.LOCKED, ex, request);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(
            RateLimitExceededException ex,
            HttpServletRequest request
    ) {
        logApi(ex, false);
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, ex, request);
    }

    @ExceptionHandler(TransactionFailedException.class)
    public ResponseEntity<ErrorResponse> handleTransactionFailed(
            TransactionFailedException ex,
            HttpServletRequest request
    ) {
        logApi(ex, false);
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex, request);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<BadRequestErrorModel> handleValidation(BindException exception) {
        List<BadRequestErrorModel.ValidationError> validationErrors = exception.getBindingResult()
                .getAllErrors().stream()
                .map(error -> new BadRequestErrorModel.ValidationError(getField(error), error.getDefaultMessage())).toList();

        BadRequestErrorModel errorModel = new BadRequestErrorModel("One or more validation errors occurred", validationErrors);
        log.warn("Validation error: {}", errorModel);
        return new ResponseEntity<>(errorModel, null, HttpStatus.BAD_REQUEST);
    }

    private static String getField(ObjectError error) {
        String[] codes = error.getCodes();
        String field = "<unknown_field>";
        if (codes != null && codes.length > 0) {
            String code = codes[0];
            String[] codeParts = code.split("\\.");
            field = codeParts[codeParts.length - 1];
        }
        return field;
    }


    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ErrorResponse> handleInternalError(
            InternalServerErrorException ex,
            HttpServletRequest request
    ) {
        log.error("Internal server error", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse(
                        "INTERNAL_SERVER_ERROR",
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Unexpected server error",
                        HtmlUtils.htmlEscape(request.getRequestURI())
                ));
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            BaseApiExceptionModel ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse(ex, HtmlUtils.htmlEscape(request.getRequestURI())));
    }

    private void logApi(BaseApiExceptionModel ex, boolean isError) {

        String errorMessage = "API Error [%s %s]: %s".formatted(ex.getStatus(), ex.getError(), ex.getMessage());
        if (isError) {
            log.error(errorMessage);
        } else {
            log.warn(errorMessage);
        }

    }
}
