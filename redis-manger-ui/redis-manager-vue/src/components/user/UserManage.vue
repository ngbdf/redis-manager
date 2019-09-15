<template>
  <div id="user-manage" class="body-wrapper">
    <div class="operation-wrapper">
      <el-button size="mini" type="success" @click="createUserVisible = true">Create</el-button>
    </div>
    <el-table :data="userList">
      <el-table-column type="index" width="50"></el-table-column>
      <el-table-column property="userName" label="User Name"></el-table-column>
      <el-table-column label="Channel Type">
        <template slot-scope="scope">
          <el-popover trigger="hover" placement="top">
            <p>password: {{ scope.row.password }}</p>
            <div slot="reference">
              <span>*********</span>
            </div>
          </el-popover>
        </template>
      </el-table-column>
      <el-table-column label="User Role">
        <template slot-scope="scope">
          <el-tag size="small" type="success">{{scope.row.userRole}}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="User Role">
        <template slot-scope="scope">
          <el-avatar :src="scope.row.headPic"></el-avatar>
        </template>
      </el-table-column>

      <el-table-column property="email" label="Email"></el-table-column>
      <el-table-column property="mobile" label="Mobile"></el-table-column>
      <el-table-column property="userType" label="User Type"></el-table-column>
      <el-table-column property="updateTime" label="Time"></el-table-column>
      <el-table-column label="Operation" width="200px;">
        <template slot-scope="scope">
          <el-button size="mini" type="primary" @click="handleEdit(scope.$index, scope.row)">Edit</el-button>
          <el-button size="mini" type="danger" @click="handleDelete(scope.$index, scope.row)">Delete</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog title="Create User" :visible.sync="createUserVisible">
      <el-form :model="user">
        <el-form-item label="User Name" label-width="100px">
          <el-input size="small" v-model="user.userName"></el-input>
        </el-form-item>
        <el-form-item label="Password" label-width="100px">
          <el-input size="small" v-model="user.password"></el-input>
        </el-form-item>
        <el-form-item label="User Role" label-width="100px">
          <el-select size="small" v-model="user.userRole" placeholder="Please select user role">
            <el-option label="Member" value="member"></el-option>
            <el-option label="Admin" value="admin"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Email" label-width="100px">
          <el-input size="small" v-model="user.email"></el-input>
        </el-form-item>
        <el-form-item label="Mobile" label-width="100px">
          <el-input size="small" v-model="user.mobile"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" type="primary">Confirm</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      userList: [
        {
          userName: "Jay",
          password: "23333",
          userRole: "admin",
          headPic:
            "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png",
          email: "zouhuajianclever@gmail.com",
          mobile: "15112345678",
          userType: "0",
          updateTime: "2019-08-25"
        }
      ],
      createUserVisible: false,
      user: {}
    };
  },
  methods: {
    handleEdit(index, row) {
      console.log(index, row);
    },
    handleDelete(index, row) {
      console.log(index, row);
    }
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