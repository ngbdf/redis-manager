<template>
  <div>
    <div id="analysis" ref="mainBoard">
      <div>
        <el-button
          plain
          type="primary"
          size="small"
          class="header-button"
          icon="el-icon-plus"
          @click="handleAdd()"
        >Add Job</el-button>
      </div>
      <div>
        <el-table :data="rdbAnaysisList" style="width: 100%">
          <el-table-column label="Name" prop="name"></el-table-column>
          <el-table-column label="Cluster" prop="cluster"></el-table-column>
          <el-table-column label="Status" prop="status"></el-table-column>
          <el-table-column label="Auto" prop="auto"></el-table-column>
          <el-table-column>
            <template slot-scope="scope" align="left">
              <el-button size="mini" @click="handleEdit(scope.$index, scope.row)">Edit</el-button>
              <el-button
                size="mini"
                type="danger"
                @click="handleDelete(scope.$index, scope.row)"
              >Delete</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <div ref="addJob" class="header-form">
      <el-form
        :model="ruleForm"
        status-icon
        ref="ruleForm"
        label-width="100px"
        class="demo-ruleForm"
      >
        <el-form-item label="密码" prop="pass">
          <el-input type="password" v-model="ruleForm.pass" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="checkPass">
          <el-input type="password" v-model="ruleForm.checkPass" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input v-model.number="ruleForm.age"></el-input>
        </el-form-item>
        <el-form-item>
          <!-- <el-button type="primary" @click="submitForm('ruleForm')">提交</el-button>
          <el-button @click="resetForm('ruleForm')">重置</el-button>-->
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>
<script>
import { store } from "@/vuex/store.js";
import { isEmpty, validateIpAndPort } from "@/utils/validate.js";
import API from "@/api/api.js";
export default {
  data () {
    return {
      rdbAnaysisList: [
        {
          name: 'e11_www',
          cluster: 'e11_wwwredis',
          status: 'success',
          auto: false
        }
      ],
      ruleForm: {
        pass: '',
        checkPass: '',
        age: ''
      }
    }
  },
  methods: {
    handleEdit () {},
    handleDelete () {},
    getRdbAnaysisList () {},
    handleAdd () {
      this.$refs.mainBoard.style.display = 'none'
      this.$refs.addJob.style.display = 'block'
    }
  }
};
</script>
<style scoped>
.header-button {
  float: right;
  margin-bottom: 20px;
}
.header-form {
  display: none;
}
</style>
