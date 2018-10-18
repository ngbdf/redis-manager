package com.newegg.ec.cache.app.model;

import com.newegg.ec.cache.core.mysql.MysqlField;
import com.newegg.ec.cache.core.mysql.MysqlTable;

import java.io.Serializable;

/**
 * Created by lf52 on 2018/4/26.
 */
@MysqlTable(name = "cluster_check_logs", autoCreate = true)
public class ClusterCheckLog implements Serializable {

    @MysqlField(field = "id", type = "varchar(36)", notNull = true)
    private String id;
    @MysqlField(field = "cluster_id", type = "varchar(64)", notNull = true)
    private String clusterId;
    @MysqlField(field = "node_id", type = "varchar(64)", notNull = true)
    private String nodeId;
    // check公式
    @MysqlField(field = "formula", type = "varchar(256)", notNull = true)
    private String formula;
    @MysqlField(field = "log_type", type = "varchar(16)", notNull = true)
    private LogType logType;
    @MysqlField(field = "log_info", type = "varchar(512)", notNull = true)
    private String logInfo;
    @MysqlField(field = "description", type = "varchar(256)", notNull = true)
    private String description;
    //某个cluster的某个formula是否check通过
    @MysqlField(field = "is_checked", type = "int", notNull = true)
    private Integer isChecked;
    @MysqlField(field = "update_time", type = "int")
    private int updateTime;

    public ClusterCheckLog() {
        // 0:未检查过 1:检查过
        this.isChecked = 0;
    }


    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public LogType getLogType() {
        return logType;
    }

    public void setLogType(LogType logType) {
        this.logType = logType;
    }

    public String getLogInfo() {
        return logInfo;
    }

    public void setLogInfo(String logInfo) {
        this.logInfo = logInfo;
    }

    public int getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(int updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(Integer isChecked) {
        this.isChecked = isChecked;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ClusterCheckLog{" +
                "id='" + id + '\'' +
                ", clusterId='" + clusterId + '\'' +
                ", nodeId='" + nodeId + '\'' +
                ", formula='" + formula + '\'' +
                ", logType=" + logType +
                ", logInfo='" + logInfo + '\'' +
                ", description='" + description + '\'' +
                ", isChecked=" + isChecked +
                ", updateTime=" + updateTime +
                '}';
    }

    public static enum LogType {
        warnlog, slowlog
    }

}
