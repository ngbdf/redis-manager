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
import API from "@/api/api.js";
export default {
  props: {
    clusterId: {
      type: Number
    }
  },
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
            if (response.data.code != 0) {
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
    var validateRedisNode = (rule, value, callback) => {
      if (isEmpty(value) || isEmpty(value.trim())) {
        return callback(new Error("Please enter redis node"));
      } else {
        if (!validateIpAndPort(value)) {
          return callback(new Error("Incorrect format"));
        }
        callback();
      }
    };
    var validateConnection = (rule, value, callback) => {
      let url = "/validate/address/" + value.trim();
      API.get(
        url,
        null,
        response => {
          if (response.data.code != 0) {
            return callback(new Error("Redis node incorrect"));
          } else {
            callback();
          }
        },
        err => {
          return callback(new Error("Network error, " + err));
        }
      );
    };
    var validateInstallationEnvironment = (rule, value, callback) => {
      if (isEmpty(value) || isEmpty(value.trim())) {
        return callback(new Error("Please select environment"));
      }
      callback();
    };
    return {
      cluster: {
        nodeList: [{ value: "" }]
      },
      rules: {
        clusterName: [
          { required: true, validator: validateClusterName, trigger: "blur" }
        ],
        redisNode: [
          { required: true, validator: validateRedisNode, trigger: "blur" },
          { required: true, validator: validateConnection, trigger: "blur" }
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
    },
    nodesToNodeList(nodes) {
      let nodeArr = nodes.split(",");
      let nodeList = [];
      nodeArr.forEach(node => {
        if (!isEmpty(node)) {
          nodeList.push({ value: node });
        }
      });
      return nodeList;
    }
  },
  computed: {
    // 监听group变化
    currentGroup() {
      return store.getters.getGroup;
    }
  },
  mounted() {
    if (isEmpty(this.clusterId)) {
      this.cluster = {
        nodeList: [{ value: "" }]
      };
    } else {
      let cluster = {
        clusterId: 1,
        groupId: 1,
        userId: 1,
        clusterName: "Shanghai",
        clusterToken: "ajsGako;3an;fnKS12a",
        redisMode: "cluster",
        os: "Linux 3.10.0-327.36.3.el7.x86_64 x86_64",
        redisVersion: "4.0.10",
        image: "redis:4.0.10",
        nodes: "127.0.0.1:8001,127.0.0.1:8002,127.0.0.1:8003",
        totalKeys: 345435,
        totalExpires: 342,
        dbSize: 1,
        clusterStatus: "HEALTH",
        clusterSlotsAssigned: 16384,
        clusterSlotsOk: 16384,
        clusterSlotsPfail: 0,
        clusterSlotsFail: 0,
        clusterKnownNodes: 120,
        clusterSize: 40,
        redisPassword: "1234",
        installationEnvironment: "docker",
        installationType: 0,
        clusterInfo: "hello",
        nodeList: [{ value: "" }]
      };
      let nodeList = this.nodesToNodeList(cluster.nodes);
      cluster.nodeList = nodeList;
      this.cluster = cluster;
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