#!/bin/bash
if [ $# -le 0 ]; then
        echo "must one param!!!!"
        exit 1
fi
port=$1
password=$2
if test -z "$password"
then
	./redis/src/redis-cli -h 127.0.0.1 -p ${port} shutdown > /dev/null 2>&1
else
	./redis/src/redis-cli -h 127.0.0.1 -a ${password} -p ${port} shutdown > /dev/null 2>&1
	
fi
