package com.newegg.ec.cache.module.extend;

import java.util.List;
import java.util.Map;

/**
 * Created by lf52 on 2019/2/26.
 */
public interface IExtensionService {

    /**
     * 手动整理node内存碎片
     * @param clusterId
     * @param ip
     * @param port
     * @return 如果成功memory purge 返回当前的mem_fragmentation_ratio
     */
    public String memoryPurge(int clusterId, String ip, int port);

    /**
     * Memory Doctor功能 : 集群所有节点的内存使用状况诊断
     * @param clusterId
     * @return
     */
    public List<Map<String,String>> memoryDoctor(int clusterId);

}
