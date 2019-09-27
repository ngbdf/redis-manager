<template>
  <div class="body-wrapper">
    <div class="operation-wrapper">
      <el-button size="mini" type="success" @click="importMachineVisible = true">Import Machine</el-button>
    </div>
    <div>
      <el-table :data="machineList" style="width: 100%" center>
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column property="machineGroupName" label="Machine Group Name" width="200"></el-table-column>
        <el-table-column property="host" label="Host"></el-table-column>
        <el-table-column property="userName" label="User Name"></el-table-column>
        <el-table-column label="Password" align="center">
          <template slot-scope="scope">
            <el-popover trigger="hover" placement="top">
              <p>Password: {{ scope.row.password }}</p>
              <div slot="reference" class="name-wrapper">
                <el-tag size="small" type="info">******</el-tag>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column label="Token" align="center">
          <template slot-scope="scope">
            <el-popover trigger="hover" placement="top">
              <p>Token: {{ scope.row.password }}</p>
              <div slot="reference" class="name-wrapper">
                <el-tag size="small" type="info">******</el-tag>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column property="machineInfo" label="Info"></el-table-column>
        <el-table-column property="updateTime" label="Time"></el-table-column>
        <el-table-column label="Operation" width="200px;">
          <template slot-scope="scope">
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
    <el-dialog title="Create User" :visible.sync="importMachineVisible" width="50%">
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
              v-for="item in machineGroupNameList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="User Name" prop="userName">
          <el-input v-model="machines.userName"></el-input>
        </el-form-item>
        <el-form-item label="Password" prop="password">
          <el-input v-model="machines.password"></el-input>
        </el-form-item>
        <el-form-item label="Token" prop="token">
          <el-input v-model="machines.token"></el-input>
        </el-form-item>

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

        <el-form-item label="Machine Info" prop="machineInfo">
          <el-input v-model="machines.machineInfo"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" @click="addHost()">New Host</el-button>
        <el-button size="small" type="primary" @click="addMachines('machines')">Confirm</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import API from "@/api/api.js";
import { formatTime } from "@/utils/time.js";
import { isEmpty, validateIp } from "@/utils/validate.js";
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
      machineList: [
        {
          machineGroupName: "huadong",
          host: "192.168.53.26",
          userName: "root",
          password: "1234",
          token: "xGea2fg4kj4hjeino;u303asa",
          machineInfo: "info",
          updateTime: ""
        }
      ],
      machineGroupNameList: [
        {
          value: "E1",
          label: "E1"
        },
        {
          value: "E2",
          label: "E2"
        },
        {
          value: "E3",
          label: "E3"
        }
      ],
      importMachineVisible: false,
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
      }
    };
  },
  methods: {
    getMachineList(groupId) {
      if (isEmpty(groupId)) {
        console.log("Please select group.");
        return;
      }
      let url = "/machine/getMachineList/" + groupId;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.machineList = result.data;
          } else {
            console.log(result.message);
          }
        },
        err => {
          console.log(err);
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
            console.log(response.message);
          }
        },
        err => {
          console.log(err);
        }
      );
    },
    addMachines(machines) {
      this.$refs[machines].validate(valid => {
        if (valid) {
          let machineList = [];
          let machineGroupName = this.machines.machineGroupName;
          let userName = this.machines.userName;
          let password = this.machines.password;
          let token = this.machines.token;
          let hostList = this.machines.hostList;
          let machineInfo = this.machines.machineInfo;
          hostList.forEach(host => {
            machineList.push({
              machineGroupName: machineGroupName,
              userName: userName,
              password: password,
              token: token,
              host: host,
              machineInfo: machineInfo
            });
          });
          let url = "/machine/addMachineList";
          API.post(url, null, response => {}, err => {});
        }
      });
    },
    handleEdit(index, row) {
      console.log(index, row);
    },
    handleDelete(index, row) {
      console.log(index, row);
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
    }
  },
  mounted() {
    let groupId = this.$route.params.groupId;
    this.getMachineList(groupId);
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
</style>