package com.newegg.ec.cache.app.model;

import com.newegg.ec.cache.core.mysql.MysqlField;
import com.newegg.ec.cache.core.mysql.MysqlTable;

/**
 * Created by gl49 on 2018/4/20.
 */
@MysqlTable(name = "user", autoCreate = true)
public class User {
    @MysqlField(isPrimaryKey = true, field = "id", type = "int")
    private int id;
    @MysqlField(field = "username", type = "varchar(64)", notNull = true)
    private String username;
    @MysqlField(field = "password", type = "varchar(64)", notNull = true)
    private String password;
    @MysqlField(field = "user_group", type = "varchar(30)", notNull = true)
    private String userGroup;

    public User(String username, String password, String userGroup) {
        this.username = username;
        this.password = password;
        this.userGroup = userGroup;
    }

    public User(int id, String username, String password, String userGroup) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.userGroup = userGroup;
    }

    public User() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(String userGroup) {
        this.userGroup = userGroup;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", userGroup='" + userGroup + '\'' +
                '}';
    }
}
