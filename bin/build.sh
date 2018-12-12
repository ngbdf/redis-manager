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
echo "please update the config."



