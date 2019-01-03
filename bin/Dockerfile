FROM openjdk:8u181-jdk-alpine3.8
LABEL author="Reason.H.Duan"
COPY redis-manager.tar /opt/app/
WORKDIR /opt/app
RUN tar -xvf redis-manager.tar ./
RUN rm redis-manager.tar
ENTRYPOINT ["sh","./bin/start.sh"]
