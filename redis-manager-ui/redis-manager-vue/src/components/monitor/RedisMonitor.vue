<template>
  <div id="monitor">
    <div class="body-wrapper" v-loading="clusterLoading">
      <el-row>
        <el-col :span="24">
          <div class="monitor-title">
            <span>
              <span class="cluster-name">{{ cluster.clusterName }}</span>
              <i class="el-icon-sunny health" v-if="cluster.clusterState == 'HEALTH'"></i>
            </span>
            <div>
              <el-button
                size="mini"
                type="primary"
                title="Query"
                icon="el-icon-search"
                @click="handleQuery(cluster.clusterId)"
              >Query</el-button>
              <el-button
                size="mini"
                type="warning"
                icon="el-icon-ali-slow"
                title="Slow log"
                @click="slowLogVisible = true"
              >Slow Log</el-button>
            </div>
          </div>
        </el-col>
      </el-row>
      <div class="base-info-wrapper">
        <el-row class="base-info">
          <el-col :xl="3" :lg="4" :md="6" :sm="8">
            <div class="base-info-item">
              Mode:
              <el-tag size="mini">{{ cluster.redisMode }}</el-tag>
            </div>
          </el-col>
          <el-col :xl="3" :lg="4" :md="6" :sm="8">
            <div class="base-info-item">
              Version:
              <el-tag size="mini">{{ cluster.redisVersion }}</el-tag>
            </div>
          </el-col>
          <el-col :xl="3" :lg="4" :md="6" :sm="8">
            <div class="base-info-item">
              Master:
              <el-tag size="mini">{{ cluster.clusterSize }}</el-tag>
            </div>
          </el-col>
          <el-col :xl="3" :lg="4" :md="6" :sm="8">
            <div class="base-info-item">
              Node:
              <el-tag size="mini">{{ cluster.clusterKnownNodes }}</el-tag>
            </div>
          </el-col>
          <el-col :xl="3" :lg="4" :md="6" :sm="8">
            <div class="base-info-item">
              Total Keys:
              <el-tag size="mini">{{ cluster.totalKeys }}</el-tag>
            </div>
          </el-col>
          <el-col :xl="3" :lg="4" :md="6" :sm="8">
            <div class="base-info-item">
              Total Expires:
              <el-tag size="mini">{{ cluster.totalExpires }}</el-tag>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
    <div style="margin-top: 20px;" v-loading="monitorDataLoading">
      <div class="monitor-condition-wrapper">
        <div class="condition-wrapper">
          <el-select
            v-model="nodes"
            filterable
            size="small"
            placeholder="Node"
            class="condition-item"
            multiple
            collapse-tags
          >
            <el-option
              v-for="node in redisNodeList"
              :key="node.label"
              :label="node.label"
              :value="node.value"
            ></el-option>
          </el-select>

          <el-select
            v-model="nodeInfoParam.timeType"
            size="small"
            placeholder="Time Type"
            class="condition-item"
            style="width: 100px"
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
              width="1080px"
              v-model="nodeInfoParam.timeRange"
              size="small"
              type="datetimerange"
              :picker-options="pickerOptions"
              range-separator="-"
              start-placeholder="Start Time"
              end-placeholder="End Time"
              align="right"
              :editable="false"
              value-format="timestamp"
              @change="pickerDateTime"
            ></el-date-picker>
          </div>
        </div>
        <i class="el-icon-refresh-left refresh" @click="getNodeInfoDataList(nodeInfoParam)"></i>
      </div>
      <el-row class="echart-wrapper" id="monitor-charts">
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div id="used_memory" class="chart" :class="{ 'chart-no-data' : noNodeInfoData }"></div>
          </el-card>
        </el-col>
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div id="used_memory_rss" class="chart" :class="{ 'chart-no-data' : noNodeInfoData }"></div>
          </el-card>
        </el-col>
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div
              id="used_memory_overhead"
              class="chart"
              :class="{ 'chart-no-data' : noNodeInfoData }"
            ></div>
          </el-card>
        </el-col>
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div
              id="used_memory_dataset"
              class="chart"
              :class="{ 'chart-no-data' : noNodeInfoData }"
            ></div>
          </el-card>
        </el-col>
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div
              id="mem_fragmentation_ratio"
              class="chart"
              :class="{ 'chart-no-data' : noNodeInfoData }"
            ></div>
          </el-card>
        </el-col>
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div
              id="connections_received"
              class="chart"
              :class="{ 'chart-no-data' : noNodeInfoData }"
            ></div>
          </el-card>
        </el-col>
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div
              id="rejected_connections"
              class="chart"
              :class="{ 'chart-no-data' : noNodeInfoData }"
            ></div>
          </el-card>
        </el-col>
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div id="connected_clients" class="chart" :class="{ 'chart-no-data' : noNodeInfoData }"></div>
          </el-card>
        </el-col>
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div id="blocked_clients" class="chart" :class="{ 'chart-no-data' : noNodeInfoData }"></div>
          </el-card>
        </el-col>
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div
              id="commands_processed"
              class="chart"
              :class="{ 'chart-no-data' : noNodeInfoData }"
            ></div>
          </el-card>
        </el-col>
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div
              id="instantaneous_ops_per_sec"
              class="chart"
              :class="{ 'chart-no-data' : noNodeInfoData }"
            ></div>
          </el-card>
        </el-col>
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div id="sync_full" class="chart" :class="{ 'chart-no-data' : noNodeInfoData }"></div>
          </el-card>
        </el-col>
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div id="sync_partial_ok" class="chart" :class="{ 'chart-no-data' : noNodeInfoData }"></div>
          </el-card>
        </el-col>
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div id="sync_partial_err" class="chart" :class="{ 'chart-no-data' : noNodeInfoData }"></div>
          </el-card>
        </el-col>
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div
              id="keyspace_hits_ratio"
              class="chart"
              :class="{ 'chart-no-data' : noNodeInfoData }"
            ></div>
          </el-card>
        </el-col>
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div id="keys" class="chart" :class="{ 'chart-no-data' : noNodeInfoData }"></div>
          </el-card>
        </el-col>
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div id="expires" class="chart" :class="{ 'chart-no-data' : noNodeInfoData }"></div>
          </el-card>
        </el-col>
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div id="cpu_sys" class="chart" :class="{ 'chart-no-data' : noNodeInfoData }"></div>
          </el-card>
        </el-col>
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div id="cpu_user" class="chart" :class="{ 'chart-no-data' : noNodeInfoData }"></div>
          </el-card>
        </el-col>
      </el-row>
      <!-- detail monitor -->
    </div>
    <el-dialog title="Query" :visible.sync="queryVisible" :close-on-click-modal="false" width="60%">
      <query :clusterId="cluster.clusterId"></query>
    </el-dialog>

    <el-dialog
      title="Slow Log"
      :visible.sync="slowLogVisible"
      :close-on-click-modal="false"
      width="80%"
    >
      <div v-loading="slowLogLoading">
        <el-select
          v-model="slowLogParam.node"
          filterable
          size="small"
          placeholder="Please select node"
          class="condition-item"
          style="margin-bottom: 20px;"
        >
          <el-option
            v-for="node in redisNodeList"
            :key="node.label"
            :label="node.label"
            :value="node.value"
          ></el-option>
        </el-select>
        <el-table :data="slowLogList" :default-sort="{prop: 'dateTime', order: 'descending'}">
          <el-table-column type="index" width="50"></el-table-column>
          <el-table-column prop="node" label="Node" sortable></el-table-column>
          <el-table-column prop="type" label="Type"></el-table-column>
          <el-table-column prop="command" label="Command"></el-table-column>
          <el-table-column prop="executionTime" label="Execution Time(μs)" sortable></el-table-column>
          <el-table-column prop="dateTime" label="Date Time" sortable></el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
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
import query from "@/components/tool/Query";
import API from "@/api/api.js";
import { formatTime, formatTimeForChart } from "@/utils/time.js";
import { isEmpty } from "@/utils/validate.js";
import { store } from "@/vuex/store.js";
import { getClusterById } from "@/components/cluster/cluster.js";
import message from "@/utils/message.js";
export default {
  components: {
    query
  },
  data() {
    return {
      queryVisible: false,
      cluster: {},
      redisNodeList: [],
      timeTypeList: [
        {
          value: 0,
          label: "Minute"
        },
        {
          value: 1,
          label: "Hour"
        }
      ],
      pickerOptions: {
        shortcuts: [
          {
            text: "Last 15 Min",
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 900 * 1000);
              picker.$emit("pick", [start, end]);
            }
          },
          {
            text: "Last 30 Min",
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 1800 * 1000);
              picker.$emit("pick", [start, end]);
            }
          },
          {
            text: "Last 1 Hour",
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000);
              picker.$emit("pick", [start, end]);
            }
          },
          {
            text: "Last 3 Hours",
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 3);
              picker.$emit("pick", [start, end]);
            }
          },
          {
            text: "Last 6 Hours",
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 6);
              picker.$emit("pick", [start, end]);
            }
          },
          {
            text: "Last 12 Hours",
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 12);
              picker.$emit("pick", [start, end]);
            }
          },
          {
            text: "Last 1 Day",
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24);
              picker.$emit("pick", [start, end]);
            }
          },
          {
            text: "Last 2 Days",
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 2);
              picker.$emit("pick", [start, end]);
            }
          },
          {
            text: "Last 3 Days",
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 3);
              picker.$emit("pick", [start, end]);
            }
          },
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
      nodeInfoParam: {
        nodeList: [],
        timeType: 0,
        timeRange: [new Date() - 15 * 60 * 1000, new Date()]
      },
      nodeInfoDataList: [],
      nodes: [],
      noNodeInfoData: true,
      xAxis: [],
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
      slowLogVisible: false,
      slowLogList: [],
      timer: 0,
      slowLogParam: {},
      slowLogList: [],
      clusterLoading: false,
      monitorDataLoading: false,
      slowLogLoading: false
    };
  },
  methods: {
    handleQuery(clusterId) {
      this.queryVisible = true;
    },
    pickerDateTime() {
      let nodeInfoParam = this.nodeInfoParam;
      let timeRange = nodeInfoParam.timeRange;
      let startTime = timeRange[0];
      let endTime = timeRange[1];
      nodeInfoParam.startTime = startTime;
      nodeInfoParam.endTime = endTime;
      this.nodeInfoParam = nodeInfoParam;
    },
    getAllNodeList(clusterId) {
      let url = "/nodeManage/getAllNodeList/" + clusterId;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            let redisNodeList = [];
            result.data.forEach(node => {
              var hostAndPort = node.host + ":" + node.port;
              var role = node.nodeRole.toLowerCase();
              redisNodeList.push({
                value: hostAndPort,
                label: hostAndPort + " " + role
              });
            });
            this.redisNodeList = redisNodeList;
          } else {
            message.error(result.message);
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    getNodeInfoDataList(nodeInfoParam) {
      this.monitorDataLoading = true;
      let url = "/monitor/getNodeInfoDataList";
      API.post(
        url,
        nodeInfoParam,
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.nodeInfoDataList = result.data;
            this.noNodeInfoData = false;
            // init data for echarts
            this.xAxis = [];
            this.initCharts(this.nodeInfoDataList);
          } else {
            message.error("Get node info list failed");
          }
          this.monitorDataLoading = false;
        },
        err => {
          this.monitorDataLoading = false;
          console.log(err);
          message.error(err);
        }
      );
    },
    initCharts(nodeInfoDataList) {
      var erd = elementResizeDetectorMaker();
      let echartsList = this.buildNodeData(nodeInfoDataList);
      echartsList.forEach(echartsData => {
        var chart = echarts.init(document.getElementById(echartsData.id));
        // 绘制图表
        chart.dispose();
        chart = echarts.init(document.getElementById(echartsData.id));
        let series = this.buildSeries(echartsData.series);
        let id = echartsData.id;
        if (
          id == "used_memory" ||
          id == "used_memory_rss" ||
          id == "used_memory_overhead" ||
          id == "used_memory_dataset"
        ) {
          id += "(MB)";
        }
        chart.setOption({
          title: {
            text: id,
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
            padding: [5, 10]
          },
          toolbox: {
            feature: {
              saveAsImage: {}
            }
          },
          xAxis: {
            data: this.xAxis
          },
          yAxis: {
            axisTick: {
              show: true
            }
          },
          series: series
        });
        // 监听容器宽度变化
        erd.listenTo(document.getElementById("monitor-charts"), function(
          element
        ) {
          setTimeout(function() {
            chart.resize();
          }, 100);
        });
        // 监听窗口变化
        window.addEventListener("resize", function() {
          setTimeout(function() {
            chart.resize();
          }, 100);
        });
      });
    },
    buildSeries(series) {
      let nodeSize = series.length;
      if (nodeSize == 0) {
        return;
      }
      let newSeries = [];
      for (var i = 0; i < nodeSize; i++) {
        var color = this.lineColor[i % nodeSize];
        var areaStyleColor = this.areaStyleColor[i % nodeSize];
        let oneNode = series[i];
        newSeries.push({
          name: oneNode.name,
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
          data: oneNode.data,
          animationDuration: 2800,
          animationEasing: "cubicInOut"
          // 背景色
          // areaStyle: {}
        });
      }
      return newSeries;
    },
    buildNodeData(nodeInfoDataList) {
      let nodeInfoList = [];
      nodeInfoDataList.forEach(oneNodeInfoList => {
        let oneNodeInfoData = {
          node: oneNodeInfoList[0].node + " " + oneNodeInfoList[0].role.toLowerCase(),
          usedMemory: [],
          usedMemoryRss: [],
          usedMemoryOverhead: [],
          usedMemoryDataset: [],
          memFragmentationRatio: [],
          connectionsReceived: [],
          rejectedConnections: [],
          connectedClients: [],
          blockedClients: [],
          commandsProcessed: [],
          instantaneousOpsPerSec: [],
          syncFull: [],
          syncPartialOk: [],
          syncPartialErr: [],
          keyspaceHitsRatio: [],
          keys: [],
          expires: [],
          cpuSys: [],
          cpuUser: []
        };
        let isBuildXAxis = this.xAxis.length == 0;
        oneNodeInfoList.forEach(nodeInfo => {
          if (isBuildXAxis) {
            this.xAxis.push(formatTimeForChart(nodeInfo.updateTime));
          }
          oneNodeInfoData.usedMemory.push(nodeInfo.usedMemory);
          oneNodeInfoData.usedMemoryRss.push(nodeInfo.usedMemoryRss);
          oneNodeInfoData.usedMemoryOverhead.push(nodeInfo.usedMemoryOverhead);
          oneNodeInfoData.usedMemoryDataset.push(nodeInfo.usedMemoryDataset);
          oneNodeInfoData.memFragmentationRatio.push(
            nodeInfo.memFragmentationRatio
          );
          oneNodeInfoData.connectionsReceived.push(
            nodeInfo.connectionsReceived
          );
          oneNodeInfoData.rejectedConnections.push(
            nodeInfo.rejectedConnections
          );
          oneNodeInfoData.connectedClients.push(nodeInfo.connectedClients);
          oneNodeInfoData.blockedClients.push(nodeInfo.blockedClients);
          oneNodeInfoData.commandsProcessed.push(nodeInfo.commandsProcessed);
          oneNodeInfoData.instantaneousOpsPerSec.push(
            nodeInfo.instantaneousOpsPerSec
          );
          oneNodeInfoData.syncFull.push(nodeInfo.syncFull);
          oneNodeInfoData.syncPartialOk.push(nodeInfo.syncPartialOk);
          oneNodeInfoData.syncPartialErr.push(nodeInfo.syncPartialErr);
          oneNodeInfoData.keyspaceHitsRatio.push(nodeInfo.keyspaceHitsRatio);
          oneNodeInfoData.keys.push(nodeInfo.keys);
          oneNodeInfoData.expires.push(nodeInfo.expires);
          oneNodeInfoData.cpuSys.push(nodeInfo.cpuSys);
          oneNodeInfoData.cpuUser.push(nodeInfo.cpuUser);
        });
        nodeInfoList.push(oneNodeInfoData);
      });

      let usedMemorySeries = [];
      let usedMemoryRssSeries = [];
      let usedMemoryOverheadSeries = [];
      let usedMemoryDatasetSeries = [];
      let memFragmentationRatioSeries = [];
      let connectionsReceivedSeries = [];
      let rejectedConnectionsSeries = [];
      let connectedClientsSeries = [];
      let blockedClientsSeries = [];
      let commandsProcessedSeries = [];
      let instantaneousOpsPerSecSeries = [];
      let syncFullSeries = [];
      let syncPartialOkSeries = [];
      let syncPartialErrSeries = [];
      let keyspaceHitsRatioSeries = [];
      let keysSeries = [];
      let expiresSeries = [];
      let cpuSysSeries = [];
      let cpuUserSeries = [];
      nodeInfoList.forEach(nodeInfoData => {
        let node = nodeInfoData.node;
        usedMemorySeries.push({
          name: node,
          data: nodeInfoData.usedMemory
        });
        usedMemoryRssSeries.push({
          name: node,
          data: nodeInfoData.usedMemoryRss
        });
        usedMemoryOverheadSeries.push({
          name: node,
          data: nodeInfoData.usedMemoryOverhead
        });
        usedMemoryDatasetSeries.push({
          name: node,
          data: nodeInfoData.usedMemoryDataset
        });
        memFragmentationRatioSeries.push({
          name: node,
          data: nodeInfoData.memFragmentationRatio
        });
        connectionsReceivedSeries.push({
          name: node,
          data: nodeInfoData.connectionsReceived
        });
        rejectedConnectionsSeries.push({
          name: node,
          data: nodeInfoData.rejectedConnections
        });
        connectedClientsSeries.push({
          name: node,
          data: nodeInfoData.connectedClients
        });
        blockedClientsSeries.push({
          name: node,
          data: nodeInfoData.blockedClients
        });
        commandsProcessedSeries.push({
          name: node,
          data: nodeInfoData.commandsProcessed
        });
        instantaneousOpsPerSecSeries.push({
          name: node,
          data: nodeInfoData.instantaneousOpsPerSec
        });
        syncFullSeries.push({
          name: node,
          data: nodeInfoData.syncFull
        });
        syncPartialOkSeries.push({
          name: node,
          data: nodeInfoData.syncPartialOk
        });
        syncPartialErrSeries.push({
          name: node,
          data: nodeInfoData.syncPartialErr
        });
        keyspaceHitsRatioSeries.push({
          name: node,
          data: nodeInfoData.keyspaceHitsRatio
        });
        keysSeries.push({
          name: node,
          data: nodeInfoData.keys
        });
        expiresSeries.push({
          name: node,
          data: nodeInfoData.expires
        });
        cpuSysSeries.push({
          name: node,
          data: nodeInfoData.cpuSys
        });
        cpuUserSeries.push({
          name: node,
          data: nodeInfoData.cpuUser
        });
        usedMemorySeries.updateTime = nodeInfoData.updateTime;
      });
      let seriesList = [];
      seriesList.push({ id: "used_memory", series: usedMemorySeries });
      seriesList.push({ id: "used_memory_rss", series: usedMemoryRssSeries });
      seriesList.push({
        id: "used_memory_overhead",
        series: usedMemoryOverheadSeries
      });
      seriesList.push({
        id: "used_memory_dataset",
        series: usedMemoryDatasetSeries
      });
      seriesList.push({
        id: "mem_fragmentation_ratio",
        series: memFragmentationRatioSeries
      });
      seriesList.push({
        id: "connections_received",
        series: connectionsReceivedSeries
      });
      seriesList.push({
        id: "rejected_connections",
        series: rejectedConnectionsSeries
      });
      seriesList.push({
        id: "connected_clients",
        series: connectedClientsSeries
      });
      seriesList.push({ id: "blocked_clients", series: blockedClientsSeries });
      seriesList.push({
        id: "commands_processed",
        series: commandsProcessedSeries
      });
      seriesList.push({
        id: "instantaneous_ops_per_sec",
        series: instantaneousOpsPerSecSeries
      });
      seriesList.push({ id: "sync_full", series: syncFullSeries });
      seriesList.push({ id: "sync_partial_ok", series: syncPartialOkSeries });
      seriesList.push({ id: "sync_partial_err", series: syncPartialErrSeries });
      seriesList.push({
        id: "keyspace_hits_ratio",
        series: keyspaceHitsRatioSeries
      });
      seriesList.push({ id: "keys", series: keysSeries });
      seriesList.push({ id: "expires", series: expiresSeries });
      seriesList.push({ id: "cpu_sys", series: cpuSysSeries });
      seriesList.push({ id: "cpu_user", series: cpuUserSeries });
      return seriesList;
    },
    timedRefresh() {
      if (this.timer) {
        clearInterval(this.timer);
      } else {
        this.timer = setInterval(() => {
          let nodeInfoParam = this.nodeInfoParam;
          let timeRange = nodeInfoParam.timeRange;
          let range = timeRange[1] - timeRange[0];
          nodeInfoParam.timeRange = [new Date() - range, new Date()];
          nodeInfoParam.startTime = new Date() - range;
          nodeInfoParam.endTime = new Date();
          this.nodeInfoParam = nodeInfoParam;
          // let group = store.getters.getCurrentGroup;
          // getClusterById(this.nodeInfoParam.clusterId, cluster => {
          //   this.cluster = cluster;
          //   let clusterId = cluster.clusterId;
          //   this.getAllNodeList(clusterId);
          //   this.nodeInfoParam.clusterId = clusterId;
          //   this.slowLogParam.clusterId = clusterId;
          //   this.pickerDateTime();
          //   this.getNodeInfoDataList(this.nodeInfoParam);
          // });
        }, 60000 * 1);
      }
    },
    getSlowLogList(slowLogParam) {
      this.slowLogLoading = true;
      let url = "/monitor/getSlowLogList";
      API.post(
        url,
        slowLogParam,
        response => {
          let result = response.data;
          if (result.code == 0) {
            let slowLogList = result.data;
            slowLogList.forEach(slowLog => {
              slowLog.dateTime = formatTime(slowLog.dateTime);
            });
            this.slowLogList = slowLogList;
          } else {
            message.error(result.message);
          }
          this.slowLogLoading = false;
        },
        err => {
          this.slowLogLoading = false;
          message.error(err);
        }
      );
    }
  },
  watch: {
    // 深度监听 nodeInfoParam 变化
    nodeInfoParam: {
      handler: function() {
        this.getNodeInfoDataList(this.nodeInfoParam);
      },
      deep: true
    },
    nodes: {
      handler: function() {
        let nodeInfoParam = this.nodeInfoParam;
        let nodeList = [];
        if (!isEmpty(this.nodes) && this.nodes.length > 0) {
          this.nodes.forEach(node => {
            nodeList.push(node);
          });
        }
        nodeInfoParam.nodeList = nodeList;
        this.nodeInfoParam = nodeInfoParam;
      }
    },
    slowLogParam: {
      handler: function() {
        this.getSlowLogList(this.slowLogParam);
      },
      deep: true
    }
  },
  mounted() {
    let clusterId = this.$route.params.clusterId;
    getClusterById(clusterId, cluster => {
      this.cluster = cluster;
      this.getAllNodeList(this.cluster.clusterId);
      this.nodeInfoParam.clusterId = clusterId;
      this.slowLogParam.clusterId = clusterId;
      this.pickerDateTime();
      this.getNodeInfoDataList(this.nodeInfoParam);
    });
    this.timedRefresh();
  },
  destroyed() {
    clearInterval(this.timer);
  }
};
</script>

<style scoped>
.monitor-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 1px solid #dcdfe6;
}

.cluster-name,
.health {
  font-size: 18px;
}

.health {
  margin-left: 10px;
}

.base-info-wrapper {
  font-size: 14px;
  padding-top: 20px;
}

.base-info-title-wrapper {
  display: flex;
  margin-bottom: 10px;
  align-items: center;
}

.base-info-title {
  color: #909399;
  font-size: 16px;
  margin-right: 10px;
}

.base-info-item {
  padding-bottom: 10px;
}

.monitor-condition-wrapper {
  padding: 20px;
  font-size: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #ffffff;
  border-radius: 4px;
}

.refresh {
  font-size: 20px;
  cursor: pointer;
  color: #909399;
}

.refresh:hover {
  color: #2c3e50;
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
  margin: 10px -5px;
}

.box-card {
  margin: 5px;
}

.chart {
  min-height: 350px;
  width: 100%;
}

.chart-no-data {
  height: 0 !important;
}
</style>
