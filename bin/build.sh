#!/bin/bash
cd ./bin/
ls | grep -v '\.sh' | xargs rm -rf
mkdir conf/
mkdir web/
mkdir logs/
cd ../redis-manager-dashboard
mvn clean package -Dmaven.test.skip=true
mv -f target/classes/data/ ../bin/
mv -f target/classes/application.yml ../bin/conf/
mv -f target/classes/log4j2.xml ../bin/conf/
rm -rf target/classes/com/
mv -f target/classes/* ../bin/web/
mv -f target/lib/ ../bin/
cp target/*.jar ../bin/
