<template>
  <div id="installation" class="body-wrapper">
    <div class="step-wrapper">
      <el-steps :active="1" finish-status="success">
        <el-step title="Environment Check"></el-step>
        <el-step title="Pull Image" description></el-step>
        <el-step title="Pull Config" description></el-step>
        <el-step title="Install" description></el-step>
        <el-step title="Init" description></el-step>
        <el-step title="Finish" description></el-step>
      </el-steps>
    </div>
    <el-row>
      <el-col :xl="12" :lg="12" :md="24" :sm="24">
        <div class="form-wrapper">
          <div class="form">
            <el-form
              :model="installationParam"
              :rules="rules"
              ref="installationParam"
              label-width="150px"
              class="demo-ruleForm"
              size="small"
            >
              <el-form-item label="Group Name" prop="groupName">
                <el-tag>Bigdata</el-tag>
              </el-form-item>
              <el-form-item label="Cluster Name" prop="clusterName">
                <el-input v-model="installationParam.cluster.clusterName"></el-input>
              </el-form-item>
              <el-form-item label="Redis Password" prop="redisPassword">
                <el-input v-model="installationParam.cluster.redisPassword"></el-input>
              </el-form-item>
              <el-form-item label="Redis Mode" prop="redisMode">
                <el-radio-group v-model="installationParam.cluster.redisMode">
                  <el-radio label="cluster"></el-radio>
                  <el-radio label="standalone"></el-radio>
                </el-radio-group>
              </el-form-item>
              <!-- environment start -->
              <el-form-item label="Environment" prop="installationEnvironment">
                <el-radio-group v-model="installationParam.installationEnvironment">
                  <el-radio-button label="Docker"></el-radio-button>
                  <el-radio-button label="Machine"></el-radio-button>
                  <!-- <el-radio-button label="Kubernetes"></el-radio-button> -->
                </el-radio-group>
              </el-form-item>
              <!-- environment end -->
              <!-- image start -->
              <el-form-item
                label="Image"
                prop="image"
                v-if="installationParam.installationEnvironment == 'Docker'"
              >
                <el-select v-model="installationParam.region" placeholder="Please choose image">
                  <el-option label="redis:4.0.10" value="redis:4.0.10"></el-option>
                  <el-option label="redis:4.0.14" value="redis:4.0.14"></el-option>
                </el-select>
                <el-popover placement="bottom" title="Customize Image" width="200" trigger="click">
                  <el-input v-model="installationParam.iamge"></el-input>
                  <el-button
                    slot="reference"
                    size="mini"
                    title="Customize Image"
                    icon="el-icon-plus"
                    circle
                  ></el-button>
                </el-popover>
              </el-form-item>
              <el-form-item
                label="Image"
                prop="image"
                v-if="installationParam.installationEnvironment == 'Machine'"
              >
                <el-select v-model="installationParam.image" placeholder="Please choose image">
                  <el-option label="4.0.10" value="4.0.10"></el-option>
                  <el-option label="4.0.14" value="4.0.14"></el-option>
                </el-select>
              </el-form-item>
              <!-- image end -->

              <el-form-item label="Auto Build" prop="autoBuild">
                <el-switch v-model="installationParam.autoBuild"></el-switch>
              </el-form-item>
              <!-- auto install start -->

              <el-form-item
                label="Machine List"
                prop="machineList"
                v-if="installationParam.autoBuild"
              >
                <div class="block">
                  <el-cascader
                    placeholder="Search"
                    :options="allMachineList"
                    :props="{ multiple: true }"
                    filterable
                  ></el-cascader>
                </div>
              </el-form-item>

              <el-form-item label="Start Port" prop="startPort" v-if="installationParam.autoBuild">
                <el-input v-model="installationParam.startPort"></el-input>
              </el-form-item>

              <el-form-item
                label="Master Number"
                prop="masterNumber"
                v-if="installationParam.autoBuild"
              >
                <el-input v-model="installationParam.masterNumber"></el-input>
              </el-form-item>
              <el-form-item
                label="Replica Number"
                prop="replicaNumber"
                v-if="installationParam.autoBuild"
              >
                <el-input v-model="installationParam.replicaNumber"></el-input>
              </el-form-item>

              <!-- auto install end -->
              <!-- custom installation start -->
              <el-form-item
                label="Machine User"
                prop="machineUser"
                v-if="!installationParam.autoBuild"
              >
                <el-input
                  v-model="installationParam.machineUser"
                  style="width:  48%; margin-right: 20px;"
                  placeholder="Machine user name"
                ></el-input>
                <el-input
                  v-model="installationParam.machinePassword"
                  style="width: 48%;"
                  placeholder="Machine password"
                ></el-input>
              </el-form-item>
              <el-form-item
                label="Redis Node"
                prop="redisNodeList"
                v-if="!installationParam.autoBuild"
              >
                <el-input
                  type="textarea"
                  :autosize="{ minRows: 2, maxRows: 12}"
                  placeholder="Please enter redis node list"
                  v-model="installationParam.redisNodeList"
                  class="textarea"
                ></el-input>
              </el-form-item>

              <!-- custom installation end -->
              <el-form-item label="Sudo" prop="sudo">
                <el-switch v-model="installationParam.sudo"></el-switch>
                <el-tooltip
                  class="item"
                  effect="dark"
                  content="Whether to use sudo during the installation operation"
                  placement="top-start"
                >
                  <i class="el-icon-info info"></i>
                </el-tooltip>
              </el-form-item>
              <el-form-item
                label="Auto Init"
                prop="autoInit"
                v-if="installationParam.cluster.redisMode == 'cluster'"
              >
                <el-switch v-model="installationParam.autoInit"></el-switch>
                <el-tooltip
                  class="item"
                  effect="dark"
                  content="Automatic allocation slot"
                  placement="top-start"
                >
                  <i class="el-icon-info info"></i>
                </el-tooltip>
              </el-form-item>
              <el-form-item label="Cluster Info" prop="clusterInfo">
                <el-input type="input" v-model="installationParam.cluster.clusterInfo"></el-input>
              </el-form-item>
              <el-form-item>
                <el-button
                  type="success"
                  @click="installationCheck('installationParam')"
                  v-if="!checkPass"
                >Check</el-button>
                <el-button
                  type="primary"
                  @click="submitForm('installationParam')"
                  v-if="checkPass"
                >Create</el-button>
                <el-button @click="resetForm('installationParam')">Reset</el-button>
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
      checkPass: false,
      installationParam: {
        cluster: {
          groupId: "",
          clusterName: "",
          redisMode: "cluster"
        },
        create: true,
        machineIdList: [],
        machineList: [{ ip: "" }, { ip: "" }],
        autoBuild: true,
        autoInit: true,
        sudo: true,
        startPort: "",
        redisNodeList: "127.0.0.1:8001 master\n127.0.0.1:8002",

        installationEnvironment: "Docker"
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
      },
      allMachineList: [
        {
          value: "Shanghai",
          label: "Shanghai",
          children: [
            {
              value: "192.168.16.2",
              label: "192.168.16.2"
            },
            {
              value: "192.168.16.3",
              label: "192.168.16.3"
            }
          ]
        },
        {
          value: "Beijing",
          label: "Beijing",
          children: [
            {
              value: "192.168.16.2",
              label: "192.168.16.2"
            },
            {
              value: "192.168.16.3",
              label: "192.168.16.3"
            }
          ]
        },
        {
          value: "Hangzhou",
          label: "Hangzhou",
          children: [
            {
              value: "192.168.16.2",
              label: "192.168.16.2"
            },
            {
              value: "192.168.16.3",
              label: "192.168.16.3"
            }
          ]
        }
      ]
    };
  },
  methods: {
    installationCheck(installationParam) {
      this.checkPass = true;
    },
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
      this.checkPass = false;
    }
  }
};
</script>

<style scoped>
#installation {
  padding: 20px;
  background-color: #ffffff;
}

.step-wrapper {
  padding-top: 10px;
  padding-bottom: 10px;
  margin-bottom: 20px;
  border-bottom: 1px solid #dcdfe6;
}

.el-step__title {
  font-size: 14px !important;
}

.current-group {
  margin-bottom: 20px;
}

.form-wrapper {
  margin-right: 10px;
}

.info {
  color: #909399;
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