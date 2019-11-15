<template>
  <div id="profile" class="body-wrapper" v-loading="updateUserLoading">
    <div class="profile-wrapper">
      <el-form :model="user" status-icon ref="user" label-width="100px" :rules="rules">
        <el-form-item label="Avatar">
          <el-upload
            class="avatar-uploader"
            name="avatarFile"
            :action="avatarUrl"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :data="user"
            :before-upload="beforeAvatarUpload"
            title="Change avatar"
          >
            <img
              v-if="user.avatar != null && user.avatar != ''"
              :src="user.avatar"
              class="avatar"
              style="object-fit: cover;"
            />
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
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
import apiConfig from "@/api/apiConfig.js";
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
      avatarUrl: apiConfig.baseUrl + "/user/saveAvatar",
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
            let user = result.data;
            let avatar = user.avatar;
            if (!isEmpty(avatar)) {
              user.avatar = apiConfig.baseUrl + avatar;
            }
            this.user = user;
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
    },
    handleAvatarSuccess(res, file) {
      let avatar = URL.createObjectURL(file.raw);
      this.user.avatar = avatar;
      let user = store.getters.getUser;
      user.avatar = avatar;
      store.dispatch("setUser", user);
    },
    beforeAvatarUpload(file) {
      const isJPGOrPNG =
        file.type === "image/jpeg" || "image/jpg" || file.type === "image/png";
      const isLt5M = file.size / 1024 / 1024 < 5;

      if (!isJPGOrPNG) {
        message.error("Uploading images can only be in JPG or PNG format!");
      }
      if (!isLt5M) {
        message.error("Upload image size can't exceed 5MB!");
      }
      return isJPGOrPNG && isLt5M;
    }
  },
  computed: {
    currentUser() {
      return store.getters.getUser;
    }
  },
  mounted() {
    let userId = this.currentUser.userId;
    if (userId != this.$route.params.userId) {
      this.$$router.push({ name: "profile", params: { userId: userId } });
    }
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

.avatar-uploader >>> .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader >>> .el-upload:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
}
</style>