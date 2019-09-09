<template>
  <div id="monitor">
    <el-row>
      <el-col :span="24">
        <div class="monitor-title">
          <span>
            <span class="cluster-name">Shanghai</span>
            <i class="el-icon-sunny health"></i>
          </span>
          <el-button size="mini" type="primary" title="Query" icon="el-icon-search" circle></el-button>
        </div>
      </el-col>
    </el-row>
    <div class="base-info-wrapper">
      <el-row class="base-info">
        <el-col :xl="6" :lg="8" :md="12" :sm="12">
          <div class="base-info-item">
            Mode:
            <b>cluster</b>
          </div>
        </el-col>
        <el-col :xl="6" :lg="8" :md="12" :sm="12">
          <div class="base-info-item">
            Version:
            <b>4.0.10</b>
          </div>
        </el-col>
        <el-col :xl="6" :lg="8" :md="12" :sm="12">
          <div class="base-info-item">
            Master:
            <b>2</b>
          </div>
        </el-col>
        <el-col :xl="6" :lg="8" :md="12" :sm="12">
          <div class="base-info-item">
            Node:
            <b>12</b>
          </div>
        </el-col>
        <el-col :xl="6" :lg="8" :md="12" :sm="12">
          <div class="base-info-item">
            Totol Keys:
            <b>4648488</b>
          </div>
        </el-col>
        <el-col :xl="6" :lg="8" :md="12" :sm="12">
          <div class="base-info-item">
            Expire Keys:
            <b>488</b>
          </div>
        </el-col>
      </el-row>
    </div>
    <div class="monitor-condition-wrapper">
      <div class="condition-wrapper">
        <el-select
          v-model="currentNode"
          filterable
          size="small"
          placeholder="All"
          class="condition-item"
        >
          <el-option
            v-for="node in nodeList"
            :key="node.label"
            :label="node.label"
            :value="node.value"
          ></el-option>
        </el-select>
        <el-select
          v-model="currentDataType"
          size="small"
          placeholder="Data Type"
          class="condition-item"
        >
          <el-option
            v-for="dataType in dataTypeList"
            :key="dataType.label"
            :label="dataType.label"
            :value="dataType.value"
          ></el-option>
        </el-select>

        <el-select
          v-model="currentTimeType"
          size="small"
          placeholder="Time Type"
          class="condition-item"
        >
          <el-option
            v-for="timeType in timeTypeList"
            :key="timeType.label"
            :label="timeType.label"
            :value="timeType.value"
          ></el-option>
        </el-select>
        <div class="time-picker-wrapper">
          <el-date-picker
            v-model="timeRange"
            size="small"
            type="datetimerange"
            :picker-options="pickerOptions"
            range-separator="-"
            start-placeholder="Start Time"
            end-placeholder="End Time"
            align="right"
          ></el-date-picker>
        </div>
      </div>
      <el-row class="echart-wrapper">
        <el-col :span="12" >
          <div id="redis-memory" style="width:100%; height:600px; border: 1px solid #000"></div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
// vue文件中引入echarts工具
var echarts = require("echarts/lib/echarts");
require("echarts/lib/chart/line");
// 以下的组件按需引入
require("echarts/lib/component/tooltip"); // tooltip组件
require("echarts/lib/component/title"); //  title组件
require("echarts/lib/component/legend"); // legend组件

export default {
  data() {
    return {
      nodeList: [
        {
          value: "192.168.5.12:8651",
          label: "192.168.5.12:8651 slave"
        },
        {
          value: "192.168.5.12:8652",
          label: "192.168.5.12:8652 slave"
        },
        {
          value: "192.168.5.12:8653",
          label: "192.168.5.12:8653 master"
        },
        {
          value: "192.168.5.12:8654",
          label: "192.168.5.12:8654 master"
        }
      ],
      dataTypeList: [
        {
          value: "AVG",
          label: "AVG"
        },
        {
          value: "MAX",
          label: "MAX"
        },
        {
          value: "MIN",
          label: "MIN"
        }
      ],
      timeTypeList: [
        {
          value: "MINUTE",
          label: "MINUTE"
        },
        {
          value: "HOUR",
          label: "HOUR"
        }
      ],
      currentNode: "",
      currentDataType: "",
      currentTimeType: "",
      pickerOptions: {
        shortcuts: [
          {
            text: "Last Week",
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
              picker.$emit("pick", [start, end]);
            }
          },
          {
            text: "Last Mooth",
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
              picker.$emit("pick", [start, end]);
            }
          }
        ]
      },
      timeRange: ""
    };
  },
  methods: {
    initChart() {
        console.log("==========")
        console.log(echarts)
      var redisMemory = echarts.init(document.getElementById("redis-memory"));
      // 绘制图表
      redisMemory.setOption({
        title: {
          text: "Redis Memory"
        },
        tooltip: {},
        xAxis: {
          data: ["衬衫", "羊毛衫", "雪纺衫", "裤子", "高跟鞋", "袜子"]
        },
        yAxis: {},
        series: [
          {
            name: "销量",
            type: "line",
            data: [5, 20, 36, 10, 10, 20]
          }
        ]
      });
    }
  },
  mounted() {
    this.initChart();
  }
};
</script>

<style scoped>
.monitor-title {
  display: flex;
  justify-content: space-between;
  background-color: #ffffff;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #dcdfe6;
}

.cluster-name,
.health {
  font-size: 18px;
  font-weight: bold;
}

.health {
  color: #40c9c6;
  margin-left: 10px;
}

.base-info-wrapper,
.monitor-condition-wrapper {
  background-color: #ffffff;
  padding: 20px;
  border-bottom: 1px solid #dcdfe6;
  font-size: 14px;
}

.base-info {
  width: 50%;
}

.base-info-item {
  padding-bottom: 10px;
}

.condition-wrapper {
  display: flex;
  justify-content: flex-start;
  align-items: center;
}

.condition-item {
  margin-right: 20px;
}

.time-picker-wrapper {
  float: right;
}

.echart-wrapper {
  margin-top: 20px;
}
</style>