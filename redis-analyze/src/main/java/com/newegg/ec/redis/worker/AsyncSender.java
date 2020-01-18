package com.newegg.ec.redis.worker;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.newegg.ec.redis.Analyzer;
import com.newegg.ec.redis.base.ElasticSearchUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hulva Luva.H
 * @since 2018年4月18日
 *
 */
public class AsyncSender implements Runnable {
	protected final static Logger LOG = LoggerFactory.getLogger(AsyncSender.class);
	private boolean exit = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		this.doSend();
	}

	private void doSend() {
		Set<String> batchData = new HashSet<>();
		long lastSendTime = System.currentTimeMillis();
		while (!exit) {
			try {
				String dataToSend = AnalyzerWorker.cache.poll(100, TimeUnit.MILLISECONDS);
				// 以500批次/或者10s发送一次数据到ES
				if (dataToSend != null) {
					if (batchData.size() >= 500 || ((System.currentTimeMillis() - lastSendTime) > 1000 * 10)) {
						if (!batchData.isEmpty()) {
							ElasticSearchUtil.bulkIndexDocument1(Analyzer.ESINDEX, "analyze", batchData);
							lastSendTime = System.currentTimeMillis();
							batchData.clear();
						}
					} else {
						batchData.add(dataToSend);
					}

				}

			} catch (Exception e) {
				LOG.error("Send to es failed !", e);
			}
		}

		AnalyzerWorker.cache.drainTo(batchData);
		if (!batchData.isEmpty()) {
			try {
				ElasticSearchUtil.bulkIndexDocument1(Analyzer.ESINDEX, "analyze", batchData);
			} catch (Exception e) {
				LOG.error("last send to es failed !", e);
			}
		}
	}

	public void exit() {
		this.exit = true;
	}

}
