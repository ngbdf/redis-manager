<template>
  <div id="profile" class="body-wrapper">
    <div class="profile-wrapper">
      <el-form :model="user" status-icon ref="userForm" label-width="100px" class="demo-ruleForm">
        <el-form-item label="Avatar">
          <el-avatar src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"></el-avatar>
        </el-form-item>
        <el-form-item label="User Role" prop="userRole">
          <el-tag size="small">Admin</el-tag>
        </el-form-item>
        <el-form-item label="User Name" prop="userName">
          <el-input size="small" type="text" v-model="user.userName"></el-input>
        </el-form-item>
        <el-form-item label="Password" prop="password">
          <el-input size="small" type="password" v-model="user.password"></el-input>
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input size="small" v-model="user.email"></el-input>
        </el-form-item>
        <el-form-item label="Mobile" prop="mobile">
          <el-input size="small" v-model.number="user.mobile"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button size="small" type="primary" @click="submitForm('ruleForm')">Confirm</el-button>
          <el-button size="small" @click="resetForm('userForm')">Reset</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>
<script>
export default {
  data() {
    return {
      user: {}
    };
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