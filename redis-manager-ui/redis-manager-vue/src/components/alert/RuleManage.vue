<template>
  <div id="rule-manage" class="body-wrapper">
    <div class="header-wrapper">
      <div>Bigdata</div>
      <div>
        <el-button size="mini" type="success" @click="createAlertRule()">Create</el-button>
      </div>
    </div>
    <div>
      <el-table :data="alertRuleList">
        <el-table-column type="index" width="50"></el-table-column>
        <el-table-column label="Alert Rule">
          <template
            slot-scope="scope"
          >{{scope.row.ruleKey}} {{scope.row.compareSign}} {{scope.row.ruleValue}}</template>
        </el-table-column>
        <el-table-column label="Rule Status">
          <template slot-scope="scope">
            <el-tag size="small" type="success" v-if="scope.row.valid">valid</el-tag>
            <el-tag size="small" type="danger" v-else>Invalid</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Is Global">
          <template slot-scope="scope">
            <el-tag size="small" v-if="scope.row.global">Global</el-tag>
          </template>
        </el-table-column>

        <el-table-column property="checkCycle" label="Check Cycle(Min)"></el-table-column>
        <el-table-column property="ruleInfo" label="Info"></el-table-column>
        <el-table-column property="time" label="Time"></el-table-column>
        <el-table-column label="Operation" width="250px;">
          <template slot-scope="scope">
            <!-- <el-button size="mini" @click="handleView(scope.$index, scope.row)">View</el-button> -->
            <el-button
              size="mini"
              type="primary"
              @click="editAlertRule(scope.$index, scope.row)"
            >Edit</el-button>
            <el-button
              size="mini"
              type="danger"
              @click="handleDelete(scope.$index, scope.row)"
            >Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- dialog: create rule-->
    <el-dialog
      title="Create Rule"
      :visible.sync="editVisible"
      width="50%"
      :close-on-click-modal="false"
      v-loading="saveAlertRuleLoading"
    >
      <el-form :model="alertRule" ref="alertRule" :rules="rules" label-width="135px">
        <el-form-item label="Rule Key" prop="ruleKey">
          <el-select size="small" v-model="alertRule.ruleKey" placeholder="Select rule key">
            <el-option
              v-for="item in ruleKeyList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Compare Type" prop="compareType">
          <el-select size="small" v-model="alertRule.compareType" placeholder="Select compare type">
            <el-option
              v-for="item in compareTypeList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Rule Value" prop="ruleValue">
          <el-input
            size="small"
            v-model="alertRule.ruleValue"
            placeholder="Please enter rule value"
            style="width: 215px;"
          ></el-input>
        </el-form-item>
        <el-form-item label="Check Cycle" prop="checkCycle">
          <el-select size="small" v-model="alertRule.checkCycle" placeholder="Select check circle">
            <el-option
              v-for="item in checkCycleList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Is Valid" prop="isValid">
          <el-switch v-model="alertRule.valid"></el-switch>
        </el-form-item>
        <el-form-item label="Is Global" prop="isGlobal">
          <el-switch v-model="alertRule.global"></el-switch>
        </el-form-item>
        <el-form-item label="Rule Info" prop="ruleInfo">
          <el-input size="small" v-model="alertRule.ruleInfo" placeholder="Please enter rule info"></el-input>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button size="small" @click="editVisible = false">Cancel</el-button>
        <el-button size="small" type="primary" @click="saveAlertRule('alertRule')">Confirm</el-button>
      </div>
    </el-dialog>
    <el-dialog title="Delete Alert Rule" :visible.sync="deleteVisible" width="30%">
      <span>
        Are you sure to delete
        <b>{{ alertRule.ruleKey }} {{ alertRule.compareSign }} {{ alertRule.ruleValue }}</b> ?
      </span>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="deleteVisible = false">Cancel</el-button>
        <el-button size="small" type="danger" @click="deleteAlertRule()">Delete</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { store } from "@/vuex/store.js";
import { isEmpty } from "@/utils/validate.js";
import { formatTime } from "@/utils/time.js";
import API from "@/api/api.js";
import message from "@/utils/message.js";
export default {
  data() {
    return {
      alertRuleList: [],
      alertRule: {
        valid: true,
        global: false
      },
      editVisible: false,
      isUpdate: false,
      deleteVisible: false,
      ruleKeyList: [
        {
          value: "used_memory",
          label: "used_memory"
        },
        {
          value: "used_memory_rss",
          label: "used_memory_rss"
        },
        {
          value: "used_memory_overhead",
          label: "used_memory_overhead"
        },
        {
          value: "used_memory_dataset",
          label: "used_memory_dataset"
        },
        {
          value: "fragmentation_ratio",
          label: "fragmentation_ratio"
        },
        {
          value: "connections_received",
          label: "connections_received"
        },
        {
          value: "rejected_connections",
          label: "rejected_connections"
        },
        {
          value: "connected_clients",
          label: "connected_clients"
        },
        {
          value: "blocked_clients",
          label: "blocked_clients"
        },
        {
          value: "commands_processed",
          label: "commands_processed"
        },
        {
          value: "instantaneous_ops_per_sec",
          label: "instantaneous_ops_per_sec"
        },
        {
          value: "sync_full",
          label: "sync_full"
        },
        {
          value: "sync_partial_ok",
          label: "sync_partial_ok"
        },
        {
          value: "sync_partial_err",
          label: "sync_partial_err"
        },
        {
          value: "keyspace_hits_ratio",
          label: "keyspace_hits_ratio"
        },
        {
          value: "keys",
          label: "keys"
        },
        {
          value: "expires",
          label: "expires"
        },
        {
          value: "cpu_sys",
          label: "cpu_sys"
        },
        {
          value: "cpu_user",
          label: "cpu_user"
        }
      ],
      /**
       * 0: =
       * 1: >
       * -1: <
       * 2: !=
       */
      compareTypeList: [
        {
          value: 1,
          label: ">"
        },
        {
          value: -1,
          label: "<"
        },
        {
          value: 0,
          label: "="
        },
        {
          value: 2,
          label: "!="
        }
      ],
      checkCycleList: [
        {
          value: 5,
          label: "5 Min"
        },
        {
          value: 10,
          label: "10 Min"
        },
        {
          value: 15,
          label: "15 Min"
        },
        {
          value: 30,
          label: "30 Min"
        },
        {
          value: 60,
          label: "1 Hour"
        },
        {
          value: 180,
          label: "3 Hour"
        },
        {
          value: 360,
          label: "6 Hour"
        },
        {
          value: 720,
          label: "12 Hour"
        },
        {
          value: 1440,
          label: "1 day"
        }
      ],
      rules: {
        ruleKey: [
          {
            required: true,
            message: "Rule key can't be empty",
            trigger: "blur"
          }
        ],
        compareType: [
          {
            required: true,
            message: "Compare type can't be empty",
            trigger: "blur"
          }
        ],
        ruleValue: [
          {
            required: true,
            message: "Rule value can't be empty",
            trigger: "blur"
          }
        ],
        checkCycle: [
          {
            required: true,
            message: "Check cycle can't be empty",
            trigger: "blur"
          }
        ]
      },
      saveAlertRuleLoading: false
    };
  },
  methods: {
    handleView(index, row) {
    },
    editAlertRule(index, row) {
      this.getAlertRule(row.ruleId);
      this.isUpdate = true;
      this.editVisible = true;
    },
    handleDelete(index, row) {
      this.alertRule = row;
      this.deleteVisible = true;
    },
    createAlertRule() {
      this.editVisible = true;
      this.isUpdate = false;
      this.alertRule = {
        valid: true,
        global: false
      };
    },
    getAlertRuleList(groupId) {
      let url = "/alert/rule/getAlertRule/group/" + groupId;
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
            message.error("Get alert rule list failed");
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    getAlertRule(ruleId) {
      let url = "/alert/rule/getAlertRule/" + ruleId;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.alertRule = result.data;
          } else {
            message.error("Get alert rule faild");
          }
        },
        err => {
         message.error(err);
        }
      );
    },
    saveAlertRule(alertRule) {
      this.$refs[alertRule].validate(valid => {
        if (valid) {
          let url;
          if (this.isUpdate) {
            url = "/alert/rule/updateAlertRule";
          } else {
            url = "/alert/rule/addAlertRule";
          }
          this.alertRule.groupId = this.currentGroupId;
          this.saveAlertRuleLoading = true;
          API.post(
            url,
            this.alertRule,
            response => {
              let result = response.data;
              if (result.code == 0) {
                this.getAlertRuleList(this.currentGroupId);
                this.editVisible = false;
                this.$refs[alertRule].resetFields();
              } else {
                message.error("Save alert rule failed");
              }
              this.saveAlertRuleLoading = false;
            },
            err => {
              this.saveAlertRuleLoading = false;
              message.error(err);
            }
          );
        }
      });
    },
    deleteAlertRule() {
      let url = "/alert/rule/deleteAlertRule";
      API.post(
        url,
        this.alertRule,
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.getAlertRuleList(this.currentGroupId);
            this.deleteVisible = false;
          } else {
            message.error("Delete alert rule failed");
          }
        },
        err => {
          message.error(err);
        }
      );
    }
  },
  computed: {
    currentGroupId() {
      return store.getters.getCurrentGroup.groupId;
    }
  },
  watch: {
    currentGroupId(groupId) {
      this.getAlertRuleList(groupId);
    }
  },
  mounted() {
    let groupId = this.$route.params.groupId;
    this.getAlertRuleList(groupId);
  }
};
</script>

<style scoped>
.body-wrapper {
  min-width: 1000px;
}
.header-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 1px solid #dcdfe6;
}
</style>