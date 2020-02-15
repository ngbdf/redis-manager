<template>
  <div id="monitor">
    <div class="body-wrapper" v-loading="clusterLoading">
      <el-row>
        <el-col :span="24">
          <div class="monitor-title">
            <span>
              <span class="cluster-name">{{ cluster.clusterName }}</span>
              <i class="el-icon-sunny health" v-if="cluster.clusterState == 'HEALTH'"></i>
              <i class="el-icon-heavy-rain bad" title="Status" v-else></i>
            </span>
            <div>
              <el-button
                size="mini"
                type="primary"
                title="Query"
                icon="el-icon-search"
                @click="handleQuery(cluster.clusterId)"
                :disabled="isSentinelMode"
              >Query</el-button>
              <el-button
                size="mini"
                type="warning"
                icon="el-icon-ali-slow"
                title="Slow log"
                @click="slowLogVisible = true"
                :disabled="isSentinelMode"
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
              Nodes:
              <el-tag size="mini">{{ cluster.clusterKnownNodes }}</el-tag>
            </div>
          </el-col>
          <el-col :xl="3" :lg="4" :md="6" :sm="8" v-if="!isSentinelMode">
            <div class="base-info-item">
              Master:
              <el-tag size="mini">{{ cluster.clusterSize }}</el-tag>
            </div>
          </el-col>
          <el-col :xl="3" :lg="4" :md="6" :sm="8" v-if="!isSentinelMode">
            <div class="base-info-item">
              Total Memory:
              <el-tag size="mini">{{ cluster.totalUsedMemory }}MB</el-tag>
            </div>
          </el-col>
          <el-col :xl="3" :lg="4" :md="6" :sm="8" v-if="!isSentinelMode">
            <div class="base-info-item">
              Total Keys:
              <el-tag size="mini">{{ cluster.totalKeys }}</el-tag>
            </div>
          </el-col>
          <el-col :xl="3" :lg="4" :md="6" :sm="8" v-if="!isSentinelMode">
            <div class="base-info-item">
              Total Expires:
              <el-tag size="mini">{{ cluster.totalExpires }}</el-tag>
            </div>
          </el-col>
          <el-col :xl="3" :lg="4" :md="6" :sm="8" v-if="isSentinelMode">
            <div class="base-info-item">
              Sentinel OK:
              <el-tag size="mini">{{ cluster.sentinelOk }}</el-tag>
            </div>
          </el-col>
          <el-col :xl="3" :lg="4" :md="6" :sm="8" v-if="isSentinelMode">
            <div class="base-info-item">
              Sentinel Masters:
              <el-tag size="mini">{{ cluster.sentinelMasters }}</el-tag>
            </div>
          </el-col>
          <el-col :xl="3" :lg="4" :md="6" :sm="8" v-if="isSentinelMode">
            <div class="base-info-item">
              Master OK:
              <el-tag size="mini">{{ cluster.masterOk }}</el-tag>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>

    <div
      class="body-wrapper"
      style="margin-top: 20px;"
      v-loading="clusterLoading"
      v-if="isSentinelMode"
    >
      <div class="base-info-wrapper">
        <el-table :data="sentinelMasterList">
          <el-table-column property="name" label="Master Name"></el-table-column>
          <el-table-column property="status" label="Status">
            <template slot-scope="scope">
              <el-tag size="mini" v-if="scope.row.status == 'ok'">{{ scope.row.status }}</el-tag>
              <el-tag size="mini" type="danger" v-else>{{ scope.row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column property="flags" label="Flags">
            <template slot-scope="scope">
              <el-tag size="mini" v-if="scope.row.flags == 'master'">{{ scope.row.flags }}</el-tag>
              <el-tag size="mini" type="danger" v-else>{{ scope.row.flags }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="monitor" label="Monitor" align="center" sortable>
            <template slot-scope="scope">
              <i class="el-icon-success status-icon normal-status" v-if="scope.row.monitor"></i>
              <i class="el-icon-error status-icon normal-bad" v-else></i>
            </template>
          </el-table-column>
          <el-table-column label="Master Node">
            <template slot-scope="scope">{{ scope.row.host }}:{{ scope.row.port }}</template>
          </el-table-column>
          <el-table-column property="lastMasterNode" label="Last Master Node"></el-table-column>
          <el-table-column property="numSlaves" label="Num Slaves"></el-table-column>
          <el-table-column property="sentinels" label="Sentinels"></el-table-column>
          <el-table-column label="Detail">
            <el-button
              size="mini"
              type="primary"
              slot-scope="scope"
              @click="sentinelMasterInfoVisible = true; sentinelMaster = scope.row"
            >Detail</el-button>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <div style="margin-top: 20px;">
      <div class="monitor-condition-wrapper">
        <div class="condition-wrapper">
          <el-select
            v-model="nodeType"
            size="small"
            placeholder="Node Type"
            class="condition-item"
            style="width: 120px"
          >
            <el-option label="All Master" value="ALL_MASTER"></el-option>
            <el-option label="All" value="ALL"></el-option>
            <el-option label="Node" value="NODE"></el-option>
          </el-select>

          <el-select
            v-model="nodes"
            filterable
            size="small"
            placeholder="Node"
            class="condition-item"
            multiple
            collapse-tags
            clearable
            style="min-width: 280px"
            v-if="nodeType == 'NODE'"
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
          <el-select
            v-model="selectedInfoItemList"
            size="small"
            placeholder="Info Item"
            class="condition-item"
            style="width: 260px"
            multiple
            collapse-tags
            @change="monitorItemChange"
          >
            <el-option
              v-for="infoItem in infoItemList"
              :key="infoItem"
              :label="infoItem"
              :value="infoItem"
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
        <i class="el-icon-refresh-left refresh" @click="refresh()"></i>
      </div>
      <el-row class="echart-wrapper" id="monitor-charts">
        <echartsItem
          v-for="infoItem in selectedInfoItemList"
          :key="infoItem"
          :infoItem="infoItem"
          :nodeInfoParam="nodeInfoParam"
        ></echartsItem>
      </el-row>
      <!-- detail monitor -->
    </div>
    <el-dialog title="Query" :visible.sync="queryVisible" :close-on-click-modal="false" width="60%">
      <query :cluster="cluster"></query>
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
            v-for="node in slowLogRedisNodeList"
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

    <el-dialog
      title="Sentinel Master Info"
      :visible.sync="sentinelMasterInfoVisible"
      :close-on-click-modal="false"
      v-if="sentinelMasterInfoVisible"
      width="40%"
    >
      <sentinelMasterInfo :sentinelMaster="sentinelMaster"></sentinelMasterInfo>
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
import sentinelMasterInfo from "@/components/view/SentinelMasterInfo";
import echartsItem from "@/components/monitor/EchartsItem";
import API from "@/api/api.js";
import { formatTime, formatTimeForChart } from "@/utils/time.js";
import { isEmpty } from "@/utils/validate.js";
import { store } from "@/vuex/store.js";
import { getClusterById } from "@/components/cluster/cluster.js";
import message from "@/utils/message.js";
export default {
  components: {
    query,
    echartsItem,
    sentinelMasterInfo
  },
  data() {
    return {
      queryVisible: false,
      cluster: {},
      redisNodeList: [],
      slowLogRedisNodeList: [],
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
      infoItemList: [
        "used_memory",
        "used_memory_rss",
        "used_memory_overhead",
        "used_memory_dataset",
        "mem_fragmentation_ratio",
        "connections_received",
        "rejected_connections",
        "connected_clients",
        "blocked_clients",
        "commands_processed",
        "instantaneous_ops_per_sec",
        "sync_full",
        "sync_partial_ok",
        "sync_partial_err",
        "keyspace_hits_ratio",
        "keys",
        "expires",
        "cpu_sys",
        "cpu_user"
      ],
      selectedInfoItemList: [
        "used_memory",
        "used_memory_rss",
        "mem_fragmentation_ratio",
        "connected_clients",
        "commands_processed",
        "instantaneous_ops_per_sec",
        "keyspace_hits_ratio",
        "keys",
        "expires"
      ],
      nodeInfoParam: {
        nodeList: [],
        timeType: 0,
        timeRange: [new Date() - 15 * 60 * 1000, new Date()]
      },
      nodeType: "",
      nodes: [],
      slowLogVisible: false,
      slowLogList: [],
      timer: 0,
      slowLogParam: {},
      slowLogList: [],
      clusterLoading: false,
      slowLogLoading: false,
      conditionSelectedLoading: false,
      isSentinelMode: false,
      sentinelMasterList: [],
      sentinelMaster: {},
      sentinelMasterInfoVisible: false
    };
  },
  methods: {
    handleQuery(clusterId) {
      this.queryVisible = true;
    },
    pickerDateTime() {
      let timeRange = this.nodeInfoParam.timeRange;
      let startTime = timeRange[0];
      let endTime = timeRange[1];
      this.nodeInfoParam.startTime = startTime;
      this.nodeInfoParam.endTime = endTime;
    },
    getAllNodeList(clusterId) {
      let url = "/node-manage/getAllNodeList/" + clusterId;
      this.slowLogRedisNodeList.push({ label: "All" });
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
              let redisNode = {
                node: hostAndPort,
                role: role,
                value: hostAndPort,
                label: hostAndPort + " " + role
              };
              redisNodeList.push(redisNode);
              this.slowLogRedisNodeList.push(redisNode);
            });
            this.redisNodeList = redisNodeList;
            this.nodeType = "ALL_MASTER";
          } else {
            message.error(result.message);
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    timedRefresh() {
      if (this.timer) {
        clearInterval(this.timer);
      } else {
        this.timer = setInterval(() => {
          this.refresh();
        }, 60000 * 5);
      }
    },
    refresh() {
      let nodeInfoParam = this.nodeInfoParam;
      let timeRange = nodeInfoParam.timeRange;
      let range = timeRange[1] - timeRange[0];
      nodeInfoParam.timeRange = [new Date() - range, new Date()];
      nodeInfoParam.startTime = new Date() - range;
      nodeInfoParam.endTime = new Date();
      this.nodeInfoParam = nodeInfoParam;
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
    },
    monitorItemChange(val) {
      setTimeout(() => {
        this.refresh();
      }, 300);
    },
    getSentinelMasterList(clusterId) {
      let url = "/sentinel/getSentinelMasterList/" + clusterId;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.sentinelMasterList = result.data;
          }
        },
        err => {
          message.error(err);
        }
      );
    }
  },
  watch: {
    nodes: {
      handler: function() {
        let nodeInfoParam = this.nodeInfoParam;
        let nodeList = [];
        if (this.nodes.length > 0) {
          this.nodes.forEach(node => {
            nodeList.push(node);
          });
          nodeInfoParam.nodeList = nodeList;
          this.nodeInfoParam = nodeInfoParam;
        }
      }
    },
    nodeType: {
      handler: function() {
        if (this.nodeType == "NODE") {
          this.nodes = [];
          return;
        }
        let nodeInfoParam = this.nodeInfoParam;
        let nodeList = [];
        // default all master
        if (this.nodeType == "ALL_MASTER") {
          this.redisNodeList.forEach(node => {
            if (node.role == "master") {
              nodeList.push(node.node);
            }
          });
        }
        if (this.nodeType == "ALL_MASTER" || this.nodeType == "ALL") {
          this.nodes = [];
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
      if (parseInt(cluster.redisVersion.substring(0)) < 4) {
        for (var i = 0; i < this.selectedInfoItemList.length; i++) {
          let infoItem = this.selectedInfoItemList[i];
          if (
            infoItem == "used_memory_overhead" ||
            infoItem == "used_memory_dataset"
          ) {
            this.selectedInfoItemList.splice(i, i);
          }
        }
      }
      this.getAllNodeList(this.cluster.clusterId);
      this.nodeInfoParam.clusterId = clusterId;
      this.slowLogParam.clusterId = clusterId;
      this.pickerDateTime();
      this.timedRefresh();
      this.isSentinelMode = cluster.redisMode == "sentinel";
      if (this.isSentinelMode) {
        this.getSentinelMasterList(clusterId);
        this.infoItemList = [
          "connections_received",
          "rejected_connections",
          "connected_clients",
          "blocked_clients",
          "commands_processed",
          "instantaneous_ops_per_sec",
          "cpu_sys",
          "cpu_user"
        ];
        this.selectedInfoItemList = this.infoItemList;
      }
    });
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
.health,
.bad {
  font-size: 18px;
}

.bad {
  color: #f4516c;
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
  min-height: 400px;
  width: 100%;
}

.chart-no-data {
  height: 0 !important;
}

.normal-status {
  color: #40c9c6;
}

.bad-status {
  color: #f4516c;
}

</style>
