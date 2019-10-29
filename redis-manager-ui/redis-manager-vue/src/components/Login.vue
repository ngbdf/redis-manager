<template>
  <!--background-color: #f0f2f5; background-image: linear-gradient(0deg,#04c0c6,#2ba3de 51%,#835be3); -->
  <el-container style="background-image: linear-gradient(0deg,#1ac5fa,#2ba3de 51%,#1d71f2);">
    <!-- #1d71f2 #1ac5fa -->
    <img
      src="../assets/redis-manager.png"
      style="position: absolute; z-index: 0;left: 0; top:32%; width: 100%; opacity:0.1;"
    />
    <div class="login-wrapper">
      <el-card shadow="hover" class="content-wrapper">
        <div slot="header">
          <b>Sign In</b>
        </div>
        <div>
          <el-form :model="user" :rules="rules" ref="user">
            <el-form-item prop="userName">
              <el-input
                prefix-icon="el-icon-user"
                v-model.trim="user.userName"
                placeholder="User Name"
              ></el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                prefix-icon="el-icon-key"
                type="password"
                v-model.trim="user.password"
                placeholder="Password"
                show-password
              ></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" style="width: 100%;" @click="signIn('user')">Sign In</el-button>
            </el-form-item>
            <el-form-item>
              <el-button
                type="success"
                style="width: 100%;"
                @click="oAuthSignIn('user')"
              >OAuth Sign In</el-button>
            </el-form-item>
          </el-form>
        </div>
        <!-- <el-divider></el-divider> -->
        <div class="link-wrapper">
          <span>
            <span>More about</span>
            <el-link
              type="primary"
              target="_blank"
              class="doc-link"
              :underline="false"
            >Redis Manager</el-link>
          </span>
        </div>
      </el-card>
    </div>
  </el-container>
</template>

<script>
import API from "@/api/api.js";
import { store } from "@/vuex/store.js";
import message from "@/utils/message.js";
export default {
  data() {
    return {
      user: {},
      rules: {
        userName: [
          { required: true, message: "Please enter user name", trigger: "blur" }
        ],
        password: [
          { required: true, message: "Please enter password", trigger: "blur" }
        ]
      }
    };
  },
  methods: {
    signIn(user) {
      this.$refs[user].validate(valid => {
        if (valid) {
          let url = "/user/login";
          API.post(
            url,
            this.user,
            response => {
              let result = response.data;
              if (result.code == 0) {
                store.dispatch("setUser", result.data);
                this.$router.push({ name: "index" });
              } else {
                message.error("User name or password wrong");
              }
            },
            err => {
              message.error(err);
            }
          );
        }
      });
    }
  }
};
</script>

<style scoped>
.el-container {
  height: 100%;
}
.login-wrapper {
  background-color: #ffffff !important;
  margin: 0px auto;
  display: flex;
  align-self: center;
  margin-top: -5%;
}
.content-wrapper {
  width: 350px;
}
.el-divider {
  margin: 5px 0;
}
.link-wrapper {
  display: flex;
  justify-content: center;
  font-size: 12px;
  color: #909399;
}
.doc-link {
  font-size: 12px;
}
</style>