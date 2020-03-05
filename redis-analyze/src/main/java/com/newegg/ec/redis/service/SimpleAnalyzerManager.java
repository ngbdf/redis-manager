package com.newegg.ec.redis.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.newegg.ec.redis.Analyzer;
import com.newegg.ec.redis.cache.AppCache;
import com.newegg.ec.redis.entity.AnalyzeStatus;
import com.newegg.ec.redis.entity.RDBAnalyzeInfo;
import com.newegg.ec.redis.service.rdbanalyze.*;
import com.newegg.ec.redis.utils.RedisObjectEstimate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.moilioncircle.redis.replicator.RedisReplicator;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.rdb.AuxFieldListener;
import com.moilioncircle.redis.replicator.rdb.RdbListener;
import com.moilioncircle.redis.replicator.rdb.datatype.AuxField;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;

/**
 * @author Hulva Luva.H
 * @since 2018年4月3日
 * 
 *        Truman edit for 分析器管理者，主要实现选择是否哪种分析器
 */
public class SimpleAnalyzerManager {
	private final static Logger LOG = LoggerFactory.getLogger(SimpleAnalyzerManager.class);
	private Long startTime = 0L;
	private Long lasActive = 0L;
	// 配合展示分析进度
	private static long counter = 0;

	private List<AbstractAnalyzer> analyzers;

	private static AnalyzeStatus status = null;
	private static int port;

	public SimpleAnalyzerManager(int[] AnalyzerConstants) {
		analyzers = new ArrayList<>();
		for (int i : AnalyzerConstants) {
			if (i == 0) {
				// 默认选择器，属于数据轻量级，仅用作生成报表
				// 不同数据类型
				analyzers.add(new DataTypeAnalyzer());
				// 不同前缀
				analyzers.add(new PrefixAnalyzer());
				// TopKey
				analyzers.add(new TopKeyAnalyzer());
				// TTL
				analyzers.add(new TTLAnalyzer());
			}

			if (i == 5) {
				analyzers.add(new ExportKeyByPrefixAnalyzer());
			}

			if (i == 6) {
				analyzers.add(new ExportKeyByFilterAnalyzer());
			}
		}
		status = AnalyzeStatus.READY;
	}

	public void setStatus(AnalyzeStatus _status) {
		status = _status;
	}

	public void execute(String rdbPath) {
		Replicator replicator = null;
		try {
			LOG.info(">>> Start Analyzing " + port);

			this.startTime = System.currentTimeMillis();
			// for Linux and Windows compatible
			String realRDBPath = rdbPath;
			if (!realRDBPath.startsWith("/")) {
				realRDBPath = "/" + rdbPath;
				String[] temp = realRDBPath.split("\\\\");
				realRDBPath = "";
				// replace \ with /
				for (int i = 0, length = temp.length; i < length; i++) {
					if (i < length - 1) {
						realRDBPath += temp[i] + "/";
					} else {
						realRDBPath += temp[i];
					}
				}

			}
			Map<String, String> params = new HashMap<String, String>();
			params.put("port", port + "");
			params.put("customPrefix", RDBAnalyzeService.getScheduleInfo().getPrefixes());
			// 初始化所有分析器
			analyzers.forEach(analyzer -> analyzer.init(params));

			replicator = new RedisReplicator("redis://" + realRDBPath);

			String used_mem_flag = "used-mem";
			AppCache.redisUsedMems.clear();
			replicator.addAuxFieldListener(new AuxFieldListener() {
				@Override
				public void handle(Replicator replicator, AuxField auxField) {
					String auxKey = auxField.getAuxKey();
					if (used_mem_flag.equalsIgnoreCase(auxKey)) {
						Long used_mem = Long.parseLong(auxField.getAuxValue());
						AppCache.redisUsedMems.add(used_mem);
					}

				}
			});

			replicator.addRdbListener(new RdbListener.Adaptor() {

				@SuppressWarnings("unchecked")
				@Override
				public void handle(Replicator rep, KeyValuePair<?> kv) {
					try {
						if (AnalyzeStatus.CANCELED.equals(RDBAnalyzeService.getStatus())) {
							status = AnalyzeStatus.CANCELED;
							return;
						}
						counter += 1;
						status = AnalyzeStatus.RUNNING;
						lasActive = System.currentTimeMillis();
						Long bytesSizeEstimate = RedisObjectEstimate.getRedisObjectSize(kv, Analyzer.USE_Custom_Algo);
						@SuppressWarnings("rawtypes")
						RDBAnalyzeInfo rbdAnalyzeInfo = new RDBAnalyzeInfo(kv, bytesSizeEstimate);
						for (AbstractAnalyzer analyzer : analyzers) {
							analyzer.execute(rbdAnalyzeInfo);
						}
						// 尽可能让jvm回收不用的内存
						rbdAnalyzeInfo = null;
					} catch (Exception e) {
						status = AnalyzeStatus.ERROR;
						e.printStackTrace();
					}
				}

			});

			replicator.open();
			String ending = AnalyzeStatus.CANCELED.equals(status) ? " Canceled" : " Done";

			// RDB文件分析完成后需要执行操作
			analyzers.forEach(analyzer -> analyzer.finallyCall());
			LOG.info(">>> Analyzing " + port + ending);

			status = AnalyzeStatus.CANCELED.equals(status) ? status : AnalyzeStatus.DONE;
			try {
				if (replicator != null) {
					replicator.close();
				}
			} catch (IOException e1) {
				LOG.error("Close RDB replicator error", e1);
			}
		} catch (Exception e) {
			LOG.error("RDB replicator error", e);
		} finally {
			try {
				if (replicator != null) {
					replicator.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public JSONObject result() {
		JSONObject ret = new JSONObject();
		ret.put("startTime", this.startTime);
		ret.put("lasActive", this.lasActive);
		ret.put("status", status);
		ret.put("count", String.valueOf(counter));
		return ret;
	}

	public void setPort(int _port) {
		port = _port;
	}

	public static int currentPort() {
		return port;
	}

	public void reset() {
		this.startTime = 0L;
		this.lasActive = 0L;
		counter = 0;
	}
}
