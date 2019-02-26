package com.newegg.ec.cache.core.logger;


/**
 * Created by lf52 on 2019/2/26.
 */
public class RMException extends RuntimeException {

    public RMException(){
        super();
    }
    public RMException(String message){
        super(message);
    }
    public RMException(String message, Throwable cause){
        super(message+"-"+cause.getMessage(),cause);
    }

}
