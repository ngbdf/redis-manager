package com.newegg.ec.cache.app.model;

import com.newegg.ec.cache.core.mysql.MysqlField;
import com.newegg.ec.cache.core.mysql.MysqlTable;

import java.io.Serializable;

/**
 * Created by lf52 on 2018/4/26.
 */
@MysqlTable(name = "cluster_check_rule", autoCreate = true)
public class ClusterCheckRule implements Serializable {

    @MysqlField(field = "id", type = "varchar(36)", notNull = true)
    private String id;
    @MysqlField(field = "cluster_id", type = "varchar(32)", notNull = true)
    private String clusterId;
    @MysqlField(field = "limit_name", type = "varchar(64)", notNull = true)
    private String limitName;
    // check公式
    @MysqlField(field = "formula", type = "varchar(256)", notNull = true)
    private String formula;
    @MysqlField(field = "description", type = "varchar(256)", notNull = true)
    private String description;
    @MysqlField(field = "update_time", type = "int")
    private int updateTime;

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

    public String getLimitName() {
        return limitName;
    }

    public void setLimitName(String limitName) {
        this.limitName = limitName;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(int updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "ClusterCheckRule{" +
                "id=" + id +
                ", clusterId='" + clusterId + '\'' +
                ", limitName='" + limitName + '\'' +
                ", formula='" + formula + '\'' +
                ", description='" + description + '\'' +
                ", updateTime='" + updateTime + '\'' +
                '}';
    }

}
