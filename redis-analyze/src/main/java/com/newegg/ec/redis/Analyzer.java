package com.newegg.ec.redis;
import com.newegg.ec.redis.base.ElasticSearchUtil;
import com.newegg.ec.redis.constant.AnalyzerConstant;
import com.newegg.ec.redis.entity.AnalyzeStatus;
import com.newegg.ec.redis.service.RDBAnalyzeService;
import com.newegg.ec.redis.utils.Report;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author：Truman.P.Du
 * @createDate: 2018年3月21日 下午3:59:19
 * @version:1.0
 * @description: 程序入口
 */
@SpringBootApplication
@EnableDiscoveryClient
public class Analyzer implements CommandLineRunner ,WebServerFactoryCustomizer<ConfigurableWebServerFactory>  {
  private final static Logger LOG = LoggerFactory.getLogger(Analyzer.class);

  @Value("${rct.elasticsearch.rest.urls}")
  private String esUrls;

  @Value("${rct.elasticsearch.index}")
  private String esIndex;
  @Value("${rct.elasticsearch.enable:false}")
  private boolean elasticsearchEnable;

  @Value("${rct.filter.bytes:512}")
  private Long filterBytes;

  @Value("${rct.filter.item.count:10}")
  private Integer filterItemCount;

  @Value("${rct.max.es.thread.num:10}")
  private Integer maxSenderThread;
  
  @Value("${eureka.client.serviceUrl.defaultZone}")
  private String serviceUrl;
  
  @Value("${rct.analyze.key.prefix.index.location:last}")
  private String keyPrefixIndexLocation;
  
  @Value("${rct.analyze.key.prefix.separators}")
  private String keyPrefixSeparators;
  

  public static String ESINDEX = null;
  public static String REGISTER_ROOT = null;
  public static Boolean USE_Custom_Algo = true;
  public static Boolean INCLUDE_NoMatchPrefixKey = false;
  public static Long FILTER_Bytes;
  public static Integer FILTER_ItemCount;
  public static Integer MAX_EsSenderThreadNum;
  public static Integer MAX_QUEUE_SIZE;
  public static String  KEY_PREFIX_INDEX_LOCATION;
  public static String  KEY_PREFIX_SEPARATORS;

  // 分析器所需参数
  public static Map<String, String> configMap = new HashMap<String, String>();
  
  public static void main(String[] args) {
    SpringApplication.run(Analyzer.class, args);
    LOG.info("RCT analyzer start success!");
  }

  @Override
  public void run(String... args) throws Exception {
    RDBAnalyzeService.setStatus(AnalyzeStatus.NOTINIT);
    
    // 从配置文件中获取参数
    if(elasticsearchEnable) {
    	ElasticSearchUtil.init(30000, 30000, esUrls);
    }
    Report.init(30000, 30000, serviceUrl);
    ESINDEX = this.esIndex;
    FILTER_Bytes = this.filterBytes;
    FILTER_ItemCount = this.filterItemCount;
    MAX_EsSenderThreadNum = this.maxSenderThread;
    MAX_QUEUE_SIZE = 1000;
    KEY_PREFIX_INDEX_LOCATION = this.keyPrefixIndexLocation;
    KEY_PREFIX_SEPARATORS = this.keyPrefixSeparators;
    
    // 从configMap中获取参数	(使用时放开以下注释，同时注释掉以上代码)
//    if(Boolean.valueOf(configMap.get(AnalyzerConstant.RCT_ELASTICSEARCH_ENABLE))) {
//    	ElasticSearchUtil.init(30000, 30000, configMap.get(AnalyzerConstant.RCT_ELASTICSEARCH_REST_URLS));
//    }
//    Report.init(30000, 30000, configMap.get(AnalyzerConstant.EUREKA_CLIENT_SERVICEURL_DEFAULTZONE));
//    ESINDEX = Analyzer.configMap.get(AnalyzerConstant.RCT_ELASTICSEARCH_INDEX);
//    FILTER_Bytes = Long.valueOf(Analyzer.configMap.get(AnalyzerConstant.RCT_FILTER_BYTES));
//    FILTER_ItemCount = Integer.valueOf(Analyzer.configMap.get(AnalyzerConstant.RCT_FILTER_ITEM_COUNT));
//    MAX_EsSenderThreadNum = Integer.valueOf(Analyzer.configMap.get(AnalyzerConstant.RCT_MAX_ES_THREAD_NUM));
//    MAX_QUEUE_SIZE = 1000;
//    KEY_PREFIX_INDEX_LOCATION = Analyzer.configMap.get(AnalyzerConstant.RCT_ANALYZE_KEY_PREFIX_INDEX_LOCATION);
//    KEY_PREFIX_SEPARATORS = Analyzer.configMap.get(AnalyzerConstant.RCT_ANALYZE_KEY_PREFIX_SEPARATORS);
  }

  // 从环境变量中获取所有参数，给configMap赋值
  @Override
  public void customize(ConfigurableWebServerFactory factory) {
	configMap.put(AnalyzerConstant.SERVER_PORT, System.getenv().get(AnalyzerConstant.SERVER_PORT));
	configMap.put(AnalyzerConstant.RCT_MAX_ES_THREAD_NUM, System.getenv().get(AnalyzerConstant.RCT_MAX_ES_THREAD_NUM));
	configMap.put(AnalyzerConstant.RCT_FILTER_ITEM_COUNT, System.getenv().get(AnalyzerConstant.RCT_FILTER_ITEM_COUNT));
	configMap.put(AnalyzerConstant.RCT_FILTER_BYTES, System.getenv().get(AnalyzerConstant.RCT_FILTER_BYTES));
	configMap.put(AnalyzerConstant.RCT_ELASTICSEARCH_ENABLE, System.getenv().get(AnalyzerConstant.RCT_ELASTICSEARCH_ENABLE));
	configMap.put(AnalyzerConstant.RCT_ELASTICSEARCH_REST_URLS, System.getenv().get(AnalyzerConstant.RCT_ELASTICSEARCH_REST_URLS));
	configMap.put(AnalyzerConstant.RCT_ELASTICSEARCH_INDEX, System.getenv().get(AnalyzerConstant.RCT_ELASTICSEARCH_INDEX));
	configMap.put(AnalyzerConstant.RCT_ANALYZE_KEY_PREFIX_SEPARATORS, System.getenv().get(AnalyzerConstant.RCT_ANALYZE_KEY_PREFIX_SEPARATORS));
	configMap.put(AnalyzerConstant.RCT_ANALYZE_KEY_PREFIX_INDEX_LOCATION, System.getenv().get(AnalyzerConstant.RCT_ANALYZE_KEY_PREFIX_INDEX_LOCATION));
	configMap.put(AnalyzerConstant.EUREKA_CLIENT_SERVICEURL_DEFAULTZONE, System.getenv().get(AnalyzerConstant.EUREKA_CLIENT_SERVICEURL_DEFAULTZONE));
	configMap.put(AnalyzerConstant.EUREKA_INSTANCE_PREFERIPADDRESS, System.getenv().get(AnalyzerConstant.EUREKA_INSTANCE_PREFERIPADDRESS));
	configMap.put(AnalyzerConstant.EUREKA_INSTANCE_LEASE_RENEWAL_NTERVAL_IN_SECONDS, System.getenv().get(AnalyzerConstant.EUREKA_INSTANCE_LEASE_RENEWAL_NTERVAL_IN_SECONDS));
	configMap.put(AnalyzerConstant.EUREKA_INSTANCE_LEASE_EXPIRATION_DURATION_IN_SECONDS, System.getenv().get(AnalyzerConstant.EUREKA_INSTANCE_LEASE_EXPIRATION_DURATION_IN_SECONDS));
	configMap.put(AnalyzerConstant.LOGGING_LEVEL_COM_NETFLIX_EUREKA, System.getenv().get(AnalyzerConstant.LOGGING_LEVEL_COM_NETFLIX_EUREKA));
	configMap.put(AnalyzerConstant.LOGGING_LEVEL_COM_NETFLIX_DISCOVERY, System.getenv().get(AnalyzerConstant.LOGGING_LEVEL_COM_NETFLIX_DISCOVERY));
	configMap.put(AnalyzerConstant.SPRING_APPLICATION_NAME, System.getenv().get(AnalyzerConstant.SPRING_APPLICATION_NAME));
  }
}
