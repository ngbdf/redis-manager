package com.newegg.ec.cache.app.model;

import com.newegg.ec.cache.core.mysql.MysqlField;
import com.newegg.ec.cache.core.mysql.MysqlTable;
/**
 * Created by gl49 on 2018/4/20.
 */
@MysqlTable( name = "cluster", autoCreate = true)
public class Cluster {
    @MysqlField(isPrimaryKey = true, field = "id", type = "int")
    private int id;
    @MysqlField(field = "cluster_name", type = "varchar(25)", notNull = true)
    private String clusterName;
    @MysqlField(field = "user_group", type = "varchar(30)", notNull = true)
    private String userGroup;
    @MysqlField(field = "address", type = "varchar(255)", notNull = true)
    private String address;
    @MysqlField(field = "redisPassword", type = "varchar(255)", notNull = false)
    private String redisPassword;
    @MysqlField(field = "ssl_username", type = "varchar(64)", notNull = false)
    private String sslUsername;
    @MysqlField(field = "ssl_password", type = "varchar(64)", notNull = false)
    private String sslPassword;
    @MysqlField(field = "cluster_type", type = "varchar(10)", notNull = true)
    private String clusterType;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(String userGroup) {
        this.userGroup = userGroup;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }

    public String getSslUsername() {
        return sslUsername;
    }

    public void setSslUsername(String sslUsername) {
        this.sslUsername = sslUsername;
    }

    public String getSslPassword() {
        return sslPassword;
    }

    public void setSslPassword(String sslPassword) {
        this.sslPassword = sslPassword;
    }

    public String getClusterType() {
        return clusterType;
    }

    public void setClusterType(String clusterType) {
        this.clusterType = clusterType;
    }

    @Override
    public String toString() {
        return "Cluster{" +
                "id=" + id +
                ", clusterName='" + clusterName + '\'' +
                ", userGroup='" + userGroup + '\'' +
                ", address='" + address + '\'' +
                ", redisPassword='" + redisPassword + '\'' +
                ", sslUsername='" + sslUsername + '\'' +
                ", sslPassword='" + sslPassword + '\'' +
                ", clusterType='" + clusterType + '\'' +
                '}';
    }
}
