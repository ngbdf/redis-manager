package com.newegg.ec.redis.client.exception;

/**
 * Created by lf52 on 2019/2/21.
 */
public class RedisClientException extends RuntimeException{

    public RedisClientException(){
        super();
    }
    public RedisClientException(String message){
        super(message);
    }
    public RedisClientException(String message, Throwable cause){
        super(message+"-"+cause.getMessage(),cause);
    }

}
