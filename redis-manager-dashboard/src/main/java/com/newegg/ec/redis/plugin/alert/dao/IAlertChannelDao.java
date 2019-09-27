package com.newegg.ec.redis.plugin.alert.dao;

import com.newegg.ec.redis.plugin.alert.entity.AlertChannel;
import jdk.nashorn.internal.objects.annotations.Where;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/7/29
 */
public interface IAlertChannelDao {

    @Select("SELECT * FROM alert_channel WHERE group_id = #{groupId}")
    List<AlertChannel> selectAlertChannelByGroupId(Integer groupId);

    @Select("<script>" +
            "SELECT * FROM alert_channel WHERE channel_id IN " +
            "<foreach item='channelId' collection='channelIdList' open='(' separator=',' close=')'>" +
            "#{channelId}" +
            "</foreach>)" +
            "</script>")
    List<AlertChannel> selectAlertChannelByIds(@Param("channelIdList") List<Integer> channelIdList);

    @Insert("INSERT INTO alert_channel (group_id, channel_name, smtp_host, smtp_user_name, smtp_password, email_from, email_to, " +
            "webhook, corp_id, agent_id, corp_secret, channel_type, channel_info, update_time) " +
            "VALUES (#{groupId}, #{channelName}, #{smtpHost}, #{smtpUserName}, #{smtpPassword}, #{emailFrom}, #{emailTo}, " +
            "#{webhook}, #{corpId}, #{agentId}, #{corpSecret}, #{channelType}, #{channelInfo}, NOW())")
    int insertAlertChannel(AlertChannel alertChannel);

    @Update("UPDATE alert_channel SET group_id = #{groupId}, channel_name = #{channelName}, smtp_host = #{smtpHost}, " +
            "smtp_user_name = #{smtpUserName}, smtp_password = #{smtpPassword}, email_from #{emailFrom}, email_to = #{emailTo}, " +
            "webhook = #{webhook}, corp_id = #{corpId}, agent_id = #{agentId}, corp_secret = #{corpSecret}, channel_type = #{channelType}, " +
            "channel_info = #{channelInfo}, update_time = NOW() " +
            "WHERE channel_id = #{channelId}")
    int updateAlertChannel(AlertChannel alertChannel);

    @Delete("DELETE FROM alert_channel WHERE channel_id = #{channelId}")
    int deleteAlertChannelById(Integer channelId);

    @Delete("DELETE FROM alert_channel WHERE group_id = #{groupId}")
    int deleteAlertChannelByGroupId(Integer groupId);

}
