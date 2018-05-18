# 框架设计
> 代码结构介绍，主要分为 app、backend、core、plugin

```  代码结构
├── README.MD
├── docs                             // 文档目录
│   └── redis-manager.sql         // 关联的 sql
├── pom.xml
├── src.main.java.com.newegg.ec.cache
    └── backend                    // 后台任务，包括定时检测集群问题task,redis monitor 获取
    └── core                         // 核心层（日志、注解...）
    └── plugin                       // 插件层
            └── docker               // docker 相关
            └── humback              // humback 相关
            └── machine              // 物理机相关
            └── INodeOperate.java    // 节点操作，启动、删除、重启等
    └── app                          // redis-manager 应用
            └── component            // 一个是节点管理组件，另一个是 redis 管理组件
            └── controller           // 程序路由接口
                └── advice           // 日志切面
                └── check            // 前端检查
                └── security         // 用户权限
                └── websocket        // websocket 用于安装时日志的显示
                └── ....
            └── dao                  // 数据处理层
            └── logic                // 逻辑处理层
            └── model                // 实体层
    └── Application                  // 程序入口
```
