package com.newegg.ec.redis.entity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author：Truman.P.Du
 * @createDate: 2018年4月11日 下午1:59:17
 * @version:1.0
 * @description:
 */
public class ScheduleInfo {
	private long scheduleID;
	private String dataPath;
	private String prefixes;
	private int[] analyzerTypes;

	private Set<String> ports = new HashSet<>();

	public ScheduleInfo() {

	}

	public ScheduleInfo(long scheduleID, String dataPath, String prefixes, Set<String> ports, int[] analyzerTypes) {
		this.scheduleID = scheduleID;
		this.dataPath = dataPath;
		this.prefixes = prefixes;
		this.ports = ports;
		this.analyzerTypes = analyzerTypes;
	}

	public long getScheduleID() {
		return scheduleID;
	}

	public void setScheduleID(long scheduleID) {
		this.scheduleID = scheduleID;
	}

	public String getDataPath() {
		return dataPath;
	}

	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}

	public Set<String> getPorts() {
		return ports;
	}

	public void setPorts(Set<String> ports) {
		this.ports = ports;
	}

	public String getPrefixes() {
		return prefixes;
	}

	public void setPrefixes(String prefixes) {
		this.prefixes = prefixes;
	}

	public int[] getAnalyzerTypes() {
		return analyzerTypes;
	}

	public void setAnalyzerTypes(int[] analyzerTypes) {
		this.analyzerTypes = analyzerTypes;
	}

	@Override
	public String toString() {
		return "ScheduleInfo [scheduleID=" + scheduleID + ", dataPath=" + dataPath + ", prefixes=" + prefixes
				+ ", analyzerTypes=" + Arrays.toString(analyzerTypes) + ", ports=" + ports + "]";
	}
}
