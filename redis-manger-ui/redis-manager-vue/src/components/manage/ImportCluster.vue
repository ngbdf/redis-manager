<template>
  <div>
    <el-form :model="cluster" ref="cluster" :rules="rules" label-width="120px">
      <el-form-item label="Group Name">
        <el-tag size="small">{{ currentGroup.groupName }}</el-tag>
      </el-form-item>
      <el-form-item label="Cluster Name" prop="clusterName">
        <el-input size="small" v-model="cluster.clusterName" maxlength="30" show-word-limit></el-input>
      </el-form-item>
      <el-form-item label="Redis Password" prop="redisPassword">
        <el-input size="small" v-model="cluster.redisPassword" maxlength="255" show-password></el-input>
      </el-form-item>
      <el-form-item
        v-for="(node, index) in cluster.nodeList"
        :label="'Redis Node ' + index"
        :key="node.key"
        :prop="'nodeList.' + index + '.value'"
        :rules="rules.redisNode"
      >
        <el-input size="small" v-model="node.value">
          <el-button slot="append" @click.prevent="removeNode(node)" icon="el-icon-delete"></el-button>
        </el-input>
      </el-form-item>
      <el-form-item label="Environment" prop="installationEnvironment">
        <el-radio-group v-model="cluster.installationEnvironment">
          <el-radio label="docker">Docker</el-radio>
          <el-radio label="machine">Machine</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="Cluster Info" prop="clusterInfo">
        <el-input size="small" v-model="cluster.clusterInfo"></el-input>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button size="small" @click="addNode()">New Node</el-button>
      <el-button size="small" type="primary" @click="importCluster('cluster')">Confirm</el-button>
    </div>
  </div>
</template>

<script>
import { store } from "@/vuex/store.js";
import { isEmpty, validateIpAndPort } from "@/utils/validate.js";
export default {
  data() {
    var validateClusterName = (rule, value, callback) => {
      if (isEmpty(value) || isEmpty(value.trim())) {
        return callback(new Error("Please enter cluster name."));
      } else {
        callback();
      }
    };
    var validateRedisNode = (rule, value, callback) => {
      if (isEmpty(value) || isEmpty(value.trim())) {
        return callback(new Error("Please enter redis node."));
      } else {
        if (!validateIpAndPort(value)) {
          return callback(new Error("Incorrect format."));
        }
        callback();
      }
    };
    var validateConnection = (rule, value, callback) => {
      callback();
    };
    var validateInstallationEnvironment = (rule, value, callback) => {
      if (isEmpty(value) || isEmpty(value.trim())) {
        return callback(new Error("Please select environment."));
      }
      callback();
    };
    return {
      cluster: {
        nodeList: [{ value: "" }]
      },
      rules: {
        clusterName: [
          { required: true, validator: validateClusterName, trigger: "change" }
        ],
        redisNode: [
          { required: true, validator: validateRedisNode, trigger: "blur" }
        ],
        installationEnvironment: [
          {
            required: true,
            validator: validateInstallationEnvironment,
            trigger: "change"
          }
        ]
      }
    };
  },
  methods: {
    // 导入外部集群
    importCluster(cluster) {
      this.$refs[cluster].validate(valid => {
        if (valid) {
          if (isEmpty(this.groupId)) {
            console.log("Group id is exist!");
            return;
          }
          this.cluster.groupId = currentGroup.groupId;
          this.cluster.userId = store.getters.getUserId;
          let nodes = "";
          this.cluster.nodeList.forEach(node => {
            nodes += node.value + ",";
          });
          this.cluster.nodes = nodes;
          console.log(this.cluster);
          // axios
        }
      });
    },
    removeNode(item) {
      var index = this.cluster.nodeList.indexOf(item);
      if (index !== -1) {
        this.cluster.nodeList.splice(index, 1);
      }
    },
    addNode() {
      if (this.cluster.nodeList.length >= 5) {
        return;
      }
      this.cluster.nodeList.push({
        value: "",
        key: Date.now()
      });
    }
  },
  computed: {
    // 监听group变化
    currentGroup() {
      return store.getters.getGroup;
    }
  }
};
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>