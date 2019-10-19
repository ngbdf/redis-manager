<template>
  <div id="user-manage" class="body-wrapper">
    <div class="operation-wrapper">
      <el-button size="mini" type="success" @click="editUserVisible = true">Create</el-button>
    </div>
    <el-table :data="userList">
      <el-table-column type="index" width="50"></el-table-column>
      <el-table-column prop="userName" label="User Name"></el-table-column>
      <el-table-column label="User Role">
        <template slot-scope="scope">
          <el-tag size="small" type="success" v-if="scope.row.userRole == 0">Super Admin</el-tag>
          <el-tag size="small" type="primary" v-else-if="scope.row.userRole == 1">Admin</el-tag>
          <el-tag size="small" type="info" v-else>Member</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="User Role">
        <template slot-scope="scope">
          <el-avatar :src="scope.row.avatar"></el-avatar>
        </template>
      </el-table-column>

      <el-table-column prop="email" label="Email"></el-table-column>
      <el-table-column prop="mobile" label="Mobile"></el-table-column>
      <el-table-column prop="time" label="Time"></el-table-column>
      <el-table-column label="Operation" width="200px;">
        <template slot-scope="scope">
          <el-button size="mini" type="primary" @click="handleOperation(scope.row)">Edit</el-button>
          <el-button size="mini" type="danger" @click="handleOperation(scope.row)">Delete</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog title="Create User" :visible.sync="editUserVisible">
      <el-form :model="user" ref="user" :rules="rules" label-width="100px" size="small">
        <el-form-item label="Group Name">
          <el-tag size="small">{{ currentGroup.groupName }}</el-tag>
        </el-form-item>
        <el-form-item label="User Role">
          <el-select v-model="user.userRole" placeholder="Please select user role">
            <el-option label="Member" value="2"></el-option>
            <el-option label="Admin" value="1"></el-option>
            <el-option label="Super Admin" value="0"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="userName" label="User Name">
          <el-input v-model.trim="user.userName"></el-input>
        </el-form-item>
        <el-form-item prop="password" label="Password">
          <el-input v-model.trim="user.password"></el-input>
        </el-form-item>
        <el-form-item prop="email" label="Email">
          <el-input v-model.trim="user.email"></el-input>
        </el-form-item>
        <el-form-item prop="mobile" label="Mobile">
          <el-input v-model.trim="user.mobile"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" type="primary" @click="saveUser('user')">Confirm</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { store } from "@/vuex/store.js";
import API from "@/api/api.js";
import { formatTime } from "@/utils/time.js";
import { isEmpty, validateEmail, validateMobile } from "@/utils/validate.js";
export default {
  data() {
    var validateEmailFormat = (rule, value, callback) => {
      if (!isEmpty(value) && !validateEmail(value)) {
        return callback(new Error("Incorrect format"));
      }
      callback();
    };
    var validateMobileFormat = (rule, value, callback) => {
      if (!isEmpty(value) && !validateMobile(value)) {
        return callback(new Error("Incorrect format"));
      }
      callback();
    };
    return {
      userList: [],
      user: {},
      isUpdate: false,
      editUserVisible: false,

      rules: {
        userRole: [
          {
            required: true,
            message: "Please select user role",
            trigger: "blur"
          }
        ],
        userName: [
          {
            required: true,
            message: "Please enter user name",
            trigger: "blur"
          }
        ],
        password: [
          {
            required: true,
            message: "Please enter password",
            trigger: "blur"
          }
        ],
        email: [
          {
            validator: validateEmailFormat,
            trigger: "blur"
          }
        ],
        mobile: [
          {
            validator: validateMobileFormat,
            trigger: "blur"
          }
        ]
      }
    };
  },
  methods: {
    handleOperation(row) {
      console.log(row);
      this.user = row;
    },
    getUserList(groupId) {
      let url = "/user/getUserList/" + groupId;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            let userList = result.data;
            userList.forEach(user => {
              user.time = formatTime(user.updateTime);
            });
            this.userList = userList;
          }
        },
        err => {
          console.log(err);
        }
      );
    },
    saveUser(user) {
      this.$refs[user].validate(valid => {
        if (valid) {
          this.user.groupId = this.currentGroup.groupId;
          console.log(this.user);
        }
      });
    }
  },
  computed: {
    currentGroup() {
      return store.getters.getCurrentGroup;
    }
  },
  mounted() {
    let groupId = this.$route.params.groupId;
    this.getUserList(groupId);
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