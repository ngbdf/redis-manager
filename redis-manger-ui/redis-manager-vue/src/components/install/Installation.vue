<template>
  <div id="installation">
    <el-row>
      <el-col :xl="12" :lg="12" :md="24" :sm="24">
        <div class="form-wrapper">
          <div class="form">
            <el-form
              :model="ruleForm"
              :rules="rules"
              ref="ruleForm"
              label-width="120px"
              class="demo-ruleForm"
              size="small"
            >
              <el-form-item label="Group Name" prop="groupName">
                <el-tag>Bigdata</el-tag>
              </el-form-item>
              <el-form-item label="Cluster Name" prop="clusterName">
                <el-input v-model="ruleForm.name"></el-input>
              </el-form-item>
              <el-form-item label="Environment" prop="clusterName">
                <el-radio-group v-model="installationEnvironment">
                  <el-radio-button label="Docker"></el-radio-button>
                  <el-radio-button label="Machine"></el-radio-button>
                  <!-- <el-radio-button label="Kubernetes"></el-radio-button> -->
                </el-radio-group>
              </el-form-item>
              <!-- image start -->
              <el-form-item label="Image" prop="image" v-if="installationEnvironment == 'Docker'">
                <el-select v-model="ruleForm.region" placeholder="Please choose image">
                  <el-option label="redis:4.0.10" value="redis:4.0.10"></el-option>
                  <el-option label="redis:4.0.14" value="redis:4.0.14"></el-option>
                </el-select>
                <el-popover placement="bottom" title="Customize Image" width="200" trigger="click">
                  <el-input v-model="ruleForm.iamge"></el-input>
                  <el-button slot="reference" size="mini" title="Customize Image" icon="el-icon-plus" circle></el-button>
                </el-popover>
              </el-form-item>
              <el-form-item label="Image" prop="image" v-if="installationEnvironment == 'Machine'">
                <el-select v-model="ruleForm.region" placeholder="Please choose image">
                  <el-option label="4.0.10" value="4.0.10"></el-option>
                  <el-option label="4.0.14" value="4.0.14"></el-option>
                </el-select>
              </el-form-item>
              <!-- image end -->

              <el-form-item label="Auto Build" prop="auto">
                <el-switch v-model="ruleForm.auto"></el-switch>
              </el-form-item>
              <el-form-item label="活动性质" prop="type">
                <el-checkbox-group v-model="ruleForm.type">
                  <el-checkbox label="美食/餐厅线上活动" name="type"></el-checkbox>
                  <el-checkbox label="地推活动" name="type"></el-checkbox>
                  <el-checkbox label="线下主题活动" name="type"></el-checkbox>
                  <el-checkbox label="单纯品牌曝光" name="type"></el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              <el-form-item label="特殊资源" prop="resource">
                <el-radio-group v-model="ruleForm.resource">
                  <el-radio label="线上品牌商赞助"></el-radio>
                  <el-radio label="线下场地免费"></el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="活动形式" prop="desc">
                <el-input type="textarea" v-model="ruleForm.desc"></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitForm('ruleForm')">立即创建</el-button>
                <el-button @click="resetForm('ruleForm')">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-col>
      <el-col :xl="12" :lg="12" :md="24" :sm="24">
        <div class="console-wrapper">
          <div class="console-title">Redis Installation Console</div>
          <pre class="console">Prepare to install redis...</pre>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  data() {
    return {
      installationEnvironment: "Docker",
      ruleForm: {
        name: "",
        region: "",
        date1: "",
        date2: "",
        delivery: false,
        type: [],
        resource: "",
        desc: ""
      },
      rules: {
        name: [
          { required: true, message: "请输入活动名称", trigger: "blur" },
          { min: 3, max: 5, message: "长度在 3 到 5 个字符", trigger: "blur" }
        ],
        region: [
          { required: true, message: "请选择活动区域", trigger: "change" }
        ],
        date1: [
          {
            type: "date",
            required: true,
            message: "请选择日期",
            trigger: "change"
          }
        ],
        date2: [
          {
            type: "date",
            required: true,
            message: "请选择时间",
            trigger: "change"
          }
        ],
        type: [
          {
            type: "array",
            required: true,
            message: "请至少选择一个活动性质",
            trigger: "change"
          }
        ],
        resource: [
          { required: true, message: "请选择活动资源", trigger: "change" }
        ],
        desc: [{ required: true, message: "请填写活动形式", trigger: "blur" }]
      }
    };
  },
  methods: {
    submitForm(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          alert("submit!");
        } else {
          console.log("error submit!!");
          return false;
        }
      });
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
    }
  }
};
</script>

<style scoped>
#installation {
  padding: 20px;
  background-color: #ffffff;
}

.current-group {
  margin-bottom: 20px;
}

.form-wrapper {
  margin-right: 10px;
}

.console-wrapper {
  margin-left: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.console {
  min-height: 500px;
  padding: 10px 20px;
  background-color: black;
  color: #ffffff;
  word-break: break-all;
  word-wrap: break-word;
  font-family: Consolas, Monaco, Menlo, "Courier New", monospace !important;
  margin: 0;
}
.console-title {
  padding: 10px 20px;
  border-bottom: 1px solid #dcdfe6;
  background: #f0f2f5;
}
</style>