#!/bin/bash
cd ./build/
ls | grep -v '\.sh' | xargs rm -rf
mkdir conf/
mkdir web/
mkdir logs/
cd ../redis-manager-dashboard
mvn clean package -Dmaven.test.skip=true
mv -f target/classes/data/ ../build/
mv -f target/classes/application.yml ../build/conf/
mv -f target/classes/log4j2.xml ../build/conf/
rm -rf target/classes/com/
mv -f target/classes/* ../build/web/
mv -f target/lib/ ../build/
cp target/*.jar ../build/
