package com.newegg.ec.redis.entity;


import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;

/**
 * @author Kyle.K.Zhao
 * @date 1/8/2020 16:26
 */
@TableName("rdb_analyze_result")
public class RDBAnalyzeResult {
	@TableId(value="id",type= IdType.AUTO)
	private Long id;
	@TableField(value="schedule_id")
	private Long scheduleId;
	@TableField(value="cluster_id")
	private Long clusterId;

	public Long getClusterId() {
		return clusterId;
	}

	public void setClusterId(Long clusterId) {
		this.clusterId = clusterId;
	}

	private String result;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getScheduleId() {
		return scheduleId;
	}
	public void setScheduleId(Long scheduleId) {
		this.scheduleId = scheduleId;
	}

	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
}