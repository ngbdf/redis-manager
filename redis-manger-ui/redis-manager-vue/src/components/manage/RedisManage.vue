<template>
  <div id="cluster-manage" class="body-wrapper">
    <div class="manage-header-wrapper">
      <div class="title-wrapper">
        <span>{{ cluster.clusterName }}</span>
        <i class="el-icon-sunny health" title="Status" v-if="cluster.clusterStatus == 'HEALTH'"></i>
      </div>

      <div class="base-info-operation-wrapper">
        <span class="base-info-item">
          Master:
          <el-tag size="mini">{{ cluster.clusterSize }}</el-tag>
        </span>
        <span class="base-info-item">
          Node:
          <el-tag size="mini">{{ cluster.clusterKnownNodes }}</el-tag>
        </span>
        <span class="base-info-item">
          Environment:
          <el-tag size="mini" v-if="cluster.installationEnvironment == 0">Docker</el-tag>
          <el-tag size="mini" v-else-if="cluster.installationEnvironment == 1">Machine</el-tag>
        </span>
        <span class="base-info-item">
          Type:
          <el-tag size="mini" v-if="cluster.installationEnvironment == 0">Redis Manager</el-tag>
          <el-tag size="mini" v-else>Import</el-tag>
        </span>
      </div>
    </div>

    <div class="nodes-wrapper">
      <div class="batch-operation-wrapper">
        <div class="batch-title">Batch Operation</div>
        <div style="display: flex; justify-content: space-between;">
          <div>
            <el-link :underline="false" icon="el-icon-finished">Memory Purge</el-link>
            <el-divider direction="vertical"></el-divider>
            <el-link :underline="false" icon="el-icon-zoom-out">Forget</el-link>
            <el-divider direction="vertical"></el-divider>
            <el-link :underline="false" icon="el-icon-video-play">Start</el-link>
            <el-divider direction="vertical"></el-divider>
            <el-link :underline="false" icon="el-icon-switch-button">Stop</el-link>
            <el-divider direction="vertical"></el-divider>
            <el-link :underline="false" icon="el-icon-refresh-left">Restart</el-link>
            <el-divider direction="vertical"></el-divider>
            <el-link :underline="false" icon="el-icon-circle-close">Delete</el-link>
            <el-divider direction="vertical"></el-divider>
            <el-link :underline="false" icon="el-icon-edit">Edit Config</el-link>
          </div>
          <el-link :underline="false" icon="el-icon-circle-plus-outline" type="primary" @click="importNodeVisible = true">Import Node</el-link>
        </div>
      </div>
      <div class="table-wrapper">
        <!-- default-expand-all -->
        <el-table
          :data="redisNodeList"
          style="width: 100%; margin-bottom: 10px;"
          row-key="nodeId"
          size="medium"
          :default-sort="{prop: 'slotRange', order: 'ascending'}"
          :row-class-name="tableRowClassName"
          @selection-change="handleSelectionChange"
          :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
        >
          <el-table-column type="selection" width="55px"></el-table-column>
          <!-- status 有多种 -->
          <el-table-column label="Link State" sortable width="150px">
            <template slot-scope="scope">
              {{ scope.row.slot }}
              <el-tag
                size="mini"
                type="success"
                v-if="scope.row.linkState == 'connected'"
              >{{ scope.row.linkState }}</el-tag>
              <el-tag size="small" class="pointer" v-else type="warning">{{ scope.row.linkState }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="Flags" width="90px">
            <template slot-scope="scope">
              <el-tag size="mini" v-if="scope.row.flags == 'master'">{{ scope.row.flags }}</el-tag>
              <el-tag size="mini" class="pointer" type="info" v-else>{{ scope.row.flags }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="In Cluster" align="center" sortable>
            <template slot-scope="scope">
              <i class="el-icon-success status-icon normal-status" v-if="scope.row.inCluster"></i>
              <i class="el-icon-error status-icon normal-bad" v-else></i>
            </template>
          </el-table-column>
          <el-table-column label="Run Status" align="center" sortable>
            <template slot-scope="scope">
              <i class="el-icon-success status-icon normal-status" v-if="scope.row.runStatus"></i>
              <i class="el-icon-error status-icon normal-bad" v-else></i>
            </template>
          </el-table-column>
          <el-table-column prop="host" label="Host" sortable></el-table-column>
          <el-table-column prop="port" label="Port" sortable></el-table-column>
          <el-table-column prop="slotRange" label="Slot Range" sortable></el-table-column>
          <el-table-column label="Meta" width="130px;">
            <template slot-scope="scope">
              <el-tag
                size="mini"
                class="pointer"
                @click="getNodeInfo(scope.row.clusterId, scope.row.host, scope.row.port)"
              >Info</el-tag>
              <el-tag
                size="mini"
                class="pointer"
                @click="getConfig(scope.row.clusterId, scope.row.host, scope.row.port)"
              >Config</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="time" label="Time" sortable></el-table-column>
          <el-table-column label="Operation" width="220px">
            <!-- <template slot="header">
              <el-input v-model="search" size="mini" placeholder="Search" />
            </template>-->
            <template slot-scope="scope">
              <el-dropdown size="mini" split-button type="warning">
                Cluster
                <el-dropdown-menu slot="dropdown" v-if="scope.row.nodeRole == 'MASTER'">
                  <el-dropdown-item @click.native="moveSlotVisible = true">Move Slot</el-dropdown-item>
                  <el-dropdown-item>Memory Purge</el-dropdown-item>
                </el-dropdown-menu>
                <el-dropdown-menu
                  slot="dropdown"
                  v-else-if="scope.row.nodeRole == 'SLAVE' || scope.row.nodeRole == 'REPLICA'"
                >
                  <el-dropdown-item>Forget</el-dropdown-item>
                  <el-dropdown-item @click.native="beSlaveVisible = true">BeSlave</el-dropdown-item>
                  <el-dropdown-item>Failover</el-dropdown-item>
                  <el-dropdown-item>Memory Purge</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
              <el-dropdown size="mini" split-button type="danger">
                Node
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item>Start</el-dropdown-item>
                  <el-dropdown-item>Stop</el-dropdown-item>
                  <el-dropdown-item>Restart</el-dropdown-item>
                  <el-dropdown-item>Delete</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog
      title="Master Node List"
      :visible.sync="beSlaveVisible"
      :close-on-click-modal="false"
      width="80%"
    >
      <el-table
        :data="redisNodeList"
        stripe
        size="medium"
        :default-sort="{prop: 'slotRange', order: 'ascending'}"
      >
        <!-- status 有多种 -->
        <el-table-column label="Link State" sortable width="150px">
          <template slot-scope="scope">
            {{ scope.row.slot }}
            <el-tag
              size="mini"
              type="success"
              v-if="scope.row.linkState == 'connected'"
            >{{ scope.row.linkState }}</el-tag>
            <el-tag size="small" class="pointer" v-else type="warning">{{ scope.row.linkState }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Flags" width="90px">
          <template slot-scope="scope">
            <el-tag size="mini" v-if="scope.row.flags == 'master'">{{ scope.row.flags }}</el-tag>
            <el-tag size="mini" class="pointer" type="info" v-else>{{ scope.row.flags }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="In Cluster" align="center" sortable>
          <template slot-scope="scope">
            <i class="el-icon-success status-icon normal-status" v-if="scope.row.inCluster"></i>
            <i class="el-icon-error status-icon normal-bad" v-else></i>
          </template>
        </el-table-column>
        <el-table-column label="Run Status" align="center" sortable>
          <template slot-scope="scope">
            <i class="el-icon-success status-icon normal-status" v-if="scope.row.runStatus"></i>
            <i class="el-icon-error status-icon normal-bad" v-else></i>
          </template>
        </el-table-column>
        <el-table-column prop="host" label="Host" sortable></el-table-column>
        <el-table-column prop="port" label="Port" sortable></el-table-column>
        <el-table-column prop="slotRange" label="Slot Range" sortable></el-table-column>
        <el-table-column prop="time" label="Time" sortable></el-table-column>
        <el-table-column label="Operation" width="100px">
          <!-- <template slot="header">
              <el-input v-model="search" size="mini" placeholder="Search" />
          </template>-->
          <template slot-scope="scope">
            <el-button size="mini" type="primary">Slave Of</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog
      title="Move Slot"
      :visible.sync="moveSlotVisible"
      :close-on-click-modal="false"
      width="30%"
    >
      <el-form :model="slotRange" size="small">
        <el-form-item label="Start Slot">
          <el-input v-model="slotRange.startSlot"></el-input>
        </el-form-item>
        <el-form-item label="End Slot">
          <el-input v-model="slotRange.endSlot"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" @click="moveSlotVisible = false">Cancel</el-button>
        <el-button size="small" type="primary" @click="moveSlotVisible = false">Confirm</el-button>
      </div>
    </el-dialog>

    <el-dialog
      title="Import New Node"
      :visible.sync="importNodeVisible"
      :close-on-click-modal="false"
      width="30%"
    >
      <el-form :model="newRedisNode" size="small">
        <el-form-item label="Host">
          <el-input v-model="newRedisNode.host"></el-input>
        </el-form-item>
        <el-form-item label="Port">
          <el-input v-model="newRedisNode.port"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" @click="importNodeVisible = false">Cancel</el-button>
        <el-button size="small" type="primary" @click="importNodeVisible = false">Confirm</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { isEmpty } from "@/utils/validate.js";
import { formatTime } from "@/utils/time.js";
import API from "@/api/api.js";
export default {
  data() {
    return {
      cluster: {},
      search: "",
      redisNodeList: [
        // {
        //   nodeId: 1,
        //   status: "ok",
        //   role: "master",
        //   address: "192.16.15.2:8600",
        //   slot: "0-100",
        //   slotNumber: 100,
        //   slotStatus: "",
        //   updateTime: "2019-08-25"
        // },
        // {
        //   nodeId: 2,
        //   status: "ok",
        //   role: "master",
        //   address: "192.16.15.4:8600",
        //   slot: "101-200",
        //   slotNumber: 100,
        //   slotStatus: "",
        //   updateTime: "2019-08-25"
        // },
        // {
        //   nodeId: 3,
        //   status: "ok",
        //   role: "master",
        //   address: "192.16.15.5:8600",
        //   slot: "201-300",
        //   slotNumber: 100,
        //   slotStatus: "",
        //   updateTime: "2019-08-25"
        // },
        // {
        //   nodeId: 4,
        //   status: "ok",
        //   role: "master",
        //   address: "192.16.15.6:8600",
        //   slot: "301-5461",
        //   slotNumber: 5462,
        //   slotStatus: "warning",
        //   updateTime: "2019-08-25",
        //   children: [
        //     {
        //       nodeId: 5,
        //       status: "ok",
        //       role: "slave",
        //       address: "192.16.15.6:8601",
        //       updateTime: "2019-08-25"
        //     }
        //   ]
        // }
      ],
      beSlaveVisible: false,
      moveSlotVisible: false,
      importNodeVisible: false,
      slotRange: {},
      beSlaveNode:{},
      newRedisNode: {}
    };
  },
  methods: {
    tableRowClassName({ row, rowIndex }) {
      if (rowIndex === 1) {
        return "warning-row";
      } else if (rowIndex === 3) {
        return "success-row";
      }
      return "";
    },
    handleSelectionChange(val) {},
    handleEdit(index, row) {
      console.log(index, row);
    },
    handleDelete(index, row) {
      console.log(index, row);
    },
    handleSlot(slot) {
      return "";
    },
    getClusterById(clusterId) {
      let url = "/cluster/getCluster/" + clusterId;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.cluster = result.data;
          } else {
            console.log("Get clsuter failed.");
          }
        },
        err => {
          console.log(err);
        }
      );
    },
    getAllNodeList(clusterId) {
      let url = "/nodeManage/getAllNodeListWithStatus/" + clusterId;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          console.log(result.data);
          if (result.code == 0) {
            let redisNodeList = [];
            let nodeList = result.data;
            let masterRedisNode;
            let children = [];
            nodeList.forEach(node => {
              let flags = node.flags;
              node.time = formatTime(node.insertTime);
              if (flags == "master") {
                children = [];
                masterRedisNode = node;
                masterRedisNode.children = children;
                redisNodeList.push(masterRedisNode);
              } else if (flags == "slave" || flags == "replica") {
                children.push(node);
              } else {
                redisNodeList.push(node);
              }
            });
            this.redisNodeList = redisNodeList;
          } else {
            console.log(result.message);
          }
        },
        err => {
          console.log(err);
        }
      );
    },
    getNodeInfo(clusterId, host, port) {
      console.log(clusterId + " " + host + " " + port);
    },
    getConfig(clusterId, host, port) {
      console.log(clusterId + " " + host + " " + port);
    }
  },

  mounted() {
    let clusterId = this.$route.params.clusterId;
    this.getClusterById(clusterId);
    this.getAllNodeList(clusterId);
  }
};
</script>

<style scoped>
#cluster-manage {
  min-width: 1000px;
}

.manage-header-wrapper {
  padding-bottom: 20px;
  border-bottom: 1px solid #dcdfe6;
}

.base-info-operation-wrapper {
  margin-top: 20px;
  display: flex;
  align-items: center;
}

.health {
  margin-left: 10px;
}

.base-info-item {
  margin-right: 20px;
}

.batch-title {
  margin-bottom: 10px;
  color: #909399;
  font-size: 14px;
}

.nodes-wrapper,
.table-wrapper {
  margin-top: 20px;
}

.el-table .warning-row {
  background: oldlace;
}

.el-table .success-row {
  background: #f0f9eb;
}

.master-line {
  background-color: #f5f7fa;
}

.status-icon {
  font-size: 16px;
}

.normal-status {
  color: #40c9c6;
}

.bad-status {
  color: #f4516c;
}
</style>