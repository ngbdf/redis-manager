package com.newegg.ec.redis.worker; /**
 * 
 */


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.newegg.ec.redis.Analyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hulva Luva.H
 * @since 2018年4月18日
 *
 */
public class AnalyzerWorker {
	protected final static Logger LOG = LoggerFactory.getLogger(AnalyzerWorker.class);

	public static BlockingQueue<String> cache = new LinkedBlockingQueue<>(Analyzer.MAX_QUEUE_SIZE);
	private static final Map<String, AsyncSender> workers = new HashMap<String, AsyncSender>();

	public static void startWorker() {
		AsyncSender sender = null;
		for (int i = 0; i < Analyzer.MAX_EsSenderThreadNum; i++) {
			sender = new AsyncSender();
			Thread thread = new Thread(sender);
			thread.setName("worker" + i);
			thread.start();
			workers.put("worker" + i, sender);
		}
	}

	public static void stopWorker() {
		for (int i = 0; i < workers.size(); i++) {
			AsyncSender sender = workers.get("worker" + i);
			sender.exit();
		}
	}

	public static void workerNumChanged() {
		if (Analyzer.MAX_EsSenderThreadNum < 0) {
			LOG.info("InValid > MAX_EsSenderThreadNum: {}", Analyzer.MAX_EsSenderThreadNum);
			return;
		}
		AsyncSender sender = null;
		if (Analyzer.MAX_EsSenderThreadNum > workers.size()) {
			for (int i = workers.size(); i < Analyzer.MAX_EsSenderThreadNum; i++) {
				sender = new AsyncSender();
				Thread thread = new Thread(sender);
				thread.setName("worker" + i);
				thread.start();
				workers.put("worker" + i, sender);
			}
		}
		if (Analyzer.MAX_EsSenderThreadNum < workers.size()) {
			for (int i = Analyzer.MAX_EsSenderThreadNum, outsider = workers.size(); i < outsider; i++) {
				sender = workers.get("worker" + i);
				sender.exit();
				workers.remove("worker" + i);
			}
		}
	}
}
