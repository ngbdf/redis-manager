<template>
  <div id="user-manage" class="container-wrapper">
    <div>
      <div class="operation-wrapper">
        <div>Current Group</div>
        <el-button
          size="mini"
          type="success"
          @click="user = {}, editUserVisible = true, isUpdate = false"
        >Create</el-button>
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
            <el-button
              size="mini"
              type="primary"
              @click="handleEdit(scope.row)"
              v-if="currentUser.userRole <= scope.row.userRole"
            >Edit</el-button>
            <el-button
              size="mini"
              type="danger"
              @click="handleDelete(scope.row)"
              v-if="currentUser.userRole <= scope.row.userRole && currentUser.userId != scope.row.userId"
            >Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="grant-wrapper">
      <div class="operation-wrapper">
        <div>Other Groups</div>
        <el-button size="mini" type="success" @click="user = {}, editGrantUserVisible = true">Grant</el-button>
      </div>
      <el-table :data="grantedUserList">
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
            <el-button size="mini" type="warning" @click="handleRevoke(scope.row)">Revoke</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog title="Create User" :visible.sync="editUserVisible" v-loading="saveUserLoading">
      <el-form :model="user" ref="user" :rules="rules" label-width="100px" size="small">
        <el-form-item label="Group Name">
          <el-tag size="small">{{ currentGroup.groupName }}</el-tag>
        </el-form-item>
        <el-form-item prop="userRole" label="User Role">
          <el-select v-model="user.userRole" placeholder="Please select user role">
            <el-option label="Member" :value="2"></el-option>
            <el-option label="Admin" :value="1" v-if="currentUser.userRole < 2"></el-option>
            <el-option label="Super Admin" :value="0" v-if="currentUser.userRole < 1"></el-option>
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

    <el-dialog title="Grant User" :visible.sync="editGrantUserVisible" v-loading="grantUserLoading">
      <el-form :model="user" ref="user" :rules="grantUserRules" label-width="100px" size="small">
        <el-form-item label="Group Name">
          <el-tag size="small">{{ currentGroup.groupName }}</el-tag>
        </el-form-item>
        <el-form-item prop="userRole" label="User Role">
          <el-select v-model="user.userRole" placeholder="Please select user role">
            <el-option label="Member" :value="2"></el-option>
            <el-option label="Admin" :value="1" v-if="currentUser.userRole < 2"></el-option>
            <el-option label="Super Admin" :value="0" v-if="currentUser.userRole < 1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="userName" label="User Name">
          <el-input v-model.trim="user.userName"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" type="primary" @click="saveGroupUser('user')">Confirm</el-button>
      </div>
    </el-dialog>

    <el-dialog title="Delete User" :visible.sync="deleteUserVisible" width="30%">
      <span>
        Are you sure to delete
        <b>{{ user.userName }}</b> ?
      </span>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="deleteUserVisible = false">Cancel</el-button>
        <el-button size="small" type="danger" @click="deleteUser()">Delete</el-button>
      </span>
    </el-dialog>

    <el-dialog title="Revoke User" :visible.sync="revokeUserVisible" width="30%">
      <span>
        Are you sure to revoke
        <b>{{ user.userName }}</b> ?
      </span>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="revokeUserVisible = false">Cancel</el-button>
        <el-button size="small" type="danger" @click="revokeUser()">Revoke</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { store } from "@/vuex/store.js";
import API from "@/api/api.js";
import { formatTime } from "@/utils/time.js";
import { isEmpty, validateEmail, validateMobile } from "@/utils/validate.js";
import message from "@/utils/message.js";
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
    var validateUserName = (rule, value, callback) => {
      let url = "/user/validateUserName/" + value;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            callback();
          } else if (this.user.userId !== result.data.userId) {
            callback(new Error(value + " has been used"));
          } else {
            callback();
          }
        },
        err => {
          return callback(new Error("Network error"));
        }
      );
    };
    var validateGrantUserName = (rule, value, callback) => {
      let url = "/user/validateUserName/" + value;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            callback(new Error(value + " not exist"));
          } else {
            let user = result.data;
            API.get(
              "/user/isGranted/" +
                this.currentGroup.groupId +
                "/" +
                user.userId,
              null,
              response => {
                let result = response.data;
                if (result.code == 0) {
                  callback();
                } else {
                  return callback(new Error(value + " has been granted"));
                }
              },
              err => {
                return callback(new Error("Network error"));
              }
            );
          }
        },
        err => {
          return callback(new Error("Network error"));
        }
      );
    };

    return {
      userList: [],
      grantedUserList: [],
      user: {},
      isUpdate: false,
      editUserVisible: false,
      editGrantUserVisible: false,
      deleteUserVisible: false,
      revokeUserVisible: false,
      isUpdate: false,
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
          },
          {
            required: true,
            validator: validateUserName,
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
      },
      grantUserRules: {
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
          },
          {
            required: true,
            validator: validateGrantUserName,
            trigger: "blur"
          }
        ]
      },
      saveUserLoading: false,
      grantUserLoading: false
    };
  },
  methods: {
    handleEdit(row) {
      this.getUserById(row.userId);
      this.isUpdate = true;
      this.editUserVisible = true;
    },
    handleDelete(row) {
      this.user = row;
      this.deleteUserVisible = true;
    },
    handleRevoke(row) {
      this.user = row;
      this.revokeUserVisible = true;
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
          message.error(err);
        }
      );
    },
    getGrantedUserList(groupId) {
      let url = "/user/getGrantedUserList/" + groupId;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            let grantedUserList = result.data;
            grantedUserList.forEach(user => {
              user.time = formatTime(user.updateTime);
            });
            this.grantedUserList = grantedUserList;
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    getUserById(userId) {
      let url = "/user/getUser/" + userId;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.user = result.data;
          } else {
            message.error("Get user failed");
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    saveUser(user) {
      this.$refs[user].validate(valid => {
        if (valid) {
          this.saveUserLoading = true;
          let groupId = this.currentGroup.groupId;
          this.user.groupId = groupId;
          let url;
          if (this.isUpdate) {
            url = "/user/updateUser";
          } else {
            url = "/user/addUser";
          }
          API.post(
            url,
            this.user,
            response => {
              let result = response.data;
              this.getUserList(groupId);
              if (result.code == 0) {
                this.editUserVisible = false;
                this.$refs[user].resetFields();
              } else {
                message.error("save user failed");
              }
              this.saveUserLoading = false;
            },
            err => {
              this.saveUserLoading = false;
              cmessage.error(err);
            }
          );
        }
      });
    },
    saveGroupUser(user) {
      this.$refs[user].validate(valid => {
        if (valid) {
          this.grantUserLoading = true;
          let groupId = this.currentGroup.groupId;
          this.user.groupId = groupId;
          let url = "/user/grantUser";
          API.post(
            url,
            this.user,
            response => {
              let result = response.data;
              this.getGrantedUserList(groupId);
              if (result.code == 0) {
                this.editGrantUserVisible = false;
                this.$refs[user].resetFields();
              } else {
                message.error("Save user failed");
              }
              this.grantUserLoading = false;
            },
            err => {
              this.grantUserLoading = false;
              message.error(err);
            }
          );
        }
      });
    },
    deleteUser() {
      let url = "/user/deleteUser";
      API.post(
        url,
        this.user,
        response => {
          this.getUserList(this.currentGroup.groupId);
          this.deleteUserVisible = false;
          this.user = {};
        },
        err => {
          message.error(err);
        }
      );
    },
    revokeUser() {
      let url = "/user/revokeUser";
      API.post(
        url,
        this.user,
        response => {
          this.getGrantedUserList(this.currentGroup.groupId);
          this.revokeUserVisible = false;
          this.user = {};
        },
        err => {
          message.error(err);
        }
      );
    }
  },
  computed: {
    currentGroup() {
      return store.getters.getCurrentGroup;
    },
    currentUser() {
      return store.getters.getUser;
    }
  },
  watch: {
    currentGroup(group) {
      let groupId = group.groupId;
      this.getUserList(groupId);
      this.getGrantedUserList(groupId);
      this.$router.push({
        name: "user-manage",
        params: { groupId: groupId }
      });
    }
  },
  mounted() {
    let groupId = this.$route.params.groupId;
    this.getUserList(groupId);
    this.getGrantedUserList(groupId);
  }
};
</script>

<style scoped>
.container-wrapper {
  min-width: 1000px;
}

.operation-wrapper {
  display: flex;
  justify-content: space-between;
  padding-bottom: 20px;
  border-bottom: 1px solid #dcdfe6;
  background-color: #ffffff;
  padding: 20px;
  border-radius: 4px;
}

.grant-wrapper {
  margin-top: 20px;
}
</style>