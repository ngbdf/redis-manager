FROM maven:3.6.0-jdk-8-alpine

RUN apk add --no-cache unzip

ARG SERVICE_DIR=/usr/share/redis-manager
ARG REMOTE_URL=https://github.com/ngbdf/redis-manager/archive/redismanager-1.1-release.zip

RUN curl -fsSL -o /tmp/redis-manager.zip ${REMOTE_URL} \
  && unzip /tmp/redis-manager.zip -d /tmp \
  && rm -f /tmp/redis-manager.zip && mv /tmp/redis-manager* /tmp/redis-manager \
  && mvn clean package -Dmaven.test.skip=true -f /tmp/redis-manager \
  && mkdir ${SERVICE_DIR} && mkdir ${SERVICE_DIR}/logs && mkdir ${SERVICE_DIR}/web \
  && mkdir ${SERVICE_DIR}/conf && mkdir ${SERVICE_DIR}/lib \
  && cp -f /tmp/redis-manager/target/redis-manager*.jar ${SERVICE_DIR} \
  && cp -rf /tmp/redis-manager/target/lib/* ${SERVICE_DIR}/lib \
  && cp -rf /tmp/redis-manager/target/classes/* ${SERVICE_DIR}/web \
  && mv ${SERVICE_DIR}/web/application.yml ${SERVICE_DIR}/web/application.yml.base \
  && rm -rf /tmp/redis-manager*

COPY redis-manager-start.sh ${SERVICE_DIR}/

WORKDIR ${SERVICE_DIR}
ENTRYPOINT ["sh","./redis-manager-start.sh"]
