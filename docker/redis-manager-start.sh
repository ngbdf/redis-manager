#!/bin/bash
set encoding=utf-8

if [ -n "$CONFIG_URL" ];then
  if wget -O application.yml.temp $CONFIG_URL; then
    echo "Redis manager run with CONFIG_URL get config."
    mv application.yml.temp ./conf/application.yml
  else
    echo "Download remote config fail."
  fi
fi

if [ "`ls -A data`" = ""]; then
  echo "The data dir is empty."
  cp -r backup_data/* data
fi
echo "Add class path."
# start redis manager
CLASSPATH=./:conf/
for i in lib/*.jar; do
        CLASSPATH=${CLASSPATH}:$i
done
for i in *.jar; do
        CLASSPATH=${CLASSPATH}:$i
done
echo ${CLASSPATH}
java -cp ${CLASSPATH} com.newegg.ec.redis.RedisManagerApplication