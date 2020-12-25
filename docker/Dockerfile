FROM alpine/git as clone
WORKDIR /app
RUN git clone https://github.com/ngbdf/redis-manager.git 

FROM node:10.16.0-alpine as front_build
COPY --from=clone /app/redis-manager /app/redis-manager
WORKDIR /app/redis-manager/redis-manager-ui/redis-manager-vue
RUN npm install \
    && npm run build

FROM maven:3.5-jdk-8-alpine as dashboard_package
COPY --from=front_build /app/redis-manager /app/redis-manager
WORKDIR /app/redis-manager
RUN rm -rf /app/redis-manager/redis-manager-dashboard/src/main/resources/templates/* \
    && rm -rf /app/redis-manager/redis-manager-dashboard/src/main/resources/static/* \
    && mv /app/redis-manager/redis-manager-ui/redis-manager-vue/dist/static/* /app/redis-manager/redis-manager-dashboard/src/main/resources/static/ \
    && mv /app/redis-manager/redis-manager-ui/redis-manager-vue/dist/index.html /app/redis-manager/redis-manager-dashboard/src/main/resources/templates/ \
    && mvn package -Dmaven.test.skip=true -f ./redis-manager-dashboard \
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
    && rm -f conf/log4j2.xml \
    && mv /app/redis-manager/lib /app/lib

FROM openjdk:8-jre-alpine
WORKDIR /app/redis-manager

COPY --from=dashboard_package /app/redis-manager /app/redis-manager
COPY --from=dashboard_package /app/lib /app/redis-manager/lib

ENTRYPOINT ["sh","./redis-manager-start.sh"]
