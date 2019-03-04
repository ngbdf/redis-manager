package com.newegg.ec.cache.backend;

import com.newegg.ec.cache.app.dao.IUserDao;
import com.newegg.ec.cache.app.model.User;
import com.newegg.ec.cache.core.mysql.MysqlUtil;
import com.newegg.ec.cache.core.userapi.UserApiUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gl49 on 2018/4/21.
 */
@Component
public class InitConfig implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private IUserDao userDao;
    @Value("${cache.mysql.database.name}")
    private String databaseName;
    @Value("${cache.user.api.path}")
    private String userApiPath;
    @Value("${cache.mysql.scan.package}")
    private String mysqlScanTable;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        initUserApi();
        initMysqlTable();
        initUserData();
        System.out.println("****************** init success ********************");
    }

    private void initUserData() {
        User user = userDao.getUserByName("admin");
        if (null == user) {
            userDao.addUser(new User("admin", "admin", "admin"));
        }
    }

    /**
     * 初始化 mysql 表
     */
    public void initMysqlTable() {
        List<String> packages = new ArrayList();
        packages.add(mysqlScanTable);
        List<String> sqlList = MysqlUtil.initMysqlTable(packages);
        for (String createSql : sqlList) {
            jdbcTemplate.execute(createSql);
        }
    }

    /**
     * 初始化用户 api
     */
    public void initUserApi() {
        if(StringUtils.isBlank(userApiPath)){
            return;
        }
        List<String> packages = new ArrayList<>();
        packages.add(mysqlScanTable);
        System.out.println(userApiPath);
        String file = userApiPath;
        UserApiUtil.autoGeneriesAllApi(packages, file);
    }
}
