package com.newegg.ec.redis;
import com.newegg.ec.redis.base.ElasticSearchUtil;
import com.newegg.ec.redis.entity.AnalyzeStatus;
import com.newegg.ec.redis.service.RDBAnalyzeService;
import com.newegg.ec.redis.utils.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author：Truman.P.Du
 * @createDate: 2018年3月21日 下午3:59:19
 * @version:1.0
 * @description: 程序入口
 */
@SpringBootApplication
@EnableDiscoveryClient
public class Analyzer implements CommandLineRunner {
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

  public static void main(String[] args) {
    SpringApplication.run(Analyzer.class, args);
    LOG.info("RCT analyzer start success!");
  }

  @Override
  public void run(String... args) throws Exception {
    RDBAnalyzeService.setStatus(AnalyzeStatus.NOTINIT);
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
  }
}
