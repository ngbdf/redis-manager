<template>
  <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item" v-loading="monitorDataLoading">
    <el-card shadow="hover" class="box-card">
      <div :id="infoItem" class="chart" :class="{ 'chart-no-data' : noNodeInfoData }"></div>
    </el-card>
  </el-col>
</template>

<script>
var elementResizeDetectorMaker = require("element-resize-detector");
// vue文件中引入echarts工具
var echarts = require("echarts/lib/echarts");
require("echarts/lib/chart/line");
// 以下的组件按需引入
require("echarts/lib/component/tooltip"); // tooltip组件
require("echarts/lib/component/title"); //  title组件
require("echarts/lib/component/legend"); // legend组件
import "echarts/lib/component/legendScroll";
import API from "@/api/api.js";
import { isEmpty } from "@/utils/validate.js";
import { formatTimeForChart } from "@/utils/time.js";
import message from "@/utils/message.js";
export default {
  props: {
    nodeInfoParam: {},
    infoItem: ""
  },
  data() {
    return {
      echartsData: [],
      echartsItemDataxAxis: [],
      noNodeInfoData: true,
      lineColor: [
        "#3888fa",
        "#FF005A",
        "#ffb980",
        "#40c9c6",
        "#b6a2de",
        "#d87a80",
        "#34bfa3",
        "#5ab1ef",
        "#3888fa",
        "#ffba00",
        "#c9c0cb",
        "#FF005A",
        "#3888fa",
        "#FF005A",
        "#ffb980",
        "#40c9c6"
      ],
      areaStyleColor: [
        "rgba(56, 136, 250, 0.1)",
        "rgba(250, 0, 90, 0.1)",
        "rgba(64, 201, 198, 0.1)",
        "rgba(255, 185, 128, 0.1)"
      ],
      monitorDataLoading: false
    };
  },
  methods: {
    initCharts(nodeInfoDataList) {
      this.buildEchartsData(nodeInfoDataList);
      let series = this.buildSeries(this.echartsData);
      var erd = elementResizeDetectorMaker();
      var start = new Date().getTime();
      if (isEmpty(this)) {
        return;
      }
      let infoItemObj = document.getElementById(this.infoItem);
      if (isEmpty(infoItemObj)) {
        return;
      }
      var chart = echarts.init(infoItemObj);

      // 绘制图表
      chart.clear();
      chart = echarts.init(infoItemObj);
      let title = this.infoItem;
      if (
        title == "used_memory" ||
        title == "used_memory_rss" ||
        title == "used_memory_overhead" ||
        title == "used_memory_dataset"
      ) {
        title += "(MB)";
      }

      chart.setOption({
        title: {
          text: title,
          left: 35
        },
        legend: {
          type: "scroll",
          bottom: 0
        },
        // 图表位置
        grid: {
          left: 20,
          right: 20,
          bottom: 50,
          top: 50,
          containLabel: true
        },
        tooltip: {
          trigger: "axis",
          axisPointer: {
            type: "cross"
          },
          padding: [5, 10],
          position: function(pos, params, dom, rect, size) {
            var obj = { top: -10 };
            obj[["left", "right"][+(pos[0] < size.viewSize[0] / 2)]] = 5;
            return obj;
          }
        },
        toolbox: {
          feature: {
            saveAsImage: {}
          }
        },
        xAxis: {
          data: this.echartsItemDataxAxis
        },
        yAxis: {
          axisTick: {
            show: true
          }
        },
        series: series
      });
      // 监听容器宽度变化
      erd.listenTo(document.getElementById(this.infoItem), function(element) {
        setTimeout(function() {
          chart.resize();
        }, 0);
      });
      // 监听窗口变化
      window.addEventListener("resize", function() {
        setTimeout(function() {
          chart.resize();
        }, 0);
      });
    },
    getNodeInfoDataList(nodeInfoItemParam) {
      this.monitorDataLoading = true;
      let url = "/monitor/getInfoItemMonitorData";
      API.post(
        url,
        nodeInfoItemParam,
        response => {
          let result = response.data;
          if (result.code == 0) {
            let nodeInfoDataList = result.data;
            this.noNodeInfoData = false;
            // init data for echarts
            this.initCharts(nodeInfoDataList);
          } else {
            message.error("Get node info list failed");
          }
          this.monitorDataLoading = false;
        },
        err => {
          this.monitorDataLoading = false;
          message.error(err);
        }
      );
    },
    buildSeries(echartsData) {
      let size = echartsData.length;
      if (size == 0) {
        return;
      }
      let series = [];
      for (var i = 0; i < size; i++) {
        var color = this.lineColor[i % size];
        var areaStyleColor = this.areaStyleColor[i % size];
        let oneNodeData = echartsData[i];
        series.push({
          name: oneNodeData.name,
          itemStyle: {
            normal: {
              color: color,
              lineStyle: {
                color: color,
                width: 1.5
              }
            }
          },
          smooth: true,
          type: "line",
          data: oneNodeData.data,
          animationDuration: 2800,
          animationEasing: "cubicInOut",
          symbol: "none"
          // 背景色
          //areaStyle: {}
        });
      }
      return series;
    },
    buildEchartsData(nodeInfoDataList) {
      this.echartsData = [];
      this.echartsItemDataxAxis = [];
      let infoItem = this.infoItem;
      nodeInfoDataList.forEach(oneNodeInfoList => {
        let firstNode = oneNodeInfoList[0];
        let oneNodeData = {
          name: firstNode.node + " " + firstNode.role.toLowerCase(),
          data: []
        };
        let buildXAxis = this.echartsItemDataxAxis.length == 0;
        oneNodeInfoList.forEach(nodeInfo => {
          if (buildXAxis) {
            this.echartsItemDataxAxis.push(
              formatTimeForChart(nodeInfo.updateTime)
            );
          }
          for (let field in nodeInfo) {
            if (
              infoItem
                .replace(/[^a-zA-Z]/g, "", /\s/g, "", /[0-9]/g, "")
                .toLowerCase() == field.toLowerCase()
            ) {
              oneNodeData.data.push(nodeInfo[field]);
            }
          }
        });
        this.echartsData.push(oneNodeData);
      });
    }
  },
  watch: {
    // 深度监听 nodeInfoParam 变化
    nodeInfoParam: {
      handler: function() {
        let param = JSON.parse(JSON.stringify(this.nodeInfoParam));
        param.infoItem = this.infoItem;
        this.getNodeInfoDataList(param);
      },
      deep: true
    }
  }
};
</script>

<style scoped>
.box-card {
  margin: 5px;
}

.chart {
  min-height: 400px;
  width: 100%;
}

.chart-no-data {
  height: 0 !important;
}
</style>