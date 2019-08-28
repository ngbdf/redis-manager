#!/bin/bash
set -e
redis_path=/redis/redis-4.0.8/
ip=$1
port=$2
params=$#
if [ $params != 2 ]; then
	echo "must two params!"
	exit 1;
fi
data_path=/data/redis/${port}/
mkdir -p ${data_path}
if [ -f "redis.conf" ]; then
	mv redis.conf ${data_path}
fi
redis_conf=${data_path}redis.conf
sed -i "s/{port}/${port}/g" ${redis_conf}
sed -i "s/{ip}/${ip}/g" ${redis_conf}
${redis_path}src/redis-server ${redis_conf}
