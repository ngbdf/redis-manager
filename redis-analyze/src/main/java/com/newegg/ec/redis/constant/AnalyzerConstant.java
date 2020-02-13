package com.newegg.ec.redis.constant;

/**
 * @author：Truman.P.Du
 * @createDate: 2018年10月16日 上午10:30:32
 * @version:1.0
 * @description: 分析器对照关系
 */
public class AnalyzerConstant {

	public static String SERVER_PORT = "server.port";
	public static String RCT_ELASTICSEARCH_ENABLE = "rct.elasticsearch.enable";
	public static String RCT_ELASTICSEARCH_REST_URLS = "rct.elasticsearch.rest.urls";
	public static String RCT_ELASTICSEARCH_INDEX = "rct.elasticsearch.index";
	public static String RCT_FILTER_BYTES = "rct.filter.bytes";
	public static String RCT_FILTER_ITEM_COUNT = "rct.filter.item.count";
	public static String RCT_MAX_ES_THREAD_NUM = "rct.max.es.thread.num";
	public static String RCT_ANALYZE_KEY_PREFIX_INDEX_LOCATION = "rct.analyze.key.prefix.index.location";
	public static String RCT_ANALYZE_KEY_PREFIX_SEPARATORS = "rct.analyze.key.prefix.separators";
	public static String EUREKA_CLIENT_SERVICEURL_DEFAULTZONE = "eureka.client.serviceUrl.defaultZone";
	public static String EUREKA_INSTANCE_PREFERIPADDRESS = "eureka.instance.preferIpAddress";
	public static String EUREKA_INSTANCE_LEASE_RENEWAL_NTERVAL_IN_SECONDS = "eureka.instance.lease-renewal-interval-in-seconds";
	public static String EUREKA_INSTANCE_LEASE_EXPIRATION_DURATION_IN_SECONDS = "eureka.instance.lease-expiration-duration-in-seconds";
	public static String LOGGING_LEVEL_COM_NETFLIX_EUREKA = "logging.level.com.netflix.eureka";
	public static String LOGGING_LEVEL_COM_NETFLIX_DISCOVERY = "logging.level.com.netflix.discovery";
	public static String SPRING_APPLICATION_NAME = "spring.application.name";
	
	public static int DEFAULT_ANALYZER = 0;
	public static int DATA_TYPE_ANALYZER = 1;
	public static int PREFIX_ANALYZER = 2;
	public static int TOP_KEY_ANALYZER = 3;
	public static int TTL_ANALYZER = 4;
	public static int EXPORT_KEY_BY_PREFIX_ANALYZER = 5;
	public static int EXPORT_KEY_BY_FILTER_ANALYZER = 6;
}
