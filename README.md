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

<img src="./documents/images/index.png"/>

## 集群导入  
> 导入已存在集群

## 集群监控  
> 监控 Memory、Clients 等 Redis 重要指标; 查询 Slow Log  

<img src="./documents/images/monitor.png"/>

## 集群管理
> 支持节点Forget、Replicate Of、Failover、Move Slot、Start、Stop、Restart、Delete、修改配置等功能  

<img src="./documents/images/node-manage.png"/>

> 修改配置文件

<img src="./documents/images/edit-conf.png"/>

## 集群告警
> 支持邮件、企业微信APP、企业微信Webhook、钉钉告警  

<img src="./documents/images/alert-manage/cluster-rule.png"/>

<img src="./documents/images/alert-manage/cluster-channel.png"/>

## 数据操作
> 支持简单的数据操作

<img src="./documents/images/data-operation.png"/>

## 创建集群      
> 目前支持 Redis Cluster、Standalone 模式的创建，包括机器安装、Docker 安装和 Humpback 安装方式  

<img src="./documents/images/installation/cluster-docker-auto.png"/>
	
## 用户组管理  

<img src="./documents/images/group-manage.png"/>

## 用户管理  

<img src="./documents/images/user-manage/user-manage.png"/>

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

## License
RedisManager is Open Source software released under the  [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html)


