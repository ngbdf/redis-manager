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
        <!-- <div class="base-info-title-wrapper">
          <span class="base-info-title">Base Info</span>
          <i class="el-icon-refresh-left refresh" @click="getClusterById(nodeInfoParam.clusterId)"></i>
        </div>-->
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
            v-model="nodeInfoParam.dataType"
            size="small"
            placeholder="Data Type"
            class="condition-item"
            style="width: 100px"
            v-if="showDataType"
          >
            <el-option
              v-for="dataType in dataTypeList"
              :key="dataType.label"
              :label="dataType.label"
              :value="dataType.value"
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
        <i class="el-icon-refresh-left refresh" @click="getNodeInfoList(nodeInfoParam)"></i>
      </div>
      <el-row class="echart-wrapper" id="monitor-charts">
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div id="memory" class="chart" :class="{ 'chart-no-data' : noNodeInfoData }"></div>
          </el-card>
        </el-col>
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div
              id="fragmentation-ratio"
              class="chart"
              :class="{ 'chart-no-data' : noNodeInfoData }"
            ></div>
          </el-card>
        </el-col>
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div id="connection" class="chart" :class="{ 'chart-no-data' : noNodeInfoData }"></div>
          </el-card>
        </el-col>
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div id="client" class="chart" :class="{ 'chart-no-data' : noNodeInfoData }"></div>
          </el-card>
        </el-col>
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div id="qps" class="chart" :class="{ 'chart-no-data' : noNodeInfoData }"></div>
          </el-card>
        </el-col>
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div id="replica" class="chart" :class="{ 'chart-no-data' : noNodeInfoData }"></div>
          </el-card>
        </el-col>
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div
              id="keyspace-hits-ration"
              class="chart"
              :class="{ 'chart-no-data' : noNodeInfoData }"
            ></div>
          </el-card>
        </el-col>
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div id="keys-expires" class="chart" :class="{ 'chart-no-data' : noNodeInfoData }"></div>
          </el-card>
        </el-col>
        <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
          <el-card shadow="hover" class="box-card">
            <div id="cpu" class="chart" :class="{ 'chart-no-data' : noNodeInfoData }"></div>
          </el-card>
        </el-col>
      </el-row>
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
            v-for="node in nodeList"
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
      nodeList: [{ label: "ALL", value: "" }],
      dataTypeList: [
        {
          value: 1,
          label: "Avg"
        },
        {
          value: 2,
          label: "Max"
        },
        {
          value: -1,
          label: "Min"
        }
      ],
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
        node: "",
        dataType: 1,
        timeType: 0,
        timeRange: [new Date() - 3600 * 1000, new Date()]
      },
      nodeInfoList: [],
      currentNode: "",
      showDataType: true,
      noNodeInfoData: true,
      xAxis: [],
      echartsData: [],
      lineColor: ["#3888fa", "#FF005A", "#ffb980", "#40c9c6"],
      areaStyleColor: [
        "rgba(56, 136, 250, 0.1)",
        "rgba(250, 0, 90, 0.1)",
        "rgba(64, 201, 198, 0.1)",
        "rgba(255, 185, 128, 0.1)"
      ],
      chartList: [],
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
            let nodeList = [{ label: "ALL", value: "" }];
            result.data.forEach(node => {
              var hostAndPort = node.host + ":" + node.port;
              var role = node.nodeRole.toLowerCase();
              nodeList.push({
                value: hostAndPort,
                label: hostAndPort + " " + role
              });
            });
            this.nodeList = nodeList;
          } else {
            message.error(result.message);
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    getNodeInfoList(nodeInfoParam) {
      this.monitorDataLoading = true;
      let url = "/monitor/getNodeInfoList";
      API.post(
        url,
        nodeInfoParam,
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.nodeInfoList = result.data;
            this.noNodeInfoData = false;
            this.nodeInfoList.forEach(nodeInfo => {
              nodeInfo.updateTime = formatTimeForChart(nodeInfo.updateTime);
            });
            // init data for echarts
            this.buildEchartsData();
            this.initCharts();
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
    initCharts() {
      var erd = elementResizeDetectorMaker();
      let echartsData = this.echartsData;
      echartsData.forEach(echartsDataGroup => {
        let series = this.buildSeries(echartsDataGroup.data);
        var chart = echarts.init(document.getElementById(echartsDataGroup.id));
        // 绘制图表
        chart.setOption({
          title: {
            text: echartsDataGroup.title,
            left: 35
          },
          legend: {
            // 工具栏位置
            bottom: -5
          },
          // 图表位置
          grid: {
            left: 20,
            right: 20,
            bottom: 30,
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
    buildEchartsData() {
      this.xAxis = [];
      let usedMemory = [];
      let usedMemoryRss = [];
      let usedMemoryOverhead = [];
      let usedMemoryDataset = [];
      let memFragmentationRatio = [];
      let connectedClients = [];
      let connectionsReceived = [];
      let rejectedConnections = [];
      let clientLongestOutputList = [];
      let clientBiggestInputBuf = [];
      let blockedClients = [];
      let commandsProcessed = [];
      let instantaneousOpsPerSec = [];
      let syncFull = [];
      let syncPartialOk = [];
      let syncPartialErr = [];
      let keyspaceHitsRatio = [];
      let keys = [];
      let expires = [];
      let cpuSys = [];
      let cpuUser = [];
      this.nodeInfoList.forEach(nodeInfo => {
        // memory
        usedMemory.push(nodeInfo.usedMemory);
        usedMemoryRss.push(nodeInfo.usedMemoryRss);
        usedMemoryOverhead.push(nodeInfo.usedMemoryOverhead);
        usedMemoryDataset.push(nodeInfo.usedMemoryDataset);
        // fragmentation-ratio
        memFragmentationRatio.push(nodeInfo.memFragmentationRatio);
        // connection
        connectionsReceived.push(nodeInfo.connectionsReceived);
        rejectedConnections.push(nodeInfo.rejectedConnections);
        // client
        connectedClients.push(nodeInfo.connectedClients);
        blockedClients.push(nodeInfo.blockedClients);
        // qps
        commandsProcessed.push(nodeInfo.commandsProcessed);
        instantaneousOpsPerSec.push(nodeInfo.instantaneousOpsPerSec);
        // replica
        syncFull.push(nodeInfo.syncFull);
        syncPartialOk.push(nodeInfo.syncPartialOk);
        syncPartialErr.push(nodeInfo.syncPartialErr);
        // keyspace hits ration
        keyspaceHitsRatio.push(nodeInfo.keyspaceHitsRatio);
        // keys expires
        keys.push(nodeInfo.keys);
        expires.push(nodeInfo.expires);
        // cpu
        cpuSys.push(nodeInfo.cpuSys);
        cpuUser.push(nodeInfo.cpuUser);
        this.xAxis.push(nodeInfo.updateTime);
      });
      let memoryData = [];
      let fragmentationRatioData = [];
      let connectionData = [];
      let clientData = [];
      let qpsData = [];
      let replicaData = [];
      let keyspaceHitsRationData = [];
      let keysExpiresData = [];
      let cpuData = [];
      memoryData.push({ name: "used_memory", data: usedMemory });
      memoryData.push({ name: "used_memory_rss", data: usedMemoryRss });
      memoryData.push({
        name: "used_memory_overhead",
        data: usedMemoryOverhead
      });
      memoryData.push({ name: "used_memory_dataset", data: usedMemoryDataset });
      fragmentationRatioData.push({
        name: "fragmentation_ratio",
        data: memFragmentationRatio
      });
      connectionData.push({
        name: "connections_received",
        data: connectionsReceived
      });
      connectionData.push({
        name: "rejected_connections",
        data: rejectedConnections
      });
      clientData.push({ name: "connected_clients", data: connectedClients });
      clientData.push({ name: "blocked_clients", data: blockedClients });
      qpsData.push({ name: "commands_processed", data: commandsProcessed });
      qpsData.push({
        name: "instantaneous_ops_per_sec",
        data: instantaneousOpsPerSec
      });
      replicaData.push({ name: "sync_full", data: syncFull });
      replicaData.push({ name: "sync_partial_ok", data: syncPartialOk });
      replicaData.push({ name: "sync_partial_err", data: syncPartialErr });
      keyspaceHitsRationData.push({
        name: "keyspace_hits_ratio",
        data: keyspaceHitsRatio
      });
      keysExpiresData.push({ name: "keys", data: keys });
      keysExpiresData.push({ name: "expires", data: expires });
      cpuData.push({ name: "cpu_sys", data: cpuSys });
      cpuData.push({ name: "cpu_user", data: cpuUser });

      let echartsData = [];
      echartsData.push({
        id: "memory",
        title: "Memory(MB)",
        data: memoryData
      });
      echartsData.push({
        id: "fragmentation-ratio",
        title: "Fragmentation Ratio",
        data: fragmentationRatioData
      });
      echartsData.push({
        id: "connection",
        title: "Connection",
        data: connectionData
      });
      echartsData.push({
        id: "client",
        title: "Client",
        data: clientData
      });
      echartsData.push({
        id: "qps",
        title: "QPS",
        data: qpsData
      });

      echartsData.push({
        id: "replica",
        title: "Replica",
        data: replicaData
      });
      echartsData.push({
        id: "keyspace-hits-ration",
        title: "Keyspace Hits Ration",
        data: keyspaceHitsRationData
      });
      echartsData.push({
        id: "keys-expires",
        title: "Keys & Expires",
        data: keysExpiresData
      });
      echartsData.push({
        id: "cpu",
        title: "CPU",
        data: cpuData
      });
      this.echartsData = echartsData;
    },
    buildSeries(data) {
      let series = [];
      let length = data.length;
      if (length == 0) {
        return series;
      }
      for (var i = 0; i < length; i++) {
        var dataItem = data[i];
        var color = this.lineColor[i];
        var areaStyleColor = this.areaStyleColor[i];
        series.push({
          name: dataItem.name,
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
          data: dataItem.data,
          animationDuration: 2800,
          animationEasing: "cubicInOut"
          // 背景色
          // areaStyle: {}
        });
      }
      return series;
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
          let group = store.getters.getCurrentGroup;
          getClusterById(this.nodeInfoParam.clusterId, cluster => {
            this.cluster = cluster;
            let clusterId = cluster.clusterId;
            this.getAllNodeList(clusterId);
            this.nodeInfoParam.clusterId = clusterId;
            this.slowLogParam.clusterId = clusterId;
            this.pickerDateTime();
            this.getNodeInfoList(this.nodeInfoParam);
          });
        }, 60000);
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
        this.getNodeInfoList(this.nodeInfoParam);
      },
      deep: true
    },
    currentNode: {
      handler: function() {
        let nodeInfoParam = this.nodeInfoParam;
        if (!isEmpty(this.currentNode)) {
          this.showDataType = false;
          nodeInfoParam.dataType = 0;
        } else {
          this.showDataType = true;
          nodeInfoParam.dataType = 1;
        }
        nodeInfoParam.node = this.currentNode;
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
      this.getNodeInfoList(this.nodeInfoParam);
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
  height: 300px;
  width: 100%;
}

.chart-no-data {
  height: 0 !important;
}
</style>
