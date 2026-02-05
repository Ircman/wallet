package com.syneronix.wallet.api.errors;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = false)
@Data
public class BaseApiExceptionModel extends RuntimeException{

    private int status;
    private String error;
    private String message;

    public BaseApiExceptionModel(HttpStatus status, String message) {
        super(message);
        this.status = status.value();
        this.error = status.name().toUpperCase();
        this.message = message;
    }


}
