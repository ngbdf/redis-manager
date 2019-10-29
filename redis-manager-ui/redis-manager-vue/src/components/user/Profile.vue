<template>
  <div id="profile" class="body-wrapper" v-loading="updateUserLoading">
    <div class="profile-wrapper">
      <el-form :model="user" status-icon ref="user" label-width="100px" :rules="rules">
        <el-form-item label="Avatar">
          <el-avatar src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"></el-avatar>
        </el-form-item>
        <el-form-item label="User Role" prop="userRole">
          <el-tag size="small" v-if="user.userRole == 0">Super Admin</el-tag>
          <el-tag size="small" v-if="user.userRole == 1">Admin</el-tag>
          <el-tag size="small" v-if="user.userRole == 2">Member</el-tag>
        </el-form-item>
        <el-form-item label="User Name" prop="userName">
          <el-tag size="small">{{ user.userName }}</el-tag>
        </el-form-item>
        <el-form-item label="Password" prop="password">
          <el-input
            size="small"
            type="password"
            v-model.trim="user.password"
            maxlength="255"
            show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input size="small" v-model.trim="user.email"></el-input>
        </el-form-item>
        <el-form-item label="Mobile" prop="mobile">
          <el-input size="small" v-model.number="user.mobile"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button size="small" type="primary" @click="updateUser('user')">Update</el-button>
          <el-button size="small" @click="resetUser('user')">Reset</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>
<script>
import { store } from "@/vuex/store.js";
import API from "@/api/api.js";
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
    return {
      user: {},
      rules: {
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
      updateUserLoading: false
    };
  },
  methods: {
    getUser(userId) {
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
            this.user = this.currentUser;
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    updateUser(user) {
      this.$refs[user].validate(valid => {
        if (valid) {
          this.updateUserLoading = true;
          let url = "/user/updateUser";
          API.post(
            url,
            this.user,
            response => {
              let result = response.data;
              if (result.code == 0) {
                message.success("update user successfully");
              } else {
                message.error("update user failed");
              }
              this.updateUserLoading = false;
            },
            err => {
              this.updateUserLoading = false;
              message.error(err);
            }
          );
        }
      });
    },
    resetUser(user) {
      this.getUser(this.currentUser.userId);
    }
  },
  computed: {
    currentUser() {
      return store.getters.getUser;
    }
  },
  mounted() {
    let userId = store.getters.getUser.userId;
    this.getUser(userId);
  }
};
</script>
<style scoped>
.body-wrapper {
  padding-right: 25%;
  padding-left: 25%;
}
.profile-wrapper {
  margin: 0 auto;
  max-width: 960px;
}
</style>