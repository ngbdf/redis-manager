#!/bin/bash
mvn clean package -Dmaven.test.skip=true
rm -rf build
mkdir build
cd ./build
mkdir logs
mkdir conf
cp -f ../target/redis-manager*.jar ./
cp -rf ../target/lib ./
cp -rf ../bin ./
cp -rf ../target/classes/* ./conf
mv ./conf/application.yml ./conf/application.yml.base
tar -cvf redis-manager.tar ./
mv ./bin/Dockerfile ./
docker build -t redis-manager:1.0.0 .
echo "please update the config."



