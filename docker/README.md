# Redis manager
[GitHub](https://github.com/ngbdf/redis-manager/tree/docker)|[码云](https://gitee.com/newegg/redis-manager)
## Start a redis-manager

```
sudo docker run -d --net=host --name redis-manager  \
-v /opt/app/redis-manager-test/conf:/usr/share/redis-manager/conf \
reasonduan/redis-manager
```

### Update config by environment variables
 If you don't need to modify too much configuration information, you can modify the configuration file by configuring environment variables.
```sh
$sudo docker run -d --net=host --name redis-manager  \
-e MYSQL_URL='jdbc:mysql://127.0.0.1:3306/redis_manager?useUnicode=true&characterEncoding=utf-8' \
-e MYSQL_USER='root' \
-e MYSQL_PWD='******' \
reasonduan/redis-manager
```
### Update config by remote file
If you need to change a lot of configuration information, you can update your configuration by configuring a remote configuration file.
```sh
$sudo docker run -d --net=host --name redis-manager  \
-e CONFIG_URL='http://127.0.0.1/config/raw/master/redis_manager/application.yml' \
reasonduan/redis-manager
```