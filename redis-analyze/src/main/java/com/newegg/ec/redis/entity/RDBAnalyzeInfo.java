package com.newegg.ec.redis.entity;

import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;

/**
 * @author：Truman.P.Du
 * @createDate: 2019年2月21日 下午1:33:05
 * @version:1.0
 * @param <T>
 * @param <T>
 * @description:
 */
public class RDBAnalyzeInfo<T> {

	private Long bytesSize;

	private KeyValuePair<T> kv;

	public RDBAnalyzeInfo(KeyValuePair<T> kv, Long bytesSize) {
		this.bytesSize = bytesSize;
		this.kv = kv;
	}

	public Long getBytesSize() {
		return bytesSize;
	}

	public void setBytesSize(Long bytesSize) {
		this.bytesSize = bytesSize;
	}

	public KeyValuePair<T> getKv() {
		return kv;
	}

	public void setKv(KeyValuePair<T> kv) {
		this.kv = kv;
	}

}
