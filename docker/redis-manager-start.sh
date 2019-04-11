#!/bin/bash
set encoding=utf-8

# check environment variable
function check() {
  if [ ! "$CONFIG_URL" ]; then
    if [ ! "$MYSQL_URL" ] || [ ! "$MYSQL_USER" ] || [ ! "$MYSQL_PWD" ]; then
      echo "if 'CONFIG_URL' is null, then [MYSQL_URL,MYSQL_USER,MYSQL_PWD] by not null."
      exit 1
    fi
  fi
}

function init_conf(){
  if [ ! -f "./conf/application.yml" ]; then
    cp ./web/application.yml.base ./conf/application.yml
  fi
  if [ ! -f "./conf/log4j.properties" ]; then
    mv ./web/log4j.properties ./conf/log4j.properties
  elif [ -f "./web/log4j.properties" ]; then
    rm -f ./web/log4j.properties
  fi
}

# update config file by environment variable
function up_conf() {
  if [ ! "$CONFIG_URL" ]; then
    cp ./web/application.yml.base ./conf/application.yml
    if [ -n "$SERVICE_PORT" ];then
      echo "service port "$SERVICE_PORT
      sed -i "s/port:.*/port: $SERVICE_PORT/g" ./conf/application.yml
    fi
    if [ -n "$MYSQL_URL" ];then
      echo "mysql url "$MYSQL_URL
      MYSQL_URL=${MYSQL_URL//\&/\\\&}
      sed -i "s#url: jdbc:mysql:.*#url: $MYSQL_URL#g" ./conf/application.yml
    fi
    if [ -n "$MYSQL_USER" ];then
      echo "mysql user name "$MYSQL_USER
      sed -i "s/username: root/username: $MYSQL_USER/g" ./conf/application.yml
    fi
    if [ -n "$MYSQL_PWD" ];then
      echo "mysql password ******"
      sed -i "s/password:.*/password: $MYSQL_PWD/g" ./conf/application.yml
    fi

  elif [ -n "$CONFIG_URL" ];then
    if wget -O application.yml.temp $CONFIG_URL; then
      echo "Redis manager run with CONFIG_URL get config."
      mv application.yml.temp ./conf/application.yml
    else
      echo "Download remote config fail."
    fi
  fi
}

init_conf
up_conf

# start redis manager
CLASSPATH=conf/:web/
for i in lib/*.jar; do
        CLASSPATH=${CLASSPATH}:$i
done
for i in *.jar; do
        CLASSPATH=${CLASSPATH}:$i
done
echo ${CLASSPATH}
java -cp ${CLASSPATH} com.newegg.ec.cache.Application