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

    /* ============================================================
       DOMAIN / API
       ============================================================ */

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request
    ) {
        logApiError(ex);
        return buildResponse(HttpStatus.BAD_REQUEST, ex, request);
    }

    @ExceptionHandler(RequestTamperingException.class)
    public ResponseEntity<ErrorResponse> handleTampering(
            RequestTamperingException ex,
            HttpServletRequest request
    ) {
        logApiError(ex);
        return buildResponse(HttpStatus.valueOf(ex.getStatus()), ex, request);
    }

    @ExceptionHandler(PreviousRequestFailedException.class)
    public ResponseEntity<ErrorResponse> handlePreviousFailure(
            PreviousRequestFailedException ex,
            HttpServletRequest request
    ) {
        log.warn("Returning previous failure: {}", ex.getMessage());
        return buildResponse(HttpStatus.valueOf(ex.getStatus()), ex, request);
    }

    @ExceptionHandler({NotFoundException.class, WalletNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(
            BaseApiExceptionModel ex,
            HttpServletRequest request
    ) {
        logApiError(ex);
        return buildResponse(HttpStatus.NOT_FOUND, ex, request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException ex,
            HttpServletRequest request
    ) {
        logApiError(ex);
        return buildResponse(HttpStatus.CONFLICT, ex, request);
    }

    @ExceptionHandler(CurrencyMismatchException.class)
    public ResponseEntity<ErrorResponse> handleCurrencyMismatch(
            CurrencyMismatchException ex,
            HttpServletRequest request
    ) {
        logApiError(ex);
        return buildResponse(HttpStatus.BAD_REQUEST, ex, request);
    }

    @ExceptionHandler(WalletLockedException.class)
    public ResponseEntity<ErrorResponse> handleWalletLocked(
            WalletLockedException ex,
            HttpServletRequest request
    ) {
        logApiError(ex);
        return buildResponse(HttpStatus.LOCKED, ex, request);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(
            RateLimitExceededException ex,
            HttpServletRequest request
    ) {
        logApiError(ex);
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, ex, request);
    }

    /* ============================================================
       VALIDATION & TRANSACTION FAILURES
       ============================================================ */

    @ExceptionHandler(BindException.class)
    public ResponseEntity<BadRequestErrorModel> handleValidation(BindException exception) {
        List<BadRequestErrorModel.ValidationError> validationErrors = exception.getBindingResult()
                .getAllErrors().stream()
                .map(error -> new BadRequestErrorModel.ValidationError(getField(error), error.getDefaultMessage())).toList();

        BadRequestErrorModel errorModel = new BadRequestErrorModel("One or more validation errors occurred", validationErrors);
        log.warn("Validation error: {}", errorModel);
        return new ResponseEntity<>(errorModel, null, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TransactionFailedException.class)
    public ResponseEntity<BadRequestErrorModel> handleTransactionFailed(TransactionFailedException ex) {
        // Since TransactionFailedException doesn't have a field, we use a generic one or assume "amount"
        List<BadRequestErrorModel.ValidationError> validationErrors = List.of(
                new BadRequestErrorModel.ValidationError("transaction", ex.getMessage())
        );

        BadRequestErrorModel errorModel = new BadRequestErrorModel("Transaction failed", validationErrors);
        log.warn("Transaction failed: {}", errorModel);
        return new ResponseEntity<>(errorModel, null, HttpStatus.UNPROCESSABLE_ENTITY);
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

    /* ============================================================
       INTERNAL / FALLBACK
       ============================================================ */

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

    /* ============================================================
       INTERNAL
       ============================================================ */

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            BaseApiExceptionModel ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse(ex, HtmlUtils.htmlEscape(request.getRequestURI())));
    }

    private void logApiError(BaseApiExceptionModel ex) {
        log.error(
                "API Error [{} {}]: {}",
                ex.getStatus(),
                ex.getError(),
                ex.getMessage()
        );
    }
}
