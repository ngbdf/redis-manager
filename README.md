# Redis Manager

[![Build Status](https://user-gold-cdn.xitu.io/2019/11/5/16e3bca6874b2a56?w=90&h=20&f=svg&s=724)](https://travis-ci.org/ngbdf/redis-manager)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

**Redis Manager** 是 Redis 一站式管理平台，支持集群的监控、安装、管理、告警以及基本的数据操作功能  
**集群监控**：支持监控 Memory、Clients 等 Redis 重要指标；可实时查看 Redis Info、Redis Config 和 Slow Log  
**集群创建**：支持 Docker、Machine、Humpback方式  
**集群管理**：支持节点Forget、Replicate Of、Failover、Move Slot、Start、Stop、Restart、Delete、修改配置等功能  
**集群告警**：支持 Memory、Clients 等指标(同监控指标)，支持邮件、企业微信APP、企业微信Webhook、钉钉告警  
**工具箱**：支持 Query、Scan 以及基本的数据操作

[Quick start](https://github.com/ngbdf/redis-manager/wiki/)


# 联系方式
> 您在使用产品的过程中如果遇到问题或者发现需要改进的地方可以通过以下两种方式直接联系我们或 Pull Request。
 
RedisManager QQ群

知乎专栏  https://zhuanlan.zhihu.com/c_1055846842708930560

云栖社区  https://yq.aliyun.com/album/227?spm=a2c4e.11155435.0.0.3ece24f0lQw15C

# 产品主要功能介绍
## Dashboard    
> 展示当前用户组监控的所有集群  

![](https://user-gold-cdn.xitu.io/2019/11/10/16e532f9cac09036?w=1920&h=975&f=png&s=93486)

## 集群导入  
> 导入已存在集群

## 集群监控  
> 监控 Memory、Clients 等 Redis 重要指标; 查询 Slow Log  

![](https://user-gold-cdn.xitu.io/2019/11/10/16e534ada3b4946a?w=1920&h=975&f=png&s=78617)

## 集群管理
> 支持节点Forget、Replicate Of、Failover、Move Slot、Start、Stop、Restart、Delete、修改配置等功能  

![](https://user-gold-cdn.xitu.io/2019/11/10/16e536d1ae64d2b4?w=1920&h=975&f=png&s=85363)

> 修改配置文件

![](https://user-gold-cdn.xitu.io/2019/11/10/16e536d4ef74a99e?w=1920&h=975&f=png&s=92459)

## 集群告警
> 支持邮件、企业微信APP、企业微信Webhook、钉钉告警  

![](https://user-gold-cdn.xitu.io/2019/11/10/16e53730d0f54474?w=1920&h=975&f=png&s=51123)

![](https://user-gold-cdn.xitu.io/2019/11/10/16e537441c8dd9ea?w=1920&h=975&f=png&s=54021)

## 数据操作
> 支持简单的数据操作

![](https://user-gold-cdn.xitu.io/2019/11/10/16e537e77c6071a8?w=1920&h=975&f=png&s=50307)

## 创建集群      
> 目前支持 Redis Cluster、Standalone 模式的创建，包括机器安装、Docker 安装和 Humpback 安装方式。  

![](https://user-gold-cdn.xitu.io/2019/11/10/16e5340d813e35c0?w=1920&h=975&f=png&s=64240)



	
## 用户手册
> 用户手册主要介绍如何使用 Redis Manager

1. [如何启动项目](https://github.com/ngbdf/redis-manager/wiki/如何启动项目)
2. [接入已经存在的集群](https://github.com/ngbdf/redis-manager/wiki/接入已经存在的集群)
3. [如何查看监控](https://github.com/ngbdf/redis-manager/wiki/如何查看监控)
4. [集群管理与节点管理](https://github.com/ngbdf/redis-manager/wiki/集群管理与节点管理)
5. [创建集群(Machine安装)](https://github.com/ngbdf/redis-manager/wiki/创建集群(Machine安装))
6. [创建集群(Docker安装)](https://github.com/ngbdf/redis-manager/wiki/创建集群(Docker安装))
7. [自定义Redis安装包](https://github.com/ngbdf/redis-manager/wiki/自定义Redis安装包)
8. [常见问题汇总](https://github.com/ngbdf/redis-manager/wiki/常见问题汇总)

## 产品设计
> 设计文档主要介绍 redisMangaer 架构设计、框架设计

1. [代码结构介绍](https://github.com/ngbdf/redis-manager/wiki/代码结构介绍) 
2. [监控数据的采集](https://github.com/ngbdf/redis-manager/wiki/监控数据的采集)

## License
RedisManager is Open Source software released under the  [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html)


