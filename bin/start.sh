#!/bin/bash
set encoding=utf-8
project_path=$(cd `dirname $0`; pwd)
cd $project_path
cd ../
mkdir logs

CLASSPATH=conf/:web/
for i in lib/*.jar; do
        CLASSPATH=${CLASSPATH}:$i
done
for i in *.jar; do
        CLASSPATH=${CLASSPATH}:$i
done
echo ${CLASSPATH}
java -cp ${CLASSPATH} com.newegg.ec.redis.RedisManagerApplication >> logs/start.log 2>&1 < /dev/null &
echo $! > pid
