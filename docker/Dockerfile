FROM maven:3.6.0-jdk-8-alpine

RUN apk add --no-cache unzip

ARG REMOTE_URL=https://github.com/ngbdf/redis-manager/archive/dev_build.zip
ARG SERVICE_DIR=/usr/share/redis-manger
ARG BRANCH=dev_build

RUN curl -fsSL -o /tmp/redis-manager.tar.zip ${REMOTE_URL} \
  && unzip /tmp/redis-manager.tar.zip -d /tmp/ \
  && mv /tmp/redis-manger-${BRANCH} ${SERVICE_DIR} \
  && rm -rf /tmp/redis-manager*
WORKDIR ${SERVICE_DIR}
RUN mkdir build \
  && mvn clean package -Dmaven.test.skip=true
WORKDIR ${SERVICE_DIR}/build
RUN mkdir logs \
  && mkdir conf \
  && cp -f ${SERVICE_DIR}/target/redis-manager*.jar ${SERVICE_DIR}/build \
  && cp -rf ${SERVICE_DIR}/build/target/lib ${SERVICE_DIR}/build \
  && cp -rf ${SERVICE_DIR}/build/docker/start.sh ${SERVICE_DIR}/build \
  && cp -rf ${SERVICE_DIR}/build/target/classes/* ${SERVICE_DIR}/build/conf \
  && mv ${SERVICE_DIR}/build/conf/application.yml ${SERVICE_DIR}/build/conf/application.yml.base
ENTRYPOINT ["sh","./start.sh"]
