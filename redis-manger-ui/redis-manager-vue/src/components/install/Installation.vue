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
                <el-tag size="small">{{ currentGroup.groupName }}</el-tag>
              </el-form-item>
              <el-form-item label="Cluster Name" prop="clusterName">
                <el-input v-model="installationParam.clusterName" maxlength="50" show-word-limit></el-input>
              </el-form-item>
              <el-form-item label="Redis Password" prop="redisPassword">
                <el-input v-model="installationParam.redisPassword" maxlength="50" show-word-limit></el-input>
              </el-form-item>
              <el-form-item label="Redis Mode" prop="redisMode">
                <el-radio-group v-model="installationParam.redisMode">
                  <el-radio label="cluster">Cluster</el-radio>
                  <el-radio label="standalone">Standalone</el-radio>
                </el-radio-group>
              </el-form-item>
              <!-- environment start -->
              <el-form-item label="Environment" prop="installationEnvironment">
                <el-radio-group v-model="installationParam.installationEnvironment">
                  <el-radio label="0">Docker</el-radio>
                  <el-radio label="1">Machine</el-radio>
                  <!-- <el-radio-button label="Kubernetes"></el-radio-button> -->
                </el-radio-group>
              </el-form-item>
              <!-- environment end -->
              <!-- image start -->
              <el-form-item
                label="Image"
                prop="image"
                v-if="installationParam.installationEnvironment == '0'"
              >
                <el-select
                  allow-create
                  filterable
                  v-model="installationParam.image"
                  placeholder="Please choose image"
                >
                  <el-option
                    v-for="image in dockerImages"
                    :key="image"
                    :label="image"
                    :value="image"
                  ></el-option>
                </el-select>
              </el-form-item>
              <el-form-item
                label="Image"
                prop="image"
                v-if="installationParam.installationEnvironment == '1'"
              >
                <el-select v-model="installationParam.package" placeholder="Please choose image">
                  <el-option
                    v-for="image in machineImages"
                    :key="image"
                    :label="image"
                    :value="image"
                  ></el-option>
                </el-select>
              </el-form-item>
              <!-- image end -->

              <el-form-item label="Auto Build" prop="autoBuild">
                <el-switch v-model="installationParam.autoBuild"></el-switch>
              </el-form-item>
              <!-- auto install start -->

              <el-form-item label="Machine List" prop="machines" v-if="installationParam.autoBuild">
                <div class="block">
                  <el-cascader
                    placeholder="Search"
                    :options="allMachineList"
                    :props="{ multiple: true }"
                    filterable
                    @change="handleChange"
                    :leafOnly="true"
                    v-model="installationParam.machines"
                  ></el-cascader>
                </div>
              </el-form-item>

              <el-form-item label="Start Port" prop="startPort" v-if="installationParam.autoBuild">
                <el-input v-model.number="installationParam.startPort"></el-input>
              </el-form-item>

              <el-form-item
                label="Master Number"
                prop="masterNumber"
                v-if="installationParam.autoBuild"
              >
                <el-input v-model.number="installationParam.masterNumber"></el-input>
              </el-form-item>
              <el-form-item
                label="Replica Number"
                prop="replicaNumber"
                v-if="installationParam.autoBuild"
              >
                <el-input v-model.number="installationParam.replicaNumber"></el-input>
              </el-form-item>

              <!-- auto install end -->
              <!-- custom installation start -->
              <el-form-item
                label="Machine User"
                prop="machineUserName"
                v-if="!installationParam.autoBuild"
              >
                <el-input
                  v-model="installationParam.machineUserName"
                  placeholder="Machine user name"
                ></el-input>
              </el-form-item>
              <el-form-item
                label="Machine Password"
                prop="machinePassword"
                v-if="!installationParam.autoBuild"
              >
                <el-input
                  v-model="installationParam.machinePassword"
                  placeholder="Machine password"
                ></el-input>
              </el-form-item>
              <el-form-item label="Topology" prop="topology" v-if="!installationParam.autoBuild">
                <el-input
                  type="textarea"
                  :autosize="{ minRows: 2, maxRows: 12}"
                  placeholder="Please enter redis node list"
                  v-model="installationParam.topology"
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
                v-if="installationParam.redisMode == 'cluster'"
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
                <el-input type="input" v-model="installationParam.clusterInfo"></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="success" @click="installationCheck('installationParam')">Check</el-button>
                <!-- <el-button @click="resetForm('installationParam')">Reset</el-button> -->
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
    <el-dialog title="Installation Info" :visible.sync="installationInfoVisible" width="50%">
      <span>这是一段信息</span>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="installationInfoVisible = false">Cancel</el-button>
        <el-button size="small" type="primary" @click="installationInfoVisible = false">Install</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { store } from "@/vuex/store.js";
import { isEmpty, validatePort } from "@/utils/validate.js";
import API from "@/api/api.js";
export default {
  data() {
    var validateClusterName = (rule, value, callback) => {
      if (isEmpty(value) || isEmpty(value.trim())) {
        return callback(new Error("Please enter cluster name"));
      } else {
        let url = "/cluster/validateClusterName/" + value;
        API.get(
          url,
          null,
          response => {
            let result = response.data;
            if (result.code != 0) {
              let cluster = result.data;
              return callback(new Error(value + " has exist"));
            } else {
              callback();
            }
          },
          err => {
            return callback(new Error("Network error, " + err));
          }
        );
      }
    };
    var validateStartPort = (rule, value, callback) => {
      if (!validatePort(value)) {
        return callback(new Error("Incorrect port format"));
      }
      callback();
    };
    var validateMasterAndReplicaNumber = (rule, value, callback) => {
      console.log(value);
      if (value <= 0) {
        return callback(new Error("Number must be greater than 0"));
      }
      callback();
    };
    return {
      dockerImages: [],
      machineImages: [],
      installationParam: {
        groupId: "",
        clusterName: "",
        redisMode: "cluster",
        create: true,
        machineIdList: [],
        machineList: [{ ip: "" }, { ip: "" }],
        autoBuild: true,
        autoInit: true,
        sudo: true,
        startPort: "",
        redisNodeList: "127.0.0.1:8001 master\n127.0.0.1:8002",

        installationEnvironment: "0"
      },
      installationInfoVisible: false,
      rules: {
        clusterName: [
          {
            required: true,
            validator: validateClusterName,
            trigger: "blur"
          }
        ],
        redisMode: [
          {
            required: true,
            message: "Please select redis mode",
            trigger: "change"
          }
        ],
        installationEnvironment: [
          {
            required: true,
            message: "Please select installation environment",
            trigger: "change"
          }
        ],
        image: [
          {
            required: true,
            message: "Please select image",
            trigger: "change"
          }
        ],
        machines: [
          {
            required: true,
            message: "Please select machines",
            trigger: "change"
          }
        ],
        startPort: [
          {
            required: true,
            message: "Please enter start port",
            trigger: "blur"
          },
          {
            required: true,
            validator: validateStartPort,
            trigger: "blur"
          }
        ],
        masterNumber: [
          {
            required: true,
            message: "Please eneter master number",
            trigger: "blur"
          },
          {
            type: "number",
            message: "Please enter integer",
            trigger: "blur"
          },
          {
            required: true,
            validator: validateMasterAndReplicaNumber,
            trigger: "blur"
          }
        ],
        replicaNumber: [
          {
            required: true,
            message: "Please eneter replica number",
            trigger: "blur"
          },
          { type: "number", message: "Please enter integer", trigger: "blur" },
          {
            required: true,
            validator: validateMasterAndReplicaNumber,
            trigger: "blur"
          }
        ],
        machineUserName: [
          {
            required: true,
            message: "Please enter machine user name",
            trigger: "blur"
          }
        ],
        machinePassword: [
          {
            required: true,
            message: "Please enter machine password",
            trigger: "blur"
          }
        ],
        topology: [
          {
            required: true,
            message: "Please enter redis topology",
            trigger: "blur"
          }
        ]
      },
      allMachineList: []
    };
  },
  methods: {
    handleChange(value) {
      console.log(value);
    },
    installationCheck(installationParam) {
      this.installationInfoVisible = true;
      this.$refs[installationParam].validate(valid => {
        if (valid) {
          alert("submit!");
        } else {
          console.log("error submit!!");
          return false;
        }
      });
    },
    // resetForm(formName) {
    //   this.$refs[formName].resetFields();
    //   this.checkPass = false;
    // },
    getDockerImageList() {
      let url = "/installation/getDockerImages";
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.dockerImages = result.data;
          } else {
            console.log(result.message);
          }
        },
        err => {
          console.log(err);
        }
      );
    },
    getMachineImageList() {
      let url = "/installation/getMachineImages";
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.machineImages = result.data;
          } else {
            console.log(result.message);
          }
        },
        err => {
          console.log(err);
        }
      );
    },
    getMachineList(groupId) {
      if (isEmpty(groupId)) {
        return;
      }
      let url = "/machine/getHierarchyMachineList/" + groupId;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            let hierarchyMachineList = result.data;
            hierarchyMachineList.forEach(oneMachineGroup => {
              let machineGroupName = oneMachineGroup.machineGroupName;
              oneMachineGroup.label = machineGroupName;
              oneMachineGroup.value = machineGroupName;
              let children = oneMachineGroup.children;
              children.forEach(machine => {
                machine.label = machine.host;
                machine.value = machine.machineId;
              });
              this.allMachineList = hierarchyMachineList;
            });
          } else {
            console.log(result.message);
          }
        },
        err => {
          console.log(err);
        }
      );
    }
  },
  computed: {
    currentGroup() {
      return store.getters.getCurrentGroup;
    }
  },
  watch: {
    "installationParam.autoBuild": function(newValue, oldValue) {
      let fields = this.$refs["installationParam"].fields;

      fields.map(field => {
        if (
          field.prop === "startPort" ||
          field.prop === "masterNumber" ||
          field.prop === "replicaNumber" ||
          field.prop === "machineUserName" ||
          field.prop === "machinePassword"
        ) {
          console.log(field.prop);
          field.resetField();
          return false;
        }
      });
    }
  },
  mounted() {
    this.getDockerImageList();
    this.getMachineImageList();
    let groupId = this.$route.params.groupId;
    this.getMachineList(groupId);
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
  min-height: 600px;
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