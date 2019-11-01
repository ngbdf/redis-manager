package com.newegg.ec.redis.schedule;

import com.newegg.ec.redis.config.SystemConfig;
import com.newegg.ec.redis.entity.User;
import com.newegg.ec.redis.service.IUserService;
import com.newegg.ec.redis.util.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @author Jay.H.Zou
 * @date 9/18/2019
 */
@Component
public class AvatarCleanup implements IDataCleanup {

    private static final Logger logger = LoggerFactory.getLogger(AvatarCleanup.class);

    @Autowired
    protected IUserService userService;

    @Autowired
    private SystemConfig systemConfig;

    /**
     * 每月1日的 00:00 执行
     */
    @Async
    @Scheduled(cron = "0 0 0 1 * ?")
    @Override
    public void cleanup() {
        try {
            List<User> allUser = userService.getAllUser();
            if (allUser == null || allUser.isEmpty()) {
                return;
            }
            List<File> allFilesName = ImageUtil.getAllFiles(systemConfig.getAvatarPath());
            for (User user : allUser) {
                String imageName = ImageUtil.getImageName("" + user.getUserId());
                Iterator<File> iterator = allFilesName.iterator();
                while (iterator.hasNext()) {
                    File file = iterator.next();
                    String fileName = file.getName();
                    if (Objects.equals(imageName, fileName)) {
                        iterator.remove();
                        break;
                    }
                }
            }
            for (File file : allFilesName) {
                try {
                    boolean delete = file.delete();
                } catch (Exception e) {
                    logger.error("clear " + file + " failed.");
                }
            }
        } catch (Exception e) {
            logger.error("Cleanup node info schedule failed.", e);
        }
    }

}
