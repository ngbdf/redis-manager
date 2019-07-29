package com.newegg.ec.redis.plugin.alert.service;

import com.newegg.ec.redis.plugin.alert.entity.AlertMessage;

/**
 * @author Jay.H.Zou
 * @date 2019/7/19
 */
public interface INotifyService {


    boolean notify(AlertMessage alertMessage);

}
