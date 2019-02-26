#!/bin/bash
set -e

package_url="$1"
package="$2"
port=$3
install_tag="$4"

# 检测目录
if [ ! -d "${install_tag}" ]; then
        mkdir -p ${install_tag}
fi

if [ "${port}" != "0" ]; then
    install_path="${install_tag}/${port}"
    mkdir -p ${install_path}
fi

# 进入到安装目录
PWD="${PWD}/${install_path}"
cd ${PWD}
echo "cd ${PWD} ..."

# 下载安装包
echo "start wget ing..............."
wget -q "${package_url}/${package}"

# 解压操作
echo "start tar ing.........";
if [ -f "${package}" ]; then
    tar -xvf ${package} --strip-components 1
    rm ${package}
fi

# 执行安装包内部 start.sh 脚本
start_file="start.sh";
echo "will bash ${start_file} starting................"
if [ -f "${start_file}" ]; then
    bash ${start_file} ${port}
fi
echo "########################## finish #########################"