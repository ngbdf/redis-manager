package com.newegg.ec.redis.entity;

/**
 * @author：Truman.P.Du
 * @createDate: 2018年4月13日 上午8:19:47
 * @version:1.0
 * @description: 调度任务进度实体
 */
public class ScheduleDetail {
	private long scheduleID;
	/**
	 * 分析redis节点ip:port
	 */
	private String instance;
	
	private AnalyzeStatus status;
	/**
	 * 调度是否成功
	 */
	private boolean scheduleStatus;
	private int process = 0;

	public ScheduleDetail() {

	}

	public ScheduleDetail(long scheduleID, String instance, boolean scheduleStatus, AnalyzeStatus status) {
		this.scheduleID = scheduleID;
		this.instance = instance;
		this.process = 0;
		this.scheduleStatus = scheduleStatus;
		this.status = status;
	}

	public ScheduleDetail(long scheduleID, String instance, int process, boolean scheduleStatus, AnalyzeStatus status) {
		this.scheduleID = scheduleID;
		this.instance = instance;
		this.process = process;
		this.scheduleStatus = scheduleStatus;
		this.status = status;
	}

	public long getScheduleID() {
		return scheduleID;
	}

	public void setScheduleID(long scheduleID) {
		this.scheduleID = scheduleID;
	}

	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public AnalyzeStatus getStatus() {
		return status;
	}

	public void setStatus(AnalyzeStatus status) {
		this.status = status;
	}

	public int getProcess() {
		return process;
	}

	public void setProcess(int process) {
		this.process = process;
	}

	public boolean isScheduleStatus() {
		return scheduleStatus;
	}

	public void setScheduleStatus(boolean scheduleStatus) {
		this.scheduleStatus = scheduleStatus;
	}
}
