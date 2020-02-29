FROM alpine/git
WORKDIR /app
RUN git clone https://github.com/ngbdf/redis-manager.git 

FROM maven:3.5-jdk-8-alpine
COPY --from=0 /app/redis-manager /app/redis-manager 
WORKDIR /app/redis-manager
RUN mvn package -Dmaven.test.skip=true -f ./redis-manager-dashboard \
    && mv ./redis-manager-dashboard/target/redis-manager-dashboard-*.tar.gz /app/redis-manager.tar.gz \
    && mv ./docker /app/docker \
    && rm -rf /app/redis-manager/* \
    && mv /app/redis-manager.tar.gz ./redis-manager.tar.gz \
    && tar -xf redis-manager.tar.gz \
    && rm redis-manager.tar.gz \
    && mv /app/docker/redis-manager-start.sh /app/redis-manager \
    && mv /app/docker/log4j2.xml /app/redis-manager/conf \
    && cp conf/application.yml conf/application.yml.backup \
    && cp -r data backup_data \
    && rm -f conf/log4j2.xml 

FROM openjdk:8-jre-alpine
WORKDIR /app/redis-manager
COPY --from=1 /app/redis-manager /app/redis-manager

ENTRYPOINT ["sh","./redis-manager-start.sh"]
