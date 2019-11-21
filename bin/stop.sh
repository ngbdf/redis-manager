#!/bin/bash
set encoding=utf-8
project_path=$(cd `dirname $0`; pwd)
cd $project_path
kill -5 `cat pid`