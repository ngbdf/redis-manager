package com.newegg.ec.redis.util;

import com.netflix.discovery.shared.Applications;
import com.netflix.eureka.EurekaServerContextHolder;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import com.newegg.ec.redis.entity.AnalyzeInstance;


import java.util.ArrayList;
import java.util.List;

/**
 * @author：Truman.P.Du
 * @createDate: 2018年10月11日 下午1:46:13
 * @version:1.0
 * @description: Eureka工具类
 */
public class EurekaUtil {
	/**
	 * 获取eureka注册节点
	 * 
	 * @return List<AnalyzeInstance>
	 */
	public static List<AnalyzeInstance> getRegisterNodes() {
		PeerAwareInstanceRegistry registry = EurekaServerContextHolder.getInstance().getServerContext().getRegistry();
		Applications applications = registry.getApplications();
		List<AnalyzeInstance> analyzes = new ArrayList<>();
		applications.getRegisteredApplications().forEach((registeredApplication) -> {
			registeredApplication.getInstances().forEach((instance) -> {
				AnalyzeInstance analyzeInstance = new AnalyzeInstance(instance.getIPAddr(), instance.getPort());
				analyzes.add(analyzeInstance);
			});
		});

		return analyzes;
	}
}
