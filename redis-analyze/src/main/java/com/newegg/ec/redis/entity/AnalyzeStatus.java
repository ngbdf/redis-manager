package com.newegg.ec.redis.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Hulva Luva.H
 * @since 2018年4月7日
 *
 */
public enum AnalyzeStatus {

  /**
   * 程序启动，等待接受主节点 init 指令
   */
  NOTINIT,

  /**
   * 接收到主节点 init 指令，对接收到的配置内容进行检查中
   */
  CHECKING,

  /**
   * 程序初始化完毕并已检查完从主节点接收到的配置信息，等待主节点 execute 指令
   */
  READY,

  /**
   * 已接收 execute 指令，正在拷贝 rdb 文件
   */
  COPY_RDB,

  /**
   * 接收到主节点 execute 指令，并处于对 RDB 的分析中
   */
  RUNNING,

  /**
   * 分析任务完成
   */
  DONE,

  /**
   * 最近一次的分析任务被手动终止
   */
  CANCELED,

  ERROR,
  
  NOT_START,

  RESETED;

  private static final Map<String, AnalyzeStatus> stringToEnum = new HashMap<String, AnalyzeStatus>();
  static {
    // Initialize map from constant name to enum constant
    for (AnalyzeStatus analyzeStatus : values()) {
      stringToEnum.put(analyzeStatus.toString(), analyzeStatus);
    }
  }

  public static AnalyzeStatus fromString(String symbol) {
    return stringToEnum.get(symbol);
  }

}
