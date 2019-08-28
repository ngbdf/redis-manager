#!/bin/bash
set -e
if [ $# -le 0 ]; then
        echo "must one param!!!!"
        exit 1
fi
port=$1
basepath=$(cd `dirname $0`; pwd)
conf_dir="${basepath}/conf/"
conf_file="${conf_dir}redis.conf"
sed -i "s#{dir}#${conf_dir}#g" ${conf_file}
sed -i "s#{port}#${port}#g" ${conf_file}

./redis/src/redis-server conf/redis.conf & > /dev/null 2>&1
