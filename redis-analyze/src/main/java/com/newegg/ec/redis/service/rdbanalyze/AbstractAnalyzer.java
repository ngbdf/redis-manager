package com.newegg.ec.redis.service.rdbanalyze;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.newegg.ec.redis.Analyzer;
import com.newegg.ec.redis.entity.RDBAnalyzeInfo;
import com.newegg.ec.redis.utils.RedisObjectEstimate;
import com.newegg.ec.redis.utils.Report;
import com.newegg.ec.redis.worker.AnalyzerWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;

/**
 * @author：Truman.P.Du
 * @createDate: 2018年10月12日 上午10:54:15
 * @version:1.0
 * @description: 分析器抽象类
 */
public abstract class AbstractAnalyzer {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractAnalyzer.class);

	/**
	 * 分析器名称
	 */
	public String name;
	/**
	 * 当前分析端口
	 */
	public int port;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public abstract void init(Map<String, String> params);

	public abstract void execute(RDBAnalyzeInfo<?> rbdAnalyzeInfo);

	/**
	 * 分析结束调用方法，如果需要自定义请重构 作用主要是处理在整个分析器完成时才处理的场景
	 */
	public void finallyCall() {

	}

	/**
	 * 上报结果到server节点处理逻辑
	 * 
	 * @param analyzerResult
	 * @return
	 */
	public boolean report2Server(Set<String> analyzerResult) {
		boolean result = false;
		String message = JSONArray.toJSONString(analyzerResult);
		Report.report2Server(message);
		return result;
	}

	/**
	 * 上报结果到elasticsearch处理逻辑
	 * 
	 * @param analyzerResult
	 * @return
	 */
//	public boolean report2ES(Set<String> analyzerResult) {
//		boolean result = false;
//		try {
//			ElasticSearchUtil.bulkIndexDocument1(Analyzer.ESINDEX, "analyze", analyzerResult);
//		} catch (Exception e) {
//			LOG.error("report2ES has error.", e);
//		}
//		return result;
//	}

	/**
	 * 异步发送数据到ES中
	 * 
	 * 采用消费者模式 该方法在队列满的话，会阻塞
	 * 
	 * @param data
	 */
	public void async2ES(String data) {
		try {
			AnalyzerWorker.cache.put(data);
		} catch (Exception e) {
			LOG.error("put data to AnalyzerWorker.cache error.", e);
		}
	}


	protected String getDataTypeName(int type) {
		String typeName = null;
		switch (type) {
		case 0:
			typeName = "String";
			break;
		case 1:
		case 10:
		case 14:
			typeName = "List";
			break;
		case 2:
		case 3:
		case 5:
		case 11:
		case 12:
			typeName = "Set";
			break;
		// case 3:
		// typeName = "ZSet";
		// break;
		case 4:
		case 9:
		case 13:
			typeName = "Hash";
			break;
		// case 5:
		// typeName = "ZSet_2";
		// break;
		case 6:
			typeName = "Module";
			break;
		case 7:
			typeName = "Module_2";
			break;
		// case 9:
		// typeName = "Hash_ZipMap";
		// break;
		// case 10:
		// typeName = "List_ZipList";
		// break;
		// case 11:
		// typeName = "Set_IntSet";
		// break;
		// case 12:
		// typeName = "ZSet_ZipList";
		// break;
		// case 13:
		// typeName = "Hash_ZipList";
		// break;
		// case 14:
		// typeName = "List_QuickList";
		// break;
		default:
			break;
		}
		return typeName;
	}

	/**
	 * value的类型为集合，获取集合的item数
	 * 
	 * @param kv
	 * @return
	 */
	protected int calcuItemCount(KeyValuePair<?> kv) {
		int count = -1;
		// string 类型的跳过
		if (kv.getValueRdbType() != 0) {
			switch (kv.getValueRdbType()) {
			case 1:
			case 10:
			case 14:
				List<?> listValue = (List<?>) kv.getValue();
				count = listValue.size();
				break;
			case 2:
			case 3:
			case 5:
			case 11:
			case 12: // zset - LinkedHashSet
				Set<?> setValue = (Set<?>) kv.getValue();
				count = setValue.size();
				break;
			case 4:
			case 9:
			case 13: // hash - LinkedHashMap
				Map<?, ?> mapValue = (Map<?, ?>) kv.getValue();
				count = mapValue.size();
				break;
			default:
				break;
			}
		}
		return count;
	}

	/**
	 * 是否过滤该数据
	 * 
	 * @param kv
	 * @return
	 */
	protected boolean keepItOrNot(KeyValuePair<?> kv) {
		boolean keepIt = false;
		long bytes = RedisObjectEstimate.getRedisObjectSize(kv, Analyzer.USE_Custom_Algo);
		if (bytes > Analyzer.FILTER_Bytes) {
			keepIt = true;
		}
		int itemCount = this.calcuItemCount(kv);
		if (itemCount != -1 && itemCount > Analyzer.FILTER_ItemCount) {
			keepIt = true;
		}
		return keepIt;
	}

}
