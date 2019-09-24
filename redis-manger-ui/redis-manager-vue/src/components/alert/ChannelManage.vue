<template>
  <div id="channel-manage" class="body-wrapper">
    <div class="header-wrapper">
      <div>Bigdata</div>
      <div>
        <el-button size="mini" type="success" @click="createVisible = true">Create</el-button>
      </div>
    </div>
    <div>
      <el-table :data="channelList" style="width: 100%">
        <el-table-column type="index" width="50"></el-table-column>
        <el-table-column property="channelName" label="Channel Name" width="150px;"></el-table-column>
        <el-table-column label="Channel Type" width="200px;">
          <template slot-scope="scope">
            <el-tag size="small" type="success" v-if="scope.row.channelType == '1'">WebChat Webhook</el-tag>
            <!-- <el-popover trigger="hover" placement="top">
              <p>姓名: {{ scope.row.name }}</p>
              <p>住址: {{ scope.row.address }}</p>
              <div slot="reference" class="name-wrapper">
                <el-tag size="medium">{{ scope.row.name }}</el-tag>
              </div>
            </el-popover>-->
          </template>
        </el-table-column>
        <el-table-column property="channelInfo" label="Info"></el-table-column>
        <el-table-column property="updateTime" label="Time"></el-table-column>
        <el-table-column label="Operation" width="250px;">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleView(scope.$index, scope.row)">View</el-button>
            <el-button size="mini" type="primary" @click="handleEdit(scope.$index, scope.row)">Edit</el-button>
            <el-button
              size="mini"
              type="danger"
              @click="handleDelete(scope.$index, scope.row)"
            >Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- dialog: create channel-->
    <el-dialog
      title="Create Channel"
      :visible.sync="createVisible"
      width="60%"
      :close-on-click-modal="false"
    >
      <el-form :model="alertChannel" ref="alertChannel" :rules="rules" label-width="135px">
        <el-form-item label="Channel Name" prop="channelName">
          <el-input size="small" v-model="alertChannel.channelName" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="Channel Type" prop="channelType">
          <el-radio-group v-model="alertChannel.channelType" size="medium">
            <el-radio label="0">Email</el-radio>
            <el-radio label="1">Wechat Web Hook</el-radio>
            <el-radio label="2">DingDing Web Hook</el-radio>
            <el-radio label="3">Wechat App</el-radio>
          </el-radio-group>
        </el-form-item>
        <!-- email start -->
        <el-form-item label="SMTP Host" prop="smtpHost" v-if="alertChannel.channelType == 0">
          <el-input size="small" v-model="alertChannel.smtpHost" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item
          label="SMTP User Name"
          prop="smtpUserName"
          v-if="alertChannel.channelType == 0"
        >
          <el-input size="small" v-model="alertChannel.smtpUserName" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item
          label="SMTP Password"
          prop="smtpPassword"
          v-if="alertChannel.channelType == 0"
        >
          <el-input size="small" v-model="alertChannel.smtpPassword" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="Email From" prop="emailFrom" v-if="alertChannel.channelType == 0">
          <el-input size="small" v-model="alertChannel.emailFrom" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="Email To" prop="emailTo" v-if="alertChannel.channelType == 0">
          <el-input size="small" v-model="alertChannel.emailTo" autocomplete="off"></el-input>
        </el-form-item>
        <!-- email end -->

        <!-- wechat web hook start -->
        <el-form-item
          label="Web Hook"
          prop="webhook"
          v-if="alertChannel.channelType == 1 || alertChannel.channelType == 2"
        >
          <el-input size="small" v-model="alertChannel.webhook" autocomplete="off"></el-input>
        </el-form-item>
        <!-- wechat web hook end -->

        <!-- wechat app start -->
        <el-form-item label="Corp Id" prop="corpId" v-if="alertChannel.channelType == 3">
          <el-input size="small" v-model="alertChannel.corpId" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="Agent Id" prop="agentId" v-if="alertChannel.channelType == 3">
          <el-input size="small" v-model="alertChannel.agentId" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="Corp Secret" prop="corpSecret" v-if="alertChannel.channelType == 3">
          <el-input size="small" v-model="alertChannel.corpSecret" autocomplete="off"></el-input>
        </el-form-item>
        <!-- wechat app end -->
      </el-form>
      <div slot="footer" class="dialog-footer">
        <!-- <el-button size="small" @click="createVisible = false">Cancel</el-button> -->
        <el-button size="small" type="primary" @click="saveChannel('alertChannel')">Confirm</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { store } from "@/vuex/store.js";
import { isEmpty, validateURL, validateEmail } from "@/utils/validate.js";
import { formatTime } from "@/utils/time.js";
import API from "@/api/api.js";
export default {
  data() {
    var validateChannelName = (rule, value, callback) => {
      if (isEmpty(value) || isEmpty(value.trim())) {
        return callback(new Error("Please enter channel name"));
      } else {
        let url = "/channel/validateChannelName/" + value;
        API.get(
          url,
          null,
          response => {
            if (response.data.code != 0) {
              return callback(new Error(value + " has exist"));
            } else {
              callback();
            }
          },
          err => {
            return callback(new Error("Network error, " + err));
          }
        );
      }
    };
    var validateSMTPHost = (rule, value, callback) => {
      if (isEmpty(value) || isEmpty(value.trim())) {
        return callback(new Error("Please enter SMTP Host"));
      } else if (!validateURL(value)) {
        return callback(new Error("Incorrect host format"));
      }
      callback();
    };
    var validateEmailFrom = (rule, value, callback) => {
      if (isEmpty(value) || isEmpty(value.trim())) {
        return callback(new Error("Please enter email from"));
      } else if (!validateEmail(value)) {
        return callback(new Error("Incorrect email format"));
      }
      callback();
    };
    var validateEmailTo = (rule, value, callback) => {
      if (isEmpty(value) || isEmpty(value.trim())) {
        return callback(new Error("Please enter email to"));
      }
      callback();
    };
    var validateWebHook = (rule, value, callback) => {
      if (isEmpty(value) || isEmpty(value.trim())) {
        return callback(new Error("Please enter web hook"));
      } else if (!validateURL(value)) {
        return callback(new Error("Incorrect url format"));
      }
      callback();
    };
    var validateCorpId = (rule, value, callback) => {
      if (isEmpty(value) || isEmpty(value.trim())) {
        return callback(new Error("Please enter corp id"));
      }
      callback();
    };
    var validateAgentId = (rule, value, callback) => {
      if (isEmpty(value) || isEmpty(value.trim())) {
        return callback(new Error("Please enter agent id"));
      }
      callback();
    };
    var validateCorpSecret = (rule, value, callback) => {
      if (isEmpty(value) || isEmpty(value.trim())) {
        return callback(new Error("Please enter corp secret"));
      }
      callback();
    };
    return {
      channelList: [
        {
          channelName: "bigdata",
          channelType: "1",
          channelInfo: "bigdata alert",
          updateTime: "2019-08-25"
        }
      ],
      createVisible: false,
      alertChannel: {
        channelType: "0"
      },
      rules: {
        channelName: [
          { required: true, validator: validateChannelName, trigger: "blur" }
        ],
        smtpHost: [
          { required: true, validator: validateSMTPHost, trigger: "blur" }
        ],
        emailFrom: [
          { required: true, validator: validateEmailFrom, trigger: "blur" }
        ],
        emailTo: [
          { required: true, validator: validateEmailTo, trigger: "blur" }
        ],
        webhook: [
          { required: true, validator: validateWebHook, trigger: "blur" }
        ],
        corpId: [
          { required: true, validator: validateCorpId, trigger: "blur" }
        ],
        agentId: [
          { required: true, validator: validateAgentId, trigger: "blur" }
        ],
        corpSecret: [
          { required: true, validator: validateCorpSecret, trigger: "blur" }
        ]
      }
    };
  },
  methods: {
    handleView(index, row) {
      console.log(index, row);
    },
    handleEdit(index, row) {
      console.log(index, row);
    },
    handleDelete(index, row) {
      console.log(index, row);
    },
    getChannelList() {
      let currentGroupId = store.getters.getCurrentGroup;
      let url = "/channel/getChannelList/" + currentGroupId;
      API.get(
        url,
        null,
        response => {
          if (response.data.code == 0) {
            this.channelList = response.data.data;
          } else {
            console.log("No data");
          }
        },
        err => {
          console.log(err);
        }
      );
    },
    saveChannel(alertChannel) {
      this.$refs[alertChannel].validate(valid => {
        if (valid) {
        }
      });
    }
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