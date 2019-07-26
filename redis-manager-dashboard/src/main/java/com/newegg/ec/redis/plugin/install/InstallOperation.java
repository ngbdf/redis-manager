package com.newegg.ec.redis.plugin.install;

import com.newegg.ec.redis.entity.InstallParam;
import com.newegg.ec.redis.entity.Machine;

import java.util.List;

/**
 *
 *
 * @author Jay.H.Zou
 * @date 7/26/2019
 */
public interface InstallOperation {

    boolean pullImage();

    boolean buildTopology();

    boolean install();

}
