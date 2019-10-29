<template>
  <div id="alert-manage">
    <div class="overview-wrapper">
      <el-row :gutter="24">
        <el-col :xl="8" :lg="8" :md="12" :sm="12">
          <div class="card-panel">
            <div class="card-panel-icon-wrapper card-panel-icon-alert">
              <i class="el-icon-bell"></i>
            </div>
            <div class="card-panel-info-wrapper">
              <div class="card-panel-info-key">Alert Record</div>
              <div class="card-panel-info-value">{{ alertRecordList.length }}</div>
            </div>
          </div>
        </el-col>
        <el-col :xl="8" :lg="8" :md="12" :sm="12">
          <div class="card-panel">
            <div class="card-panel-icon-wrapper card-panel-icon-rule">
              <i class="el-icon-notebook-2"></i>
            </div>
            <div class="card-panel-info-wrapper">
              <div class="card-panel-info-key">Alert Rule</div>
              <div class="card-panel-info-value">{{ alertRuleList.length }}</div>
            </div>
          </div>
        </el-col>
        <el-col :xl="8" :lg="8" :md="12" :sm="12">
          <div class="card-panel">
            <div class="card-panel-icon-wrapper card-panel-icon-channel">
              <i class="el-icon-coin"></i>
            </div>
            <div class="card-panel-info-wrapper">
              <div class="card-panel-info-key">Alert Channel</div>
              <div class="card-panel-info-value">{{ alertChannelList.length }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    <div class="body-wrapper">
      <div class="title-wrapper">
        <span>{{ cluster.clusterName }}</span>
        <i class="el-icon-sunny health" title="Status" v-if="cluster.clusterState == 'HEALTH'"></i>
      </div>
      <el-tabs v-model="activeName" @tab-click="handleClick">
        <el-tab-pane label="Alert Record" name="record">
          <div class="operation-wrapper">
            <div class="batch-title">Batch Operation</div>
            <div style="display: flex; justify-content: space-between;">
              <div>
                <el-link
                  type="danger"
                  :underline="false"
                  icon="el-icon-delete"
                  @click="handleDeleteAlertRecordBatch"
                >Delete</el-link>
              </div>
            </div>
          </div>
          <el-table
            :data="alertRecordList"
            @selection-change="handleRecordSelectionChange"
            :default-sort="{prop: 'time', order: 'descending'}"
          >
            <el-table-column type="selection" width="55"></el-table-column>
            <el-table-column type="index" width="50"></el-table-column>
            <el-table-column property="redisNode" label="Redis Node"></el-table-column>
            <el-table-column property="alertRule" label="Alert Rule"></el-table-column>
            <el-table-column property="actualData" label="Actual Data"></el-table-column>
            <el-table-column label="Is Global">
              <template slot-scope="scope">
                <el-tag size="small" v-if="scope.row.global">Global</el-tag>
              </template>
            </el-table-column>
            <el-table-column property="checkCycle" label="Check Cycle"></el-table-column>
            <el-table-column property="ruleInfo" label="Info"></el-table-column>
            <el-table-column property="time" label="Time"></el-table-column>
            <el-table-column label="Operation" width="100px;">
              <template slot-scope="scope">
                <el-button
                  size="mini"
                  type="danger"
                  @click="handleDeleteAlertRecord(scope.row)"
                >Delete</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="Alert Rule" name="rule">
          <div class="operation-wrapper">
            <div class="batch-title">Batch Operation</div>
            <div style="display: flex; justify-content: space-between;">
              <div>
                <el-link type="danger" :underline="false" icon="el-icon-delete">Delete</el-link>
              </div>
              <el-link
                :underline="false"
                icon="el-icon-plus"
                type="primary"
                @click="getAlertRuleListNotUsed(cluster.clusterId)"
              >Add Rule</el-link>
            </div>
          </div>
          <el-table :data="alertRuleList" :default-sort="{prop: 'time', order: 'descending'}">
            <el-table-column type="index" width="50"></el-table-column>
            <el-table-column label="Alert Rule">
              <template
                slot-scope="scope"
              >{{ scope.row.ruleKey }}{{ scope.row.compareSign }}{{ scope.row.ruleValue }}</template>
            </el-table-column>
            <el-table-column label="Rule Status">
              <template slot-scope="scope">
                <el-tag size="mini" type="success" v-if="scope.row.valid">valid</el-tag>
                <el-tag size="mini" type="danger" v-else>Invalid</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Is Global">
              <template slot-scope="scope">
                <el-tag size="small" v-if="scope.row.global">Global</el-tag>
              </template>
            </el-table-column>

            <el-table-column property="ruleInfo" label="Info"></el-table-column>
            <el-table-column property="time" label="Time"></el-table-column>
            <el-table-column label="Operation" width="100px;">
              <template slot-scope="scope" v-if="!scope.row.global">
                <el-button
                  size="mini"
                  type="danger"
                  @click="handleDeleteAlertRule(scope.row)"
                >Delete</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="Alert Channel" name="channel">
          <div class="operation-wrapper">
            <div class="batch-title">Batch Operation</div>
            <div style="display: flex; justify-content: space-between;">
              <div>
                <el-link type="danger" :underline="false" icon="el-icon-delete">Delete</el-link>
              </div>
              <el-link
                :underline="false"
                icon="el-icon-plus"
                type="primary"
                @click="getAlertChannelListNotUsed(cluster.clusterId)"
              >Add Channel</el-link>
            </div>
          </div>
          <el-table :data="alertChannelList" :default-sort="{prop: 'time', order: 'descending'}">
            <el-table-column type="index" width="50"></el-table-column>
            <!-- <el-table-column type="expand">
              <template slot-scope="props">
                <el-form label-position="left" inline class="table-expand">
                  <div v-if="props.row.channelType == 0">
                    <el-form-item label="Email From">
                      <span>{{ props.row.emailFrom }}</span>
                    </el-form-item>
                    <el-form-item label="Email To">
                      <span>{{ props.row.emailTo }}</span>
                    </el-form-item>
                  </div>
                  <div v-if="props.row.channelType == 1 || props.row.channelType == 2">
                    <el-form-item label="Web Hook">
                      <span>{{ props.row.webhook }}</span>
                    </el-form-item>
                  </div>
                  <div v-if="props.row.channelType == 3"></div>
                  <el-form-item label>
                    <span>{{ props.row.name }}</span>
                  </el-form-item>
                </el-form>
              </template>
            </el-table-column>-->
            <el-table-column property="channelName" label="Channel Name"></el-table-column>
            <el-table-column label="Channel Type">
              <template slot-scope="scope">
                <el-popover trigger="hover" placement="top" v-if="scope.row.channelType == '0'">
                  <p>
                    <span class="label-color">Email From:</span>
                    {{ scope.row.emailFrom }}
                  </p>
                  <p>
                    <span class="label-color">Email To:</span>
                    {{ scope.row.emailTo }}
                  </p>
                  <div slot="reference" class="name-wrapper">
                    <el-tag size="small" type="success">Email</el-tag>
                  </div>
                </el-popover>
                <el-popover trigger="hover" placement="top" v-if="scope.row.channelType == '1'">
                  <p>
                    <span class="label-color">Web Hook:</span>
                    {{ scope.row.webhook }}
                  </p>
                  <div slot="reference" class="name-wrapper">
                    <el-tag size="small" type="success">WebChat Webhook</el-tag>
                  </div>
                </el-popover>
                <el-popover trigger="hover" placement="top" v-if="scope.row.channelType == '2'">
                  <p>
                    <span class="label-color">Web Hook:</span>
                    {{ scope.row.webhook }}
                  </p>
                  <div slot="reference" class="name-wrapper">
                    <el-tag size="small" type="primary">DingDing Webhook</el-tag>
                  </div>
                </el-popover>
                <el-popover trigger="hover" placement="top" v-if="scope.row.channelType == '3'">
                  <p>
                    <span class="label-color">Corp ID:</span>
                    {{ scope.row.corpId }}
                  </p>
                  <p>
                    <span class="label-color">Agent ID:</span>
                    {{ scope.row.agentId }}
                  </p>
                  <p>
                    <span class="label-color">Corp Secret:</span>
                    {{ scope.row.corpSecret }}
                  </p>
                  <div slot="reference" class="name-wrapper">
                    <el-tag size="small" type="success">WebChat App</el-tag>
                  </div>
                </el-popover>
              </template>
            </el-table-column>
            <el-table-column property="channelInfo" label="Info"></el-table-column>
            <el-table-column property="time" label="Time"></el-table-column>
            <el-table-column label="Operation" width="100px;">
              <template slot-scope="scope">
                <el-button
                  size="mini"
                  type="danger"
                  @click="handleDeleteAlertChannel(scope.row)"
                >Delete</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
    <el-dialog title="Add Alert Rule" :visible.sync="addAlertRuleVisible">
      <el-table :data="alertRuleListNotUsed" stripe @selection-change="addRuleSelectionChange">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column label="Alert Rule">
          <template
            slot-scope="scope"
          >{{ scope.row.ruleKey }}{{ scope.row.compareSign }}{{ scope.row.ruleValue }}</template>
        </el-table-column>
        <el-table-column property="checkCycle" label="Check Cycle(Min)"></el-table-column>
        <el-table-column property="ruleInfo" label="Info"></el-table-column>
        <el-table-column property="time" label="Time"></el-table-column>
      </el-table>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" type="primary" @click="addAlertRule()">Confirm</el-button>
      </span>
    </el-dialog>

    <el-dialog title="Delete Alert Rule" :visible.sync="deleteAlertRuleVisible">
      Are you sure to delete
      <b>{{ deletingAlertRule.ruleKey }} {{ deletingAlertRule.compareSign }} {{ deletingAlertRule.ruleValue }}</b> ?
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="deleteAlertRuleVisible = false">Cancel</el-button>
        <el-button size="small" type="primary" @click="deleteAlertRule()">Confirm</el-button>
      </span>
    </el-dialog>

    <el-dialog title="Add Alert Channel" :visible.sync="addAlertChannelVisible">
      <el-table :data="alertRuleListNotUsed" stripe @selection-change="addChannelSelectionChange">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column property="channelName" label="Channel Name" width="150px;"></el-table-column>
        <el-table-column label="Channel Type" width="200px;">
          <template slot-scope="scope">
            <el-popover trigger="hover" placement="top" v-if="scope.row.channelType == '0'">
              <p>
                <span class="label-color">Email From:</span>
                {{ scope.row.emailFrom }}
              </p>
              <p>
                <span class="label-color">Email To:</span>
                {{ scope.row.emailTo }}
              </p>
              <div slot="reference" class="name-wrapper">
                <el-tag size="small" type="success">Email</el-tag>
              </div>
            </el-popover>
            <el-popover trigger="hover" placement="top" v-if="scope.row.channelType == '1'">
              <p>
                <span class="label-color">Web Hook:</span>
                {{ scope.row.webhook }}
              </p>
              <div slot="reference" class="name-wrapper">
                <el-tag size="small" type="success">WebChat Webhook</el-tag>
              </div>
            </el-popover>
            <el-popover trigger="hover" placement="top" v-if="scope.row.channelType == '2'">
              <p>
                <span class="label-color">Web Hook:</span>
                {{ scope.row.webhook }}
              </p>
              <div slot="reference" class="name-wrapper">
                <el-tag size="small" type="primary">DingDing Webhook</el-tag>
              </div>
            </el-popover>
            <el-popover trigger="hover" placement="top" v-if="scope.row.channelType == '3'">
              <div slot="reference" class="name-wrapper">
                <el-tag size="small" type="success">WebChat App</el-tag>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column property="channelInfo" label="Info"></el-table-column>
        <el-table-column property="updateTime" label="Time"></el-table-column>
      </el-table>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" type="primary" @click="addAlertChannel()">Confirm</el-button>
      </span>
    </el-dialog>

    <el-dialog title="Delete Alert Channel" :visible.sync="deleteAlertChannelVisible" width="30%">
      <span>
        Are you sure to delete
        <b>{{ deletingAlertChannel.channelName }}</b> ?
      </span>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="deleteAlertChannelVisible = false">Cancel</el-button>
        <el-button size="small" type="danger" @click="deleteAlertChannel()">Delete</el-button>
      </span>
    </el-dialog>

    <el-dialog title="Delete Alert Record" :visible.sync="deleteAlertRecordVisible" width="30%">
      <span>Are you sure to delete?</span>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="deleteAlertChannelVisible = false">Cancel</el-button>
        <el-button size="small" type="danger" @click="deleteAlertRecord()">Delete</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { store } from "@/vuex/store.js";
import { isEmpty } from "@/utils/validate.js";
import API from "@/api/api.js";
import { formatTime } from "@/utils/time.js";
import { getClusterById } from "@/components/cluster/cluster.js";
import message from "@/utils/message.js";
export default {
  data() {
    return {
      cluster: {},
      alertRecordList: [],
      alertRuleList: [
        {
          alertKey: "test",
          alertValue: "12",
          compareType: ">",
          checkCycle: "5",
          valid: true,
          global: true,
          ruleInfo: "test",
          updateTime: "2019-08-25"
        }
      ],
      alertRuleListNotUsed: [],
      alertChannelList: [
        {
          channelName: "bigdata",
          channelType: "1",
          channelInfo: "bigdata alert",
          updateTime: "2019-08-25"
        }
      ],
      activeName: "record",
      addAlertRuleVisible: false,
      addAlertChannelVisible: false,
      deleteAlertRuleVisible: false,
      deleteAlertChannelVisible: false,
      deleteAlertRecordVisible: false,
      deletingAlertRule: {},
      deletingAlertChannel: {},
      deletingAlertRecordIds: []
    };
  },
  methods: {
    handleClick(tab, event) {
      //console.log(tab, event);
    },
    addRuleSelectionChange(val) {
      let newRuleIds = "";
      if (val.length > 0) {
        val.forEach(alertRule => {
          newRuleIds += alertRule.ruleId;
          newRuleIds += ",";
        });
      }
      this.cluster.ruleIds = newRuleIds;
    },
    addChannelSelectionChange(val) {
      let newChannelIds = "";
      if (val.length > 0) {
        val.forEach(alertChannel => {
          newChannelIds += alertChannel.channelId;
          newChannelIds += ",";
        });
      }
      this.cluster.channelIds = newChannelIds;
    },
    getAlertRecordList(clusterId) {
      let url = "/alert/record/getAlertRecord/cluster/" + clusterId;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            let alertRecordList = result.data;
            if (alertRecordList == null || alertRecordList.length == 0) {
              this.alertRecordList = alertRecordList;
              return;
            }
            alertRecordList.forEach(alertRecord => {
              alertRecord.time = formatTime(alertRecord.updateTime);
            });
            this.alertRecordList = alertRecordList;
          } else {
            message.error("Get alert record list failed");
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    getAlertRuleList(clusterId) {
      let url = "/alert/rule/getAlertRule/cluster/" + clusterId;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            let alertRuleList = result.data;
            this.alertRuleList = this.buildAlertRuleList(alertRuleList);
          } else {
            message.error("Get alert rule list failed");
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    getAlertRuleListNotUsed(clusterId) {
      let url = "/alert/rule/getAlertRuleNotUsed/" + clusterId;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.alertRuleListNotUsed = this.buildAlertRuleList(result.data);
            this.addAlertRuleVisible = true;
          } else {
            message.error("Get alert rule list not used failed");
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    buildAlertRuleList(alertRuleList) {
      if (alertRuleList == null || alertRuleList.length == 0) {
        return [];
      }
      alertRuleList.forEach(alertRule => {
        alertRule.time = formatTime(alertRule.updateTime);
        let compareType = alertRule.compareType;
        alertRule.compareSign =
          compareType == 0
            ? "="
            : compareType == 1
            ? ">"
            : compareType == -1
            ? "<"
            : "!=";
      });
      return alertRuleList;
    },
    addAlertRule() {
      if (isEmpty(this.cluster.ruleIds)) {
        message.warning("No rule");
        return;
      }
      let url = "/cluster/addAlertRule";
      API.post(
        url,
        this.cluster,
        response => {
          let result = response.data;
          let clusterId = this.cluster.clusterId;
          this.getAlertRuleList(clusterId);
          if (result.code == 0) {
            this.addAlertRuleVisible = false;
          } else {
            message.error("Save alert rule failed");
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    handleDeleteAlertRule(alertRule) {
      this.cluster.ruleIds = alertRule.ruleId;
      this.deletingAlertRule = alertRule;
      this.deleteAlertRuleVisible = true;
    },
    deleteAlertRule() {
      let url = "/cluster/deleteAlertRule";
      API.post(
        url,
        this.cluster,
        response => {
          let result = response.data;
          let clusterId = this.cluster.clusterId;
          this.getAlertRuleList(clusterId);
          if (result.code == 0) {
            this.deleteAlertRuleVisible = false;
          } else {
            message.error("Delete alert rule failed");
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    getAlertChannelList(clusterId) {
      let url = "/alert/channel/getAlertChannel/cluster/" + clusterId;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.alertChannelList = this.buildAlertChannelList(result.data);
          } else {
            message.error("Get alert channel list failed");
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    getAlertChannelListNotUsed(clusterId) {
      let url = "/alert/channel/getAlertChannelNotUsed/" + clusterId;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.alertRuleListNotUsed = this.buildAlertChannelList(result.data);
            this.addAlertChannelVisible = true;
          } else {
            message.error("Get alert channel list not used failed");
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    buildAlertChannelList(alertChannelList) {
      if (alertChannelList == null || alertChannelList.length == 0) {
        return [];
      }
      alertChannelList.forEach(alertChannel => {
        alertChannel.time = formatTime(alertChannel.updateTime);
      });
      return alertChannelList;
    },
    addAlertChannel() {
      if (isEmpty(this.cluster.channelIds)) {
        console.log("no channels");
        return;
      }
      let url = "/cluster/addAlertChannel";
      API.post(
        url,
        this.cluster,
        response => {
          let result = response.data;
          let clusterId = this.cluster.clusterId;
          this.getAlertChannelList(clusterId);
          if (result.code == 0) {
            this.addAlertChannelVisible = false;
          } else {
            message.error("Save alert channel failed");
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    handleDeleteAlertChannel(alertChannel) {
      this.cluster.channelIds = alertChannel.channelId;
      this.deletingAlertChannel = alertChannel;
      this.deleteAlertChannelVisible = true;
    },
    deleteAlertChannel() {
      let url = "/cluster/deleteAlertChannel";
      API.post(
        url,
        this.cluster,
        response => {
          let result = response.data;
          let clusterId = this.cluster.clusterId;
          this.getAlertChannelList(clusterId);
          if (result.code == 0) {
            this.deleteAlertChannelVisible = false;
          } else {
            message.error("Delete alert channel failed");
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    handleDeleteAlertRecord(alertRecord) {
      this.deletingAlertRecordIds = [];
      this.deletingAlertRecordIds.push(alertRecord.recordId);
      this.deleteAlertRecordVisible = true;
    },
    handleRecordSelectionChange(val) {
      if (val.length == 0) {
        return;
      }
      this.deletingAlertRecordIds = [];
      val.forEach(alertRecord => {
        this.deletingAlertRecordIds.push(alertRecord.recordId);
      });
    },
    handleDeleteAlertRecordBatch() {
      if (this.deletingAlertRecordIds.length == 0) {
        return;
      }
      this.deleteAlertRecordVisible = true;
    },
    deleteAlertRecord() {
      let url = "/alert/record/deleteAlertRecordBatch";
      API.post(
        url,
        this.deletingAlertRecordIds,
        response => {
          let result = response.data;
          let clusterId = this.cluster.clusterId;
          this.getAlertRecordList(clusterId);
          if (result.code == 0) {
            this.deleteAlertRecordVisible = false;
          } else {
            message.error("Delete alert record failed");
          }
        },
        err => {
          message.error(err);
        }
      );
    }
  },
  computed: {
    // 监听group变化
    currentGroup() {
      return store.getters.getCurrentGroup;
    }
  },
  mounted() {
    let clusterId = this.$route.params.clusterId;
    getClusterById(clusterId, cluster => {
      this.cluster = cluster;
      this.getAlertRecordList(clusterId);
      this.getAlertRuleList(clusterId);
      this.getAlertChannelList(clusterId);
    });
  }
};
</script>
<style scoped>
.body-wrapper {
  min-width: 1000px;
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

.card-panel-icon-alert {
  color: #ffb980;
}

.card-panel-icon-alert:hover {
  background-color: #ffb980;
  color: #ffffff;
}

.card-panel-icon-rule {
  color: #36a3f7;
}

.card-panel-icon-rule:hover {
  background-color: #36a3f7;
  color: #ffffff;
}

.card-panel-icon-channel {
  color: #40c9c6;
}

.card-panel-icon-channel:hover {
  background-color: #40c9c6;
  color: #ffffff;
}

.card-panel-info-value {
  color: #666;
  padding: 4px 0;
  font-size: 20px;
}

.operation-wrapper {
  padding-bottom: 20px;
  margin-bottom: 20px;
  border-bottom: 1px solid #dcdfe6;
}

.batch-title {
  margin-bottom: 10px;
  color: #909399;
  font-size: 14px;
}

.table-expand {
  font-size: 0;
}

.table-expand label {
  width: 90px;
  color: #99a9bf;
}

.table-expand .el-form-item {
  margin-right: 0;
  margin-bottom: 0;
}

.label-color {
  color: #99a9bf;
}
</style>