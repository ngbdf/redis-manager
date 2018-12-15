# RedisManager

[![Build Status](https://travis-ci.org/ngbdf/redis-manager.svg?branch=master)](https://travis-ci.org/ngbdf/redis-manager)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

Redis Manager 是 Redis 一站式管理平台，支持集群的创建、管理、监控和报警。  
**集群创建**：包含了三种方式 Docker、Machine、Humpback；  
**集群管理**：支持节点扩容、缩容、Slots迁移、BeMaster、BeSlave、Memory Purge、配置修改等功能；  
**集群监控**：Redis 集群重要监控指标，如Memory、Clients、命中率等；可实时查看Redis Info、Redis Config、Slow Log等信息；Query 功能可查询任何类型的Key。  
**集群报警**：支持used_memory、clients等监控，并默认实现了邮件报警，用户可自己对报警接口进行其他实现，如微信、短信报警等。 

[快速开始](https://github.com/ngbdf/redis-manager/wiki/) 


## 联系方式
> 您在使用产品的过程中如果遇到问题或者发现需要改进的地方可以通过以下两种方式直接联系我们。

1. RedisManager讨论群      
[![Join the chat at https://gitter.im/dianping/cat](https://badges.gitter.im/dianping/cat.svg)](https://gitter.im/redis-manager/Lobby?utm_source=share-link&utm_medium=link&utm_campaign=share-link)
2. RedisManager微信群     
 <img src="./docs/images/wechat.png" width="200px"/>

## 产品主要功能介绍
### Dashboard    
> 展示当前用户组监控的所有集群  

![](https://user-gold-cdn.xitu.io/2018/12/14/167aa7768e035622?w=1920&h=938&f=png&s=91256)

### 1.创建集群      
> 目前支持 Redis Cluster 模式的创建，包括机器安装、Docker 安装和 Humpback 安装方式。  

![](https://user-gold-cdn.xitu.io/2018/12/14/167aa7831c6da7a8?w=1920&h=938&f=png&s=24508)
  
![](https://user-gold-cdn.xitu.io/2018/12/14/167aa78813c4297d?w=1920&h=938&f=png&s=45941)

### 2.管理集群
> 管理集群分为 ClusterManager 和 NodeManager 两部分

##### 2.1 ClusterManager
> 集群管理主要包括节点导入、Slots 迁移、Master Slave 角色切换、动态配置、整理内存碎片等功能

![](https://user-gold-cdn.xitu.io/2018/12/14/167aa790e4ab8e10?w=1920&h=938&f=png&s=48100)

##### 2.2 NodeManager
> 节点管理主要包含了节点的启动、关闭、重启、扩容等。

![](https://user-gold-cdn.xitu.io/2018/12/14/167aa7945f5d0682?w=1920&h=938&f=png&s=47976)

### 3.集群监控
> 提供集群监控、查询 Key、查看 Slowlog 等功能。

#### 3.1 具体监控详情
> 可在右上角通过 Time Ranges、Nodes 的选择来查看不同时间或不同节点的监控，监控详情如图：

![](https://user-gold-cdn.xitu.io/2018/12/14/167aa81359cba9bc?w=1920&h=938&f=png&s=174475)

#### 3.2 Query客户端
> 支持任意类型的 Key 查询。

![](https://user-gold-cdn.xitu.io/2018/12/14/167aa8efd47dc148?w=1920&h=938&f=png&s=160886)

### 4.集群报警
> 针对 Redis 集群一些重要指标进行监控报警，默认实现了邮件报警。

![](https://user-gold-cdn.xitu.io/2018/12/14/167aa8fe80f84d8a?w=1920&h=938&f=png&s=44373)


	
## 用户手册
> 用户手册主要介绍如何使用 Redis Manager

0. [如何启动项目](https://github.com/ngbdf/redis-manager/wiki/如何启动项目)
1. [如何创建集群](https://github.com/ngbdf/redis-manager/wiki/如何创建集群)
2. [如何管理或监控一个已存在的集群](https://github.com/ngbdf/redis-manager/wiki/如何管理或监控一个已存在的集群)
3. [如何管理集群](https://github.com/ngbdf/redis-manager/wiki/如何管理集群)
4. [如何管理节点](https://github.com/ngbdf/redis-manager/wiki/如何管理节点)
5. [如何查看监控](https://github.com/ngbdf/redis-manager/wiki/如何查看监控)
6. [query功能介绍](https://github.com/ngbdf/redis-manager/wiki/query功能介绍)

## 产品设计
> 设计文档主要介绍 redisMangaer 架构设计、框架设计

1. [代码结构介绍](https://github.com/ngbdf/redis-manager/wiki/代码结构介绍) 
2. [监控数据的采集](https://github.com/ngbdf/redis-manager/wiki/监控数据的采集)

## License
RedisManager is Open Source software released under the  [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html)


