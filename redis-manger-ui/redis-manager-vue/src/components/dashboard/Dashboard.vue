<template>
  <div v-loading="clusterListLoading">
    <el-row :gutter="20" class="card-panel-group">
      <el-col :xl="6" :lg="6" :md="12" :sm="12">
        <div class="card-panel">
          <div class="card-panel-icon-wrapper card-panel-icon-user">
            <i class="el-icon-user"></i>
          </div>

          <div class="card-panel-info-wrapper">
            <div class="card-panel-info-key">User Number</div>
            <div class="card-panel-info-value">{{ overview.userNumber }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xl="6" :lg="6" :md="12" :sm="12">
        <div class="card-panel">
          <div class="card-panel-icon-wrapper card-panel-icon-health">
            <i class="el-icon-sunny"></i>
          </div>
          <div class="card-panel-info-wrapper">
            <div class="card-panel-info-key">Health Number</div>
            <div class="card-panel-info-value">{{ overview.healthNumber }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xl="6" :lg="6" :md="12" :sm="12">
        <div class="card-panel">
          <div class="card-panel-icon-wrapper card-panel-icon-bad">
            <i class="el-icon-heavy-rain"></i>
          </div>

          <div class="card-panel-info-wrapper">
            <div class="card-panel-info-key">Bad Number</div>
            <div class="card-panel-info-value">{{ overview.badNumber }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xl="6" :lg="6" :md="12" :sm="12">
        <div class="card-panel">
          <div class="card-panel-icon-wrapper card-panel-icon-alert">
            <i class="el-icon-bell"></i>
          </div>

          <div class="card-panel-info-wrapper">
            <div class="card-panel-info-key">Alert Number</div>
            <div class="card-panel-info-value">{{ overview.alertNumber }}</div>
          </div>
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col
        :xs="24"
        :sm="12"
        :md="12"
        :lg="8"
        :xl="6"
        v-for="cluster in clusterList"
        :key="cluster.clusterId"
      >
        <el-card class="box-card" shadow="hover">
          <div slot="header" class="box-card-title">
            <span>{{ cluster.clusterName }}</span>
            <i
              class="el-icon-lock health"
              style="float: right; padding: 3px 0"
              v-if="cluster.redisPassword != null && cluster.redisPassword != ''"
            ></i>
          </div>
          <div>
            <div class="text item">
              State:
              <el-tag
                size="mini"
                v-if="cluster.clusterState == 'HEALTH'"
                type="success"
              >{{ cluster.clusterState }}</el-tag>
              <el-tag size="mini" v-else type="danger">{{ cluster.clusterState }}</el-tag>
            </div>
            <div class="text item">
              Model:
              <el-tag size="mini">{{ cluster.redisMode }}</el-tag>
            </div>
            <div class="text item">
              Version:
              <el-tag size="mini">{{ cluster.redisVersion }}</el-tag>
            </div>
            <div class="text item">
              Master:
              <el-tag size="mini">{{ cluster.clusterSize }}</el-tag>
            </div>
            <div class="text item">
              Nodes:
              <el-tag size="mini">{{ cluster.clusterKnownNodes }}</el-tag>
            </div>
            <div class="text item" v-if="cluster.redisMode == 'cluster'">
              Slots Assigned(ok/assigned):
              <el-tag size="mini">{{ cluster.clusterSlotsOk }}/{{ cluster.clusterSlotsAssigned }}</el-tag>
            </div>
            <!-- <div class="text item" v-if="cluster.redisMode == 'cluster'">
              Slots Bad(pfail/fail):
              <el-tag size="mini">{{ cluster.clusterSlotsPfail }}/{{ cluster.clusterSlotsFail }}</el-tag>
            </div>-->
            <div class="text item" v-if="cluster.redisMode == 'standalone'">
              DB Size:
              <el-tag size="mini">{{ cluster.dbSize }}</el-tag>
            </div>
            <div class="text item">
              Environment:
              <el-tag size="mini" v-if="cluster.installationEnvironment == 0">Docker</el-tag>
              <el-tag size="mini" v-else-if="cluster.installationEnvironment == 1">Machine</el-tag>
            </div>
            <div class="text item">
              From:
              <el-tag size="mini" v-if="cluster.installationType == 0">Redis Manager</el-tag>
              <el-tag size="mini" v-else>Import</el-tag>
            </div>
          </div>
          <div class="card-bottom">
            <el-button
              size="mini"
              title="Query"
              icon="el-icon-search"
              circle
              @click="handleQuery(cluster)"
            ></el-button>
            <el-button
              size="mini"
              title="Monitor"
              type="primary"
              icon="el-icon-view"
              circle
              @click="toMonitor(cluster.clusterId)"
            ></el-button>
            <el-button
              size="mini"
              title="Alert"
              type="warning"
              icon="el-icon-bell"
              circle
              @click="toAlertManage(cluster.clusterId)"
              v-if="currentUser.userRole < 2"
            ></el-button>

            <el-button
              size="mini"
              title="Manage"
              type="danger"
              icon="el-icon-setting"
              circle
              @click="toManage(cluster.clusterId)"
              v-if="currentUser.userRole < 2"
            ></el-button>

            <el-dropdown trigger="click" class="more-operation" v-if="currentUser.userRole < 2">
              <el-button size="mini" title="Edit or delete" type="info" icon="el-icon-more" circle></el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item
                  icon="el-icon-edit-outline"
                  class="edit"
                  @click.native="editCluster(cluster.clusterId)"
                >Edit</el-dropdown-item>
                <el-dropdown-item
                  icon="el-icon-delete"
                  class="delete"
                  @click.native="showDeleteCluster(cluster.clusterId)"
                >Delete</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-dialog
      :title="'Query ' + cluster.clusterName"
      :visible.sync="queryVisible"
      v-if="queryVisible"
      :close-on-click-modal="false"
      width="50%"
    >
      <query :clusterId="queryClusterId"></query>
    </el-dialog>
    <el-dialog
      title="Edit Cluster"
      :visible.sync="editClusterVisible"
      :close-on-click-modal="false"
      v-if="editClusterVisible"
    >
      <editCluster @closeDialog="closeEditClusterDialog" :clusterId="editClusterId"></editCluster>
    </el-dialog>
    <el-dialog title="Delete Cluster" :visible.sync="deleteClusterVisible" width="30%">
      <span>
        Are you sure to delete
        <b>{{ cluster.clusterName }}</b> ?
      </span>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="dialogVisible = false">Cancel</el-button>
        <el-button size="small" type="danger" @click="deleteCluster(cluster.clusterId)">Delete</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import query from "@/components/tool/Query";
import editCluster from "@/components/manage/EditCluster";
import { store } from "@/vuex/store.js";
import { isEmpty } from "@/utils/validate.js";
import API from "@/api/api.js";
import message from "@/utils/message.js"
export default {
  components: {
    query,
    editCluster
  },
  data() {
    return {
      overview: {
        userNumber: 0,
        healthNumber: 0,
        badNumber: 0,
        alertNumber: 0
      },
      clusterList: [],
      cluster: {},
      queryClusterId: "",
      queryVisible: false,
      editClusterId: "",
      editClusterVisible: false,
      deleteClusterVisible: false,
      clusterListLoading: false
    };
  },

  methods: {
    toMonitor(clusterId) {
      this.$router.push({
        name: "redis-monitor",
        params: { clusterId: clusterId }
      });
    },
    toManage(clusterId) {
      this.$router.push({
        name: "redis-manage",
        params: {
          clusterId: clusterId
        }
      });
    },
    toAlertManage(clusterId) {
      this.$router.push({
        name: "alert-manage",
        params: {
          clusterId: clusterId
        }
      });
    },
    handleQuery(cluster) {
      this.queryClusterId = cluster.clusterId;
      this.cluster = cluster;
      this.queryVisible = true;
    },
    getOverview(groupId) {
      let url = "/group/overview/" + groupId;
      if (!isEmpty(groupId)) {
        API.get(
          url,
          null,
          response => {
            let result = response.data;
            if (result.code == 0) {
              let overview = result.data;
              this.overview.alertNumber = overview.alertNumber;
              this.overview.userNumber = overview.userNumber;
            } else {
              message.error("Get group overview failed");
            }
          },
          err => {
            message.error(err);
          }
        );
      }
    },
    getClusterList(groupId) {
      let url = "/cluster/getClusterList/" + groupId;
      this.clusterListLoading = true;
      if (!isEmpty(groupId)) {
        API.get(
          url,
          null,
          response => {
            let result = response.data;
            if (result.code == 0) {
              let clusterList = result.data;
              let healthNumber = 0;
              let badNumber = 0;
              clusterList.forEach(cluster => {
                if (cluster.clusterState == "HEALTH") {
                  healthNumber++;
                } else {
                  badNumber++;
                }
              });
              this.overview.healthNumber = healthNumber;
              this.overview.badNumber = badNumber;
              this.clusterList = clusterList;
            } else {
              message.error("Get cluster list failed");
            }
            this.clusterListLoading = false;
          },
          err => {
            this.clusterListLoading = false;
            message.error(err);
          }
        );
      }
    },
    editCluster(clusterId) {
      this.editClusterId = clusterId;
      this.editClusterVisible = true;
    },
    showDeleteCluster(clusterId) {
      this.clusterList.forEach(cluster => {
        if (cluster.clusterId == clusterId) {
          this.cluster = cluster;
          this.deleteClusterVisible = true;
        }
      });
    },
    deleteCluster(clusterId) {
      let url = "/cluster/deleteCluster";
      API.post(
        url,
        { clusterId: clusterId },
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.cluster = {};
            this.getClusterList(this.currentGroupId);
            this.deleteClusterVisible = false;
          } else {
            message.error("Delete cluster failed");
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    closeEditClusterDialog(editClusterVisible) {
      this.editClusterVisible = editClusterVisible;
      this.getClusterList(this.currentGroupId)
    }
  },
  computed: {
    currentGroupId() {
      return store.getters.getCurrentGroup.groupId;
    },
    currentUser() {
      return store.getters.getUser;
    }
  },
  watch: {
    currentGroupId(groupId) {
      this.getClusterList(groupId);
      this.getOverview(groupId);
      this.$router.push({
        name: "dashboard",
        params: { groupId: groupId }
      });
    }
  },
  mounted() {
    let groupId = this.$route.params.groupId;
    this.getClusterList(groupId);
    this.getOverview(groupId);
    this.userRole = store.getters.getUserRole;
  }
};
</script>

<style scoped>
.card-panel-group {
  margin-bottom: 0px !important;
}
.card-panel {
  margin-bottom: 1.5rem;
  padding: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 4px;
  min-height: 36px;
  background-color: #ffffff;
}

.card-panel-icon-wrapper {
  font-size: 3rem;
  width: 5rem;
  height: 5rem;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  border-radius: 4px;
  /* 背景色变化动画 */
  transition: all 0.38s ease-out;
}

.card-panel-icon-user {
  color: #36a3f7;
}

.card-panel-icon-user:hover {
  background-color: #36a3f7;
  color: #ffffff;
}

.card-panel-icon-health {
  color: #40c9c6;
}

.card-panel-icon-health:hover {
  background-color: #40c9c6;
  color: #ffffff;
}

.card-panel-icon-bad {
  color: #f4516c;
}

.card-panel-icon-bad:hover {
  background-color: #f4516c;
  color: #ffffff;
}

.card-panel-icon-alert {
  color: #ffb980;
}

.card-panel-icon-alert:hover {
  background-color: #ffb980;
  color: #ffffff;
}

.card-panel-info-wrapper {
  padding: 0 0.5rem;
  font-weight: bold;
  text-align: center;
}

.card-panel-info-key {
  color: #8c8c8c;
  padding: 4px 0;
  font-size: 14px;
}

.card-panel-info-value {
  color: #666;
  padding: 4px 0;
  font-size: 20px;
}

/* cluster card */
.text {
  font-size: 13px;
}

.item {
  margin-bottom: 10px;
}

/* .clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}

.clearfix:after {
  clear: both;
} */

.box-card {
  margin-bottom: 20px;
}

.box-card-title {
  font-weight: bold;
}

.card-bottom {
  display: flex;
  justify-content: flex-end;
}

.more-operation {
  margin-left: 12px;
}

.edit {
  color: #409eff;
}

.delete {
  color: #f56c6c;
}
.delete:hover {
  background-color: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.el-row {
  margin-bottom: 20px;
}
.el-col {
  border-radius: 4px;
}
</style>
