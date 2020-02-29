
FROM alpine/git
WORKDIR /app
RUN git clone https://github.com/ngbdf/redis-manager.git

FROM maven:3.5-jdk-8-alpine
COPY --from=0 /app/redis-manager /app/redis-manager 
WORKDIR /app/redis-manager
RUN mvn package -Dmaven.test.skip=true -f /app/redis-manager/redis-manager-dashboard \
    && mv /app/redis-manager/redis-manager-dashboard/target/redis-manager-dashboard-*.tar.gz /app/redis-manager.tar.gz \
    && rm -rf /app/redis-manager/* \
    && mv /app/redis-manager.tar.gz /app/redis-manager/redis-manager.tar.gz \
    && tar -xf redis-manager.tar.gz \
    && rm redis-manager.tar.gz \
    && cp conf/application.yml conf/application.yml.backup \
    && cp -r data backup_data \
    && rm -f conf/log4j2.xml 

FROM openjdk:8-jre-alpine
COPY --from=1 /app/redis-manager /app/redis-manager
COPY redis-manager-start.sh /app/redis-manager
COPY log4j2.xml /app/redis-manager

ENTRYPOINT ["sh","./redis-manager-start.sh"]
