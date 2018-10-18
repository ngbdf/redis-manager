package com.newegg.ec.cache.plugin.machine;

import com.newegg.ec.cache.core.mysql.MysqlField;
import com.newegg.ec.cache.core.mysql.MysqlTable;
import com.newegg.ec.cache.plugin.basemodel.Node;

/**
 * Created by gl49 on 2018/4/22.
 */
@MysqlTable(name = "machine_node", autoCreate = true)
public class MachineNode extends Node {
    @MysqlField(isPrimaryKey = true, field = "id", type = "int")
    private int id;
    @MysqlField(field = "image", type = "varchar(250)", notNull = true)
    private String image;
    @MysqlField(field = "cluster_id", type = "int", notNull = true)
    private int clusterId;
    @MysqlField(field = "user_group", type = "varchar(25)", notNull = true)
    private String userGroup;
    @MysqlField(field = "username", type = "varchar(25)", notNull = true)
    private String username;
    @MysqlField(field = "password", type = "varchar(30)", notNull = true)
    private String password;
    @MysqlField(field = "ip", type = "varchar(25)", notNull = true)
    private String ip;
    @MysqlField(field = "port", type = "smallint", notNull = true)
    private int port;
    @MysqlField(field = "install_path", type = "varchar(200)", notNull = true)
    private String installPath;

    @MysqlField(field = "add_time", type = "int", notNull = true)
    private int addTime;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public String getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(String userGroup) {
        this.userGroup = userGroup;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getInstallPath() {
        return installPath;
    }

    public void setInstallPath(String installPath) {
        this.installPath = installPath;
    }

    public int getAddTime() {
        return addTime;
    }

    public void setAddTime(int addTime) {
        this.addTime = addTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "MachineNode{" +
                "id=" + id +
                ", image='" + image + '\'' +
                ", clusterId=" + clusterId +
                ", userGroup='" + userGroup + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", installPath='" + installPath + '\'' +
                ", addTime=" + addTime +
                '}';
    }
}
