FROM centos7

MAINTAINER george.z.lin george.z.lin@newegg.com
RUN mkdir -p /data/redis/
RUN mkdir -p /redis/
ADD redis-4.0.8.tar /redis/
WORKDIR /redis/redis-4.0.8/
ENTRYPOINT ["./start.sh"]
