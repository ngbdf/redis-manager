package com.newegg.ec.redis.entity;

import redis.clients.jedis.HostAndPort;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 7/26/2019
 */
public class SlowLogParam {

    private String clusterId;

    private List<HostAndPort> hostAndPortList;

    private int limit;

}
