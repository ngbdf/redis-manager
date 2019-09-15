<template>
  <div id="monitor" class="body-wrapper">
    <el-row>
      <el-col :span="24">
        <div class="monitor-title">
          <span>
            <span class="cluster-name">Shanghai</span>
            <i class="el-icon-sunny health"></i>
          </span>
          <div>
            <el-button size="mini" type="primary" title="Query" icon="el-icon-search" @click="handleQuery()">Query</el-button>
            <el-button size="mini" type="warning" icon="el-icon-search" title="Slow log">Slow Log</el-button>
          </div>
        </div>
      </el-col>
    </el-row>
    <div class="base-info-wrapper">
      <div class="base-info-title-wrapper">
        <span class="base-info-title">Base Info</span>
        <i class="el-icon-refresh-left refresh"></i>
      </div>
      <el-row class="base-info">
        <el-col :xl="6" :lg="8" :md="12" :sm="12">
          <div class="base-info-item">
            Mode:
            <el-tag size="mini">cluster</el-tag>
          </div>
        </el-col>
        <el-col :xl="6" :lg="8" :md="12" :sm="12">
          <div class="base-info-item">
            Version:
            <el-tag size="mini">4.0.10</el-tag>
          </div>
        </el-col>
        <el-col :xl="6" :lg="8" :md="12" :sm="12">
          <div class="base-info-item">
            Master:
            <el-tag size="mini">2</el-tag>
          </div>
        </el-col>
        <el-col :xl="6" :lg="8" :md="12" :sm="12">
          <div class="base-info-item">
            Node:
            <el-tag size="mini">12</el-tag>
          </div>
        </el-col>
        <el-col :xl="6" :lg="8" :md="12" :sm="12">
          <div class="base-info-item">
            Totol Keys:
            <el-tag size="mini">45648</el-tag>
          </div>
        </el-col>
        <el-col :xl="6" :lg="8" :md="12" :sm="12">
          <div class="base-info-item">
            Expire Keys:
            <el-tag size="mini">4898</el-tag>
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
            width="1080px"
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
      <i class="el-icon-refresh-left refresh"></i>
    </div>
    <el-row class="echart-wrapper" id="monitor-charts">
      <el-col :xl="12" :lg="12" :md="24" :sm="24">
        <div id="memory" class="chart"></div>
      </el-col>
      <el-col :xl="12" :lg="12" :md="24" :sm="24">
        <div id="fragmentation-ratio" class="chart"></div>
      </el-col>
      <el-col :xl="12" :lg="12" :md="24" :sm="24">
        <div id="connection" class="chart"></div>
      </el-col>
      <el-col :xl="12" :lg="12" :md="24" :sm="24">
        <div id="client" class="chart"></div>
      </el-col>
      <el-col :xl="12" :lg="12" :md="24" :sm="24">
        <div id="qps" class="chart"></div>
      </el-col>
      <el-col :xl="12" :lg="12" :md="24" :sm="24">
        <div id="replica" class="chart"></div>
      </el-col>
      <el-col :xl="12" :lg="12" :md="24" :sm="24">
        <div id="keyspace-hits-ration" class="chart"></div>
      </el-col>
      <el-col :xl="12" :lg="12" :md="24" :sm="24">
        <div id="keys-expires" class="chart"></div>
      </el-col>
      <el-col :xl="12" :lg="12" :md="24" :sm="24">
        <div id="cpu" class="chart"></div>
      </el-col>
    </el-row>
    <el-dialog title="Query" :visible.sync="queryVisible" width="60%">
      <query :clusterId="clusterId"></query>
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
export default {
  components: {
    query
  },
  data() {
    return {
      queryVisible: false,
      clusterId: "222",
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
      timeRange: "",
      nodeInfoList: [
        {
          connectedClients: 34,
          clientLongestOutputList: 34,
          clientBiggestInputBuf: 435,
          blockedClients: 5,
          usedMemory: 543,
          usedMemoryRss: 222,
          usedMemoryOverhead: 345,
          usedMemoryDataset: 123,
          usedMemoryDatasetPerc: 1,
          memFragmentationRatio: 1,
          totalConnectionsReceived: 34,
          connectionsReceived: 534,
          totalCommandsProcessed: 435,
          commandsProcessed: 543,
          instantaneousOpsPerSec: 86,
          totalNetInputBytes: 2,
          netInputBytes: 13,
          totalNetOutputBytes: 352,
          netOutputBytes: 1,
          rejectedConnections: 32,
          syncFull: 32,
          syncPartialOk: 32,
          syncPartialErr: 32,
          keyspaceHits: 32,
          keyspaceMisses: 32,
          keyspaceHitsRatio: 0.2,
          usedCpuSys: 32,
          usedCpuUser: 32,
          keys: 32345,
          expires: 3224,
          updateTime: "09/20"
        },
        {
          connectedClients: 633,
          clientLongestOutputList: 32,
          clientBiggestInputBuf: 32,
          blockedClients: 32,
          usedMemory: 32,
          usedMemoryRss: 32,
          usedMemoryOverhead: 32,
          usedMemoryDataset: 32,
          usedMemoryDatasetPerc: 32,
          memFragmentationRatio: 32,
          totalConnectionsReceived: 32,
          connectionsReceived: 32,
          totalCommandsProcessed: 32,
          commandsProcessed: 32,
          instantaneousOpsPerSec: 32,
          totalNetInputBytes: 32,
          netInputBytes: 32,
          totalNetOutputBytes: 32,
          netOutputBytes: 32,
          rejectedConnections: 32,
          syncFull: 32,
          syncPartialOk: 32,
          syncPartialErr: 32,
          keyspaceHits: 32,
          keyspaceMisses: 32,
          keyspaceHitsRatio: 0.5,
          usedCpuSys: 32,
          usedCpuUser: 32,
          keys: 3244,
          expires: 32,
          updateTime: "09/21"
        },
        {
          connectedClients: 666,
          clientLongestOutputList: 32,
          clientBiggestInputBuf: 32,
          blockedClients: 32,
          usedMemory: 32,
          usedMemoryRss: 32,
          usedMemoryOverhead: 32,
          usedMemoryDataset: 32,
          usedMemoryDatasetPerc: 32,
          memFragmentationRatio: 32,
          totalConnectionsReceived: 32,
          connectionsReceived: 32,
          totalCommandsProcessed: 32,
          commandsProcessed: 32,
          instantaneousOpsPerSec: 32,
          totalNetInputBytes: 32,
          netInputBytes: 32,
          totalNetOutputBytes: 32,
          netOutputBytes: 32,
          rejectedConnections: 32,
          syncFull: 32,
          syncPartialOk: 32,
          syncPartialErr: 32,
          keyspaceHits: 5532,
          keyspaceMisses: 32,
          keyspaceHitsRatio: 1,
          usedCpuSys: 32,
          usedCpuUser: 32,
          keys: 32555,
          expires: 3233,
          updateTime: "09/23"
        }
      ],
      xAxis: [],
      echartsData: [],
      lineColor: ["#3888fa", "#FF005A", "#40c9c6", "#ffb980"],
      areaStyleColor: [
        "rgba(56, 136, 250, 0.1)",
        "rgba(250, 0, 90, 0.1)",
        "rgba(64, 201, 198, 0.1)",
        "rgba(255, 185, 128, 0.1)"
      ],
      chartList: []
    };
  },
  methods: {
    handleQuery() {
      this.queryVisible = true;
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
      let usedCpuSys = [];
      let usedCpuUser = [];
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
        usedCpuSys.push(nodeInfo.usedCpuSys);
        usedCpuUser.push(nodeInfo.usedCpuUser);
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
      cpuData.push({ name: "used_cpu_sys", data: usedCpuSys });
      cpuData.push({ name: "used_cpu_user", data: usedCpuUser });

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
    }
  },
  mounted() {
    // init data for echarts
    this.buildEchartsData();
    this.initCharts();
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
  border-bottom: 1px solid #dcdfe6;
  padding: 20px 0;
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

.base-info {
  width: 50%;
}

.base-info-item {
  padding-bottom: 10px;
}

.monitor-condition-wrapper {
  padding: 20px 0;
  font-size: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.chart {
  height: 300px;
  width: 100%;
  padding: 20px 0;
}
</style>