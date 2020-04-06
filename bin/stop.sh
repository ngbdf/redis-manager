#!/bin/bash
set encoding=utf-8
project_path=$(cd `dirname $0`; pwd)
cd $project_path
cd ../

kill -5 `cat pid`
rm -f pid
