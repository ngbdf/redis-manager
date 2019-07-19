package com.newegg.ec.redis.service;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/7/19
 */
public interface IQueryService {

    List<String> scan(String key);

    Object query(String key);

}
