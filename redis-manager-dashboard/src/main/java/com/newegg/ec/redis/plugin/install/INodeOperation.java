package com.newegg.ec.redis.plugin.install;

/**
 * @author Jay.H.Zou
 * @date 2019/7/19
 */
public interface INodeOperation {

    boolean start();

    boolean stop();

    boolean restart();

    boolean remove();

}
