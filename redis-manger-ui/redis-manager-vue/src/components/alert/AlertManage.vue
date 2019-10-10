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
              <div class="card-panel-info-value">6</div>
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
              <div class="card-panel-info-value">23</div>
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
              <div class="card-panel-info-value">1</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    <div class="body-wrapper">
      <div class="title-wrapper">
        <span>Shanghai online</span>
        <i class="el-icon-sunny health" title="Status"></i>
      </div>
      <el-tabs v-model="activeName" @tab-click="handleClick">
        <el-tab-pane label="Alert Record" name="record">
          <div class="operation-wrapper">
            <div class="batch-title">Batch Operation</div>
            <el-button size="mini" type="danger">Delete</el-button>
          </div>
          <el-table :data="alertRecordList">
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
                  @click="handleDelete(scope.$index, scope.row)"
                >Delete</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="Alert Rule" name="rule">
          <div class="operation-wrapper">
            <div class="batch-title">Batch Operation</div>
            <el-button size="mini" type="success" @click="addAlertRuleVisible = true">Add</el-button>
            <el-button size="mini" type="danger">Delete</el-button>
          </div>
          <el-table :data="alertRuleList">
            <el-table-column type="index" width="50"></el-table-column>
            <el-table-column label="Alert Rule" width="200px;">
              <template
                slot-scope="scope"
              >{{scope.row.alertKey}}{{scope.row.compareType}}{{scope.row.alertValue}}</template>
            </el-table-column>
            <el-table-column label="Rule Status" width="100px;">
              <template slot-scope="scope">
                <el-tag size="mini" type="success" v-if="scope.row.valid">valid</el-tag>
                <el-tag size="mini" type="danger" v-else>Invalid</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Is Global" width="100px;">
              <template slot-scope="scope">
                <el-tag size="small" v-if="scope.row.global">Global</el-tag>
              </template>
            </el-table-column>

            <el-table-column property="ruleInfo" label="Info"></el-table-column>
            <el-table-column property="time" label="Time"></el-table-column>
            <el-table-column label="Operation" width="250px;">
              <template slot-scope="scope">
                <el-button size="mini" @click="handleView(scope.$index, scope.row)">View</el-button>
                <el-button
                  size="mini"
                  type="danger"
                  @click="handleDelete(scope.$index, scope.row)"
                >Delete</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="Alert Channel" name="channel">
          <div class="operation-wrapper">
            <div class="batch-title">Batch Operation</div>
            <el-button size="mini" type="success" @click="addAlertChannelVisible = true">Add</el-button>
            <el-button size="mini" type="danger">Delete</el-button>
          </div>
          <el-table :data="alertChannelList" style="width: 100%">
            <el-table-column type="index" width="50"></el-table-column>
            <el-table-column property="channelName" label="Channel Name" width="150px;"></el-table-column>
            <el-table-column label="Channel Type" width="200px;">
              <template slot-scope="scope">
                <el-tag
                  size="small"
                  type="success"
                  v-if="scope.row.channelType == '1'"
                >WebChat Webhook</el-tag>
              </template>
            </el-table-column>
            <el-table-column property="channelInfo" label="Info"></el-table-column>
            <el-table-column property="time" label="Time"></el-table-column>
            <el-table-column label="Operation" width="200px;">
              <template slot-scope="scope">
                <el-button size="mini" @click="handleView(scope.$index, scope.row)">View</el-button>
                <el-button
                  size="mini"
                  type="danger"
                  @click="handleDelete(scope.$index, scope.row)"
                >Delete</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
    <el-dialog title="Add Alert Rule" :visible.sync="addAlertRuleVisible">
      <el-table :data="alertRuleList">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column label="Alert Rule">
          <template
            slot-scope="scope"
          >{{scope.row.alertKey}}{{scope.row.compareType}}{{scope.row.alertValue}}</template>
        </el-table-column>
        <el-table-column property="checkCycle" label="Check Cycle"></el-table-column>
        <el-table-column property="ruleInfo" label="Info"></el-table-column>
        <el-table-column property="updateTime" label="Time"></el-table-column>
      </el-table>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" type="primary" @click="dialogVisible = false">Confirm</el-button>
      </span>
    </el-dialog>
    <el-dialog title="Add Alert Channel" :visible.sync="addAlertChannelVisible">
      <el-table :data="alertChannelList">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column property="channelName" label="Channel Name" width="150px;"></el-table-column>
        <el-table-column label="Channel Type" width="200px;">
          <template slot-scope="scope">
            <el-popover trigger="hover" placement="top">
              <p>info1: {{ scope.row.channelType }}</p>
              <p>info2: {{ scope.row.channelType }}</p>
              <div slot="reference" class="name-wrapper">
                <el-tag
                  size="small"
                  type="success"
                  v-if="scope.row.channelType == '1'"
                >WebChat Webhook</el-tag>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column property="channelInfo" label="Info"></el-table-column>
        <el-table-column property="updateTime" label="Time"></el-table-column>
      </el-table>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" type="primary" @click="dialogVisible = false">Confirm</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { store } from "@/vuex/store.js";
import { isEmpty } from "@/utils/validate.js";
import API from "@/api/api.js";
export default {
  data() {
    return {
      alertRecordList: [
        {
          redisNode: "192.168.0.5:8000",
          alertRuleL: "test>8",
          actualData: "test=9",
          checkCycle: "5"
        }
      ],
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
      unaddedRules: [],
      unaddedChannels: []
    };
  },
  methods: {
    handleClick(tab, event) {
      console.log(tab, event);
    },
    handleView(index, row) {
      console.log(index, row);
    },
    handleEdit(index, row) {
      console.log(index, row);
    },
    handleDelete(index, row) {
      console.log(index, row);
    },
    getAlertRecordList(clusterId) {
      let url = "/alert/record/getAlertRecordList/" + clusterId;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          console.log(result);
          if (result.code == 0) {
            let alertRecordList = result.data;
            alertRecordList.forEach(alertRecord => {
              alertRecord.time = formatTime(alertRecord.updateTime);
            });
            this.alertRecordList = alertRecordList;
          } else {
            console.log("No data");
          }
        },
        err => {
          console.log(err);
        }
      );
    },
    getAlertRuleList(clusterId) {
      let url = "/alert/rule/getAlertRuleListByClusterId/" + clusterId;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            let alertRuleList = result.data;
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
            this.alertRuleList = alertRuleList;
          } else {
            console.log("Get alert rule list failed");
          }
        },
        err => {
          console.log(err);
        }
      );
    },
    getChannelList(clusterId) {
      let url = "/alert/channel/getAlertChannelListByClusterId/" + clusterId;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            let alertChannelList = result.data;
            alertChannelList.forEach(alertChannel => {
              alertChannel.time = formatTime(alertChannel.updateTime);
            });
            this.alertChannelList = alertChannelList;
          } else {
            console.log("No data");
          }
        },
        err => {
          console.log(err);
        }
      );
    }
  },
  mounted() {
    let clusterId = this.$route.params.clusterId;
    this.getAlertRecordList(clusterId);
    this.getAlertRuleList(clusterId);
    this.getChannelList(clusterId);
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
</style>