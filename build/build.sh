#!/bin/bash
project_path=$(cd `dirname $0`; pwd)
cd $project_path
ls | grep -v '\.sh' | xargs rm -rf
mkdir conf/
mkdir web/
mkdir logs/
cd ../redis-manager-dashboard
mvn clean package -Dmaven.test.skip=true
mv -f target/classes/data/ $project_path/
mv -f target/classes/application.yml $project_path/conf/
mv -f target/classes/log4j2.xml $project_path/conf/
rm -rf target/classes/com/
mv -f target/classes/* $project_path/web/
mv -f target/lib/ $project_path/
cp target/*.jar $project_path/
cp -rf ../bin/ $project_path/
