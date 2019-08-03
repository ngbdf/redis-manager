package com.newegg.ec.redis.exception;

/**
 * @author Jay.H.Zou
 * @date 8/3/2019
 */
public class ConfigurationException extends RuntimeException {

    private int code;

    private String message;

    public ConfigurationException() {
        super();
    }

    public ConfigurationException(String message) {
        super(message);
    }
}
