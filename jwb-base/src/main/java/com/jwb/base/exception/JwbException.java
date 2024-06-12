package com.jwb.base.exception;

import lombok.Getter;

@Getter
public class JwbException extends RuntimeException {
    private String errMessage;

    public JwbException() {

    }

    public JwbException(String message) {
        super(message);
        this.errMessage = message;
    }

    public static void cast(CommonError commonError) {
        throw new JwbException(commonError.getErrMessage());
    }

    public static void cast(String errMessage) {
        throw new JwbException(errMessage);
    }
}
