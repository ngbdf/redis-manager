package com.newegg.ec.redis.exception;

/**
 * @author Jay.H.Zou
 * @date 2019/8/5
 */
public class InvalidCommandException extends RuntimeException {

    private int code;

    private String message;

    public InvalidCommandException() {
        super();
    }

    public InvalidCommandException(String message) {
        super(message);
    }
}
