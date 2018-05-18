create database if not exists redis_manager_new;
use redis_manager_new;

create table if not exists user (
  id int auto_increment primary key,
  username varchar(64) not null,
  password varchar(64) not null,
  user_group varchar(30) not null
)ENGINE=MyISAM DEFAULT CHARSET=utf8;

create table if not exists cluster (
  id int auto_increment primary key,
  user_group varchar(30) not null,
  ip varchar(25) not null,
  port int not null,
  ssl_username varchar(64) DEFAULT NULL,
  ssl_password varchar(64) DEFAULT NULL
)ENGINE=MyISAM DEFAULT CHARSET=utf8;
