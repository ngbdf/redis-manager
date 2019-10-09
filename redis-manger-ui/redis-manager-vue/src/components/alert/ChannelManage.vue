<template>
  <div id="channel-manage" class="body-wrapper">
    <div class="header-wrapper">
      <div>Bigdata</div>
      <div>
        <el-button size="mini" type="success" @click="editVisible = true, isUpdate = false">Create</el-button>
      </div>
    </div>
    <div>
      <el-table :data="alertChannelList" style="width: 100%">
        <el-table-column type="index" width="50"></el-table-column>
        <el-table-column prop="channelName" label="Channel Name" width="150px;"></el-table-column>
        <el-table-column label="Channel Type" width="200px;">
          <template slot-scope="scope">
            <el-tag size="small" type="success" v-if="scope.row.channelType == 0">Email</el-tag>
            <el-tag size="small" type="success" v-if="scope.row.channelType == 1">WebChat Webhook</el-tag>
            <!-- <el-popover trigger="hover" placement="top">
              <p>姓名: {{ scope.row.name }}</p>
              <p>住址: {{ scope.row.address }}</p>
              <div slot="reference" class="name-wrapper">
                <el-tag size="medium">{{ scope.row.name }}</el-tag>
              </div>
            </el-popover>-->
          </template>
        </el-table-column>
        <el-table-column prop="channelInfo" label="Info"></el-table-column>
        <el-table-column prop="updateTime" label="Time"></el-table-column>
        <el-table-column label="Operation" width="200px;">
          <template slot-scope="scope">
            <!-- <el-button size="mini" @click="handleView(scope.$index, scope.row)">View</el-button> -->
            <el-button size="mini" type="primary" @click="editChannel(scope.$index, scope.row)">Edit</el-button>
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
      :visible.sync="editVisible"
      width="60%"
      :close-on-click-modal="false"
    >
      <el-form :model="alertChannel" ref="alertChannel" :rules="rules" label-width="135px">
        <el-form-item label="Channel Name" prop="channelName">
          <el-input size="small" v-model="alertChannel.channelName" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="Channel Type" prop="channelType">
          <el-radio-group v-model="alertChannel.channelType" size="medium">
            <el-radio :label="0">Email</el-radio>
            <el-radio :label="1">Wechat Web Hook</el-radio>
            <el-radio :label="2">DingDing Web Hook</el-radio>
            <el-radio :label="3">Wechat App</el-radio>
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
        <el-form-item label="Channel Info" prop="channelInfo">
          <el-input size="small" v-model="alertChannel.channelInfo"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <!-- <el-button size="small" @click="createVisible = false">Cancel</el-button> -->
        <el-button
          size="small"
          type="primary"
          @click="saveChannel('alertChannel')"
          v-if="isUpdate"
        >Update</el-button>
        <el-button size="small" type="primary" @click="saveChannel('alertChannel')" v-else>Confirm</el-button>
      </div>
    </el-dialog>
    <el-dialog title="Delete Alert Channel" :visible.sync="deleteVisible" width="30%">
      <span>
        Are you sure to delete
        <b>{{ alertChannel.channelName }}</b> ?
      </span>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="deleteVisible = false">Cancel</el-button>
        <el-button size="small" type="danger" @click="deleteChannel()">Delete</el-button>
      </span>
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
    var validateSMTPHost = (rule, value, callback) => {
      if (!validateURL(value)) {
        return callback(new Error("Incorrect host format"));
      }
      callback();
    };
    var validateEmailFrom = (rule, value, callback) => {
      if (!validateEmail(value)) {
        return callback(new Error("Incorrect email format"));
      }
      callback();
    };
    var validateWebHook = (rule, value, callback) => {
      if (!validateURL(value)) {
        return callback(new Error("Incorrect url format"));
      }
      callback();
    };
    return {
      alertChannelList: [],
      alertChannel: { channelType: 0 },
      editVisible: false,
      isUpdate: false,
      deleteVisible: false,
      rules: {
        channelName: [
          {
            required: true,
            message: "Channel name can't be empty",
            trigger: "blur"
          }
        ],
        smtpHost: [
          {
            required: true,
            message: "SMTP host can't be empty",
            trigger: "blur"
          },
          { required: true, validator: validateSMTPHost, trigger: "blur" }
        ],
        emailFrom: [
          {
            required: true,
            message: "Email from can't be empty",
            trigger: "blur"
          },
          { required: true, validator: validateEmailFrom, trigger: "blur" }
        ],
        emailTo: [
          {
            required: true,
            message: "Email to can't be empty",
            trigger: "blur"
          }
        ],
        webhook: [
          {
            required: true,
            message: "Web hook can't be empty",
            trigger: "blur"
          },
          { required: true, validator: validateWebHook, trigger: "blur" }
        ],
        corpId: [
          { required: true, message: "Corp id can't be empty", trigger: "blur" }
        ],
        agentId: [
          {
            required: true,
            message: "Agent id can't be empty",
            trigger: "blur"
          }
        ],
        corpSecret: [
          {
            required: true,
            message: "Corp Secret can't be empty",
            trigger: "blur"
          }
        ]
      }
    };
  },
  methods: {
    handleView(index, row) {
      console.log(index, row);
    },
    editChannel(index, row) {
      this.getAlertChannel(row.channelId);
      this.isUpdate = true;
      this.editVisible = true;
      
    },
    handleDelete(index, row) {
      console.log(index, row);
      this.alertChannel.channelId = row.channelId;
      this.deleteVisible = true;
    },
    deleteChannel() {
      let url = "/alert/channel/deleteAlertChannel";
      API.post(
        url,
        this.alertChannel,
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.getChannelList(this.currentGroupId);
            this.deleteVisible = false;
            this.alertChannel = { channelType: 0 };
          } else {
            console.log("Delete alert channel failed.");
          }
        },
        err => {
          console.log(err);
        }
      );
    },
    getChannelList(groupId) {
      let url = "/alert/channel/getAlertChannelListByGroupId/" + groupId;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            let alertChannelList = result.data;
            alertChannelList.forEach(alertChannel => {
              alertChannel.updateTime = formatTime(alertChannel.updateTime);
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
    },
    getAlertChannel(channelId) {
      let url = "/alert/channel/getAlertChannel/" + channelId;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.alertChannel = result.data;
          } else {
            console.log("Get alert channel faild");
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
          let url;
          if (this.isUpdate) {
            url = "/alert/channel/updateAlertChannel";
          } else {
            url = "/alert/channel/addAlertChannel";
          }
          this.alertChannel.groupId = this.currentGroupId;
          API.post(
            url,
            this.alertChannel,
            response => {
              let result = response.data;
              if (result.code == 0) {
                this.getChannelList(this.currentGroupId);
                this.editVisible = false;
                this.$refs[alertChannel].resetFields();
              } else {
                console.log("Save alert channel failed.");
              }
            },
            err => {
              console.log(err);
            }
          );
        }
      });
    }
  },
  computed: {
    currentGroupId() {
      return store.getters.getCurrentGroupId;
    }
  },
  watch: {
    "alertChannel.channelType": function(newValue, oldValue) {
      let fields = this.$refs["alertChannel"].fields;

      fields.map(field => {
        if (field.prop === "channelName" || field.prop === "channelType") {
          return true;
        }
        field.resetField();
        return false;
      });
    }
  },
  mounted() {
    let groupId = this.$route.params.groupId;
    this.getChannelList(groupId);
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
