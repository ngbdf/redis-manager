<template>
  <div class="body-wrapper">
    <div class="operation-wrapper">
      <el-button
        size="mini"
        type="success"
        @click="editMachineVisible = true; isUpdate = false, machines={ hostList: [{ value: '' }]}"
      >Import Machine</el-button>
    </div>

    <div class="batch-operation-wrapper">
      <div class="batch-title">Batch Operation</div>
      <el-row>
        <el-button size="mini" type="danger" @click="handleDeleteBatch()">Delete</el-button>
      </el-row>
    </div>
    <div>
      <!-- :data="machineList.filter(data => !search || data.host.toLowerCase().includes(search.toLowerCase()) || data.machineGroupName.toLowerCase().includes(search.toLowerCase()))" -->
      <el-table
        :data="machineList"
        style="width: 100%"
        center
        @selection-change="handleSelectionChange"
        :default-sort="{prop: 'machineGroupName', order: 'ascending'}"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column
          property="machineGroupName"
          label="Machine Group Name"
          width="200"
          sortable
        ></el-table-column>
        <el-table-column property="host" label="Host" sortable></el-table-column>
        <el-table-column property="userName" label="User Name"></el-table-column>
        <el-table-column label="Password" align="center">
          <template slot-scope="scope">
            <el-popover trigger="hover" placement="top">
              <p>{{ scope.row.password }}</p>
              <div slot="reference" class="name-wrapper">
                <el-tag size="small" type="info">******</el-tag>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <!-- <el-table-column label="Token" align="center">
          <template slot-scope="scope">
            <el-popover trigger="hover" placement="top">
              <p>Token: {{ scope.row.password }}</p>
              <div slot="reference" class="name-wrapper">
                <el-tag size="small" type="info">******</el-tag>
              </div>
            </el-popover>
          </template>
        </el-table-column>-->
        <el-table-column property="machineInfo" label="Info"></el-table-column>
        <el-table-column property="time" label="Time" sortable></el-table-column>
        <el-table-column label="Operation" width="200px;">
          <!-- <template slot="header" slot-scope="scope">
            <el-input v-model="search" size="mini" placeholder="Search" />
          </template>-->
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="editMachine(scope.$index, scope.row)">Edit</el-button>
            <el-button
              size="mini"
              type="danger"
              @click="handleDelete(scope.$index, scope.row)"
            >Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-dialog
      title="Edit machines"
      :visible.sync="editMachineVisible"
      width="50%"
      :close-on-click-modal="false"
      v-loading="saveMachineLoading"
    >
      <el-form :model="machines" ref="machines" :rules="rules" size="small" label-width="135px">
        <el-form-item label="Machine Group" prop="machineGroupName">
          <el-select
            v-model="machines.machineGroupName"
            filterable
            allow-create
            default-first-option
            placeholder="Select or enter group"
          >
            <el-option
              v-for="(machineGroupName, index) in machineGroupNameList"
              :key="index"
              :label="machineGroupName"
              :value="machineGroupName"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="User Name" prop="userName">
          <el-input v-model="machines.userName"></el-input>
        </el-form-item>
        <el-form-item label="Password" prop="password">
          <el-input v-model="machines.password"></el-input>
        </el-form-item>
        <!-- <el-form-item label="Token" prop="token">
          <el-input v-model="machines.token"></el-input>
        </el-form-item>-->

        <div v-if="!isUpdate">
          <el-form-item
            v-for="(host, index) in machines.hostList"
            :label="'Host ' + (index + 1)"
            :key="host.key"
            :prop="'hostList.' + index + '.value'"
            :rules="rules.host"
          >
            <el-input v-model="host.value">
              <el-button slot="append" @click.prevent="removeHost(host)" icon="el-icon-delete"></el-button>
            </el-input>
          </el-form-item>
        </div>
        <el-form-item label="Host" prop="host" :rules="rules.host" v-else>
          <el-input v-model="machines.host"></el-input>
        </el-form-item>

        <el-form-item label="Machine Info" prop="machineInfo">
          <el-input v-model="machines.machineInfo" maxlength="50" show-word-limit></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" @click="addHost()" v-if="!isUpdate">New Host</el-button>
        <el-button
          size="small"
          type="primary"
          @click="saveMachines('machines')"
          v-if="isUpdate"
        >Update</el-button>
        <el-button size="small" type="primary" @click="saveMachines('machines')" v-else>Confirm</el-button>
      </div>
    </el-dialog>

    <el-dialog title="Delete Machine" :visible.sync="deleteVisible" width="30%">
      <span>
        Are you sure to delete
        <b>{{ machines.machineGroupName }} {{ machines.host }}</b> ?
      </span>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="deleteVisible = false">Cancel</el-button>
        <el-button size="small" type="danger" @click="deleteMachine(machines)">Delete</el-button>
      </span>
    </el-dialog>
    <el-dialog title="Delete Machine Batch" :visible.sync="deleteBatchVisible" width="30%">
      <span>
        Are you sure to delete ?
        <br />
        <div
          v-for="(machine, index) in selectedMachineList"
          :key="index"
        >{{ machine.machineGroupName }} {{ machine.host }}</div>
      </span>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="deleteBatchVisible = false">Cancel</el-button>
        <el-button size="small" type="danger" @click="deleteMachine(selectedMachineList)">Delete</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import { store } from "@/vuex/store.js";
import API from "@/api/api.js";
import { formatTime } from "@/utils/time.js";
import { isEmpty, validateIp } from "@/utils/validate.js";
import message from "@/utils/message.js";
export default {
  data() {
    var validateMahineGroupName = (rule, value, callback) => {
      if (isEmpty(value) || isEmpty(value.trim())) {
        return callback(new Error("Please select or enter machine group name"));
      }
      callback();
    };
    var validateUserName = (rule, value, callback) => {
      if (isEmpty(value) || isEmpty(value.trim())) {
        return callback(new Error("Please enter machine user name"));
      }
      callback();
    };
    var validatePassword = (rule, value, callback) => {
      if (isEmpty(value) || isEmpty(value.trim())) {
        return callback(new Error("Please enter machine password"));
      }
      callback();
    };
    var validateToken = (rule, value, callback) => {
      if (isEmpty(value) || isEmpty(value.trim())) {
        return callback(new Error("Please  enter machine token"));
      }
      callback();
    };
    var validateHost = (rule, value, callback) => {
      if (isEmpty(value) || isEmpty(value.trim())) {
        return callback(new Error("Please enter machine ip"));
      } else if (!validateIp(value)) {
        return callback(new Error("Incorrect ip format"));
      }
      callback();
    };
    var validateHostConnection = (rule, value, callback) => {
      let userName = this.machines.userName;
      let password = this.machines.password;
      if (
        isEmpty(userName) ||
        isEmpty(userName.trim()) ||
        isEmpty(password) ||
        isEmpty(password.trim())
      ) {
        return callback(new Error("Please enter user name and passwor first"));
      }
      let url = "/machine/validateConnection";
      let machine = {
        userName: userName,
        password: password,
        host: value
      };
      API.post(
        url,
        machine,
        response => {
          let result = response.data;
          if (result.code == 0) {
            callback();
          } else {
            return callback(new Error(result.message));
          }
        },
        err => {
          return callback(new Error("Network error"));
        }
      );
    };
    return {
      machineList: [],
      machineGroupNameList: [],
      editMachineVisible: false,
      isUpdate: false,
      deleteVisible: false,
      deleteBatchVisible: false,
      batchDelete: false,
      machines: {
        hostList: [{ value: "" }]
      },
      rules: {
        machineGroupName: [
          {
            required: true,
            validator: validateMahineGroupName,
            trigger: "blur"
          }
        ],
        userName: [
          {
            required: true,
            validator: validateUserName,
            trigger: "blur"
          }
        ],
        password: [
          {
            required: true,
            validator: validatePassword,
            trigger: "blur"
          }
        ],
        host: [
          {
            required: true,
            validator: validateHost,
            trigger: "blur"
          },
          {
            required: true,
            validator: validateHostConnection,
            trigger: "blur"
          }
        ]
      },
      search: "",
      selectedMachineList: [],
      saveMachineLoading: false
    };
  },
  methods: {
    getMachineList(groupId) {
      if (isEmpty(groupId)) {
        return;
      }
      let url = "/machine/getMachineList/" + groupId;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            let machineList = result.data;
            machineList.forEach(machine => {
              machine.time = formatTime(machine.updateTime);
            });
            this.machineList = machineList;
          } else {
           message.error("Get machine list failed");
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    getMachineGroupNameList(groupId) {
      let url = "/machine/getMachineGroupNameList/" + groupId;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.machineGroupNameList = result.data;
          } else {
            message.error("Get machine group name list failed");
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    buildMachineList(machines) {
      let machineList = [];
      if (this.isUpdate) {
        machineList.push(machines);
        return machineList;
      }
      let machineGroupName = this.machines.machineGroupName;
      let userName = this.machines.userName;
      let password = this.machines.password;
      let token = this.machines.token;
      let hostList = this.machines.hostList;
      let machineInfo = this.machines.machineInfo;
      hostList.forEach(host => {
        machineList.push({
          groupId: this.currentGroupId,
          machineGroupName: machineGroupName,
          userName: userName,
          password: password,
          token: token,
          host: host.value,
          machineInfo: machineInfo
        });
      });
      return machineList;
    },
    saveMachines(machines) {
      this.$refs[machines].validate(valid => {
        if (valid) {
          let machineList = this.buildMachineList(this.machines);
          let url = "";
          if (this.isUpdate) {
            url = "/machine/updateMachine";
          } else {
            url = "/machine/addMachineList";
          }
          this.saveMachineLoading = true;
          API.post(
            url,
            machineList,
            response => {
              let result = response.data;
              if (result.code == 0) {
                this.editMachineVisible = false;
                this.getMachineGroupNameList(this.currentGroupId);
                this.getMachineList(this.currentGroupId);
              } else {
                message.error("Save machines failed");
              }
              this.$refs[machines].resetFields();
              this.saveMachineLoading = false;
            },
            err => {
              this.saveMachineLoading = true;
              message.error(err);
            }
          );
        }
      });
    },
    editMachine(index, row) {
      this.machines = row;
      this.editMachineVisible = true;
      this.isUpdate = true;
    },
    handleDelete(index, row) {
      this.machines = row;
      this.deleteVisible = true;
      this.batchDelete = false;
    },
    handleDeleteBatch() {
      this.batchDelete = true;
      if (this.selectedMachineList.length == 0) {
        message.warning("Please select machine");
      } else {
        this.deleteBatchVisible = true;
      }
    },
    deleteMachine() {
      let url = "";
      let data;
      if (this.batchDelete) {
        url = "/machine/deleteMachineBatch";
        data = this.selectedMachineList;
        data.forEach(machine => {
          machine.updateTime = new Date();
        });
      } else {
        url = "/machine/deleteMachine";
        data = this.machines;
        data.updateTime = new Date();
      }
      API.post(
        url,
        data,
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.deleteVisible = false;
            this.deleteBatchVisible = false;
            this.getMachineGroupNameList(this.currentGroupId);
            this.getMachineList(this.currentGroupId);
            this.machines = {
              hostList: [{ value: "" }]
            };
            this.selectedMachineList = [];
          } else {
            message.error("Delete machine failed");
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    removeHost(item) {
      if (this.machines.hostList.length == 1) {
        return;
      }
      var index = this.machines.hostList.indexOf(item);
      if (index !== -1) {
        this.machines.hostList.splice(index, 1);
      }
    },
    addHost() {
      if (this.machines.hostList.length >= 20) {
        return;
      }
      this.machines.hostList.push({
        value: "",
        key: Date.now()
      });
    },
    handleSelectionChange(val) {
      this.selectedMachineList = val;
    }
  },
  computed: {
    currentGroupId() {
      return store.getters.getCurrentGroup.groupId;
    }
  },
  watch: {
    currentGroupId(groupId) {
      this.getMachineList(groupId);
      this.$router.push({
        name: "machine-manage",
        params: { groupId: groupId }
      });
    }
  },
  mounted() {
    let groupId = this.$route.params.groupId;
    this.getMachineList(groupId);
    this.getMachineGroupNameList(groupId);
  }
};
</script>
<style scoped>
.body-wrapper {
  min-width: 1000px;
}
.operation-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-bottom: 20px;
  border-bottom: 1px solid #dcdfe6;
}

.batch-operation-wrapper {
  margin: 20px 0;
}

.batch-title {
  margin-bottom: 10px;
  color: #909399;
  font-size: 14px;
}
</style>