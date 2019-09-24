package com.newegg.ec.redis.exception;

/**
 * @author Jay.H.Zou
 * @date 8/3/2019
 */
public class ParameterException extends RuntimeException {

    private int code;

    private String message;

    public ParameterException() {
        super();
    }

    public ParameterException(String message) {
        super(message);
    }
}
