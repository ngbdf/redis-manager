FROM maven:3.6.0-jdk-8-alpine

RUN apk add --no-cache unzip

ARG BRANCH=docker
ARG REMOTE_URL=https://gitee.com/newegg/redis-manager/archive/docker.zip
ARG SERVICE_DIR=/usr/share/redis-manager

RUN curl -fsSL -o /tmp/redis-manager.tar.zip ${REMOTE_URL} \
  && unzip /tmp/redis-manager.tar.zip -d /tmp/
WORKDIR /tmp/redis-manager
RUN mkdir build \
  && mvn clean package -Dmaven.test.skip=true
WORKDIR /tmp/redis-manager/build
RUN mkdir logs \
  && mkdir conf \
  && cp -f /tmp/redis-manager/target/redis-manager*.jar ${SERVICE_DIR} \
  && cp -rf /tmp/redis-manager/target/lib ${SERVICE_DIR} \
  && cp -rf /tmp/redis-manager/docker/start.sh ${SERVICE_DIR} \
  && cp -rf /tmp/redis-manager/target/classes/* ${SERVICE_DIR}/conf \
  && mv ${SERVICE_DIR}/conf/application.yml ${SERVICE_DIR}/conf/application.yml.base
  && rm -rf /tmp/redis-manager*
WORKDIR ${SERVICE_DIR}
ENTRYPOINT ["sh","./start.sh"]
