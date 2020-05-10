package com.newegg.ec.redis.plugin.alert.dao;

import com.newegg.ec.redis.plugin.alert.entity.AlertChannel;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/7/29
 */
@Mapper
public interface IAlertChannelDao {

    @Select("SELECT * FROM alert_channel WHERE group_id = #{groupId}")
    List<AlertChannel> selectAlertChannelByGroupId(Integer groupId);

    @Select("SELECT * FROM alert_channel WHERE channel_id = #{channelId}")
    AlertChannel selectAlertChannelById(Integer channelId);

    @Select("<script>" +
            "SELECT * FROM alert_channel WHERE channel_id IN " +
            "<foreach item='channelId' collection='channelIdList' open='(' separator=',' close=')'>" +
            "#{channelId}" +
            "</foreach>" +
            "</script>")
    List<AlertChannel> selectAlertChannelByIds(@Param("channelIdList") List<Integer> channelIdList);

    @Select("<script>" +
            "SELECT * FROM alert_channel WHERE group_id = #{groupId} " +
            "AND channel_id NOT IN " +
            "<foreach item='channelId' collection='channelIdList' open='(' separator=',' close=')'>" +
            "#{channelId}" +
            "</foreach>" +
            "</script>")
    List<AlertChannel> selectAlertChannelNotUsed(@Param("groupId") Integer groupId, @Param("channelIdList") List<Integer> channelIdList);

    @Insert("INSERT INTO alert_channel (group_id, channel_name, smtp_host, email_user_name, email_password, email_from, email_to, " +
            "webhook, corp_id, agent_id, corp_secret, token, channel_type, channel_info, update_time) " +
            "VALUES (#{groupId}, #{channelName}, #{smtpHost}, #{emailUserName}, #{emailPassword}, #{emailFrom}, #{emailTo}, " +
            "#{webhook}, #{corpId}, #{agentId}, #{corpSecret}, #{token}, #{channelType}, #{channelInfo}, NOW())")
    int insertAlertChannel(AlertChannel alertChannel);

    @Update("UPDATE alert_channel SET group_id = #{groupId}, channel_name = #{channelName}, smtp_host = #{smtpHost}, " +
            "email_user_name = #{emailUserName}, email_password = #{emailPassword}, email_from = #{emailFrom}, email_to = #{emailTo}, " +
            "webhook = #{webhook}, corp_id = #{corpId}, agent_id = #{agentId}, corp_secret = #{corpSecret}, token = #{token}, " +
            "channel_type = #{channelType}, channel_info = #{channelInfo}, update_time = NOW() " +
            "WHERE channel_id = #{channelId}")
    int updateAlertChannel(AlertChannel alertChannel);

    @Delete("DELETE FROM alert_channel WHERE channel_id = #{channelId}")
    int deleteAlertChannelById(Integer channelId);

    @Delete("DELETE FROM alert_channel WHERE group_id = #{groupId}")
    int deleteAlertChannelByGroupId(Integer groupId);

    @Select("create TABLE IF NOT EXISTS `alert_channel` (" +
            "channel_id integer(4) NOT NULL AUTO_INCREMENT, " +
            "group_id integer(4) NOT NULL, " +
            "channel_name varchar(50) NOT NULL, " +
            "smtp_host varchar(255) DEFAULT NULL, " +
            "email_user_name varchar(255) DEFAULT NULL, " +
            "email_password varchar(255) DEFAULT NULL, " +
            "email_from varchar(255) DEFAULT NULL, " +
            "email_to varchar(255) DEFAULT NULL, " +
            "webhook varchar(255) DEFAULT NULL, " +
            "corp_id varchar(255) DEFAULT NULL, " +
            "agent_id varchar(255) DEFAULT NULL, " +
            "corp_secret varchar(255) DEFAULT NULL, " +
            "token varchar(255) DEFAULT NULL, " +
            "channel_type integer(2) DEFAULT NULL, " +
            "channel_info varchar(255) DEFAULT NULL, " +
            "global tinyint(1) DEFAULT 1, " +
            "update_time datetime(0) NOT NULL, " +
            "PRIMARY KEY (channel_id) " +
            ") ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;")
    void createAlertChannelTable();

}
