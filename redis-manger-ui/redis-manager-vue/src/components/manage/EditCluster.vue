<template>
  <div>
    <el-form :model="cluster" ref="cluster" :rules="rules" label-width="120px" size="small">
      <el-form-item label="Group Name">
        <el-tag size="small">{{ currentGroup.groupName }}</el-tag>
      </el-form-item>
      <el-form-item label="Cluster Name" prop="clusterName">
        <el-input v-model="cluster.clusterName" maxlength="30" show-word-limit></el-input>
      </el-form-item>
      <el-form-item label="Redis Password" prop="redisPassword">
        <el-input v-model="cluster.redisPassword" maxlength="255" show-password></el-input>
      </el-form-item>
      <el-form-item
        v-for="(node, index) in cluster.nodeList"
        :label="'Redis Node ' + index"
        :key="node.key"
        :prop="'nodeList.' + index + '.value'"
        :rules="rules.redisNode"
      >
        <el-input v-model="node.value">
          <el-button slot="append" @click.prevent="removeNode(node)" icon="el-icon-delete"></el-button>
        </el-input>
      </el-form-item>
      <el-form-item label="Environment" prop="installationEnvironment">
        <el-radio-group v-model="cluster.installationEnvironment">
          <el-radio label="DOCKER">Docker</el-radio>
          <el-radio label="MACHINE">Machine</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="Cluster Info" prop="clusterInfo">
        <el-input v-model="cluster.clusterInfo"></el-input>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button size="small" @click="addNode()">New Node</el-button>
      <el-button
        size="small"
        type="primary"
        @click="saveCluster('cluster')"
        v-if="clusterId == null || clusterId == ''"
      >Confirm</el-button>
      <el-button size="small" type="primary" @click="saveCluster('cluster')" v-else>Update</el-button>
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
            let result = response.data;
            if (result.code != 0) {
              let cluster = result.data;
              if (
                this.clusterId == cluster.clusterId &&
                value == cluster.clusterName
              ) {
                callback();
              }
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
            return callback(new Error("Connection refused."));
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
    saveCluster(cluster) {
      this.$refs[cluster].validate(valid => {
        if (valid) {
          let currentGroup = this.currentGroup;
          if (isEmpty(currentGroup) || isEmpty(currentGroup.groupId)) {
            return;
          }
          this.cluster.groupId = currentGroup.groupId;
          this.cluster.userId = store.getters.getUserId;
          let nodes = "";
          this.cluster.nodeList.forEach(node => {
            nodes += node.value + ",";
          });
          this.cluster.nodes = nodes;
          let installationEnvironment = this.cluster.installationEnvironment.toUpperCase();
          this.cluster.installationEnvironment = installationEnvironment;
          console.log(this.cluster);
          // axios
          let url = "";
          console.log(this.clusterId);
          if (isEmpty(this.clusterId)) {
            url = "/cluster/importCluster";
          } else {
            url = "/cluster/updateCluster";
          }
          console.log(url);
          API.post(
            url,
            this.cluster,
            response => {
              let result = response.data;
              if (result.code == 0) {
                this.$refs[cluster].resetFields();
                this.$router.push({
                  name: "dashboard",
                  params: {
                    groupId: currentGroup.groupId
                  }
                });
                this.$emit("closeDialog", false);
              } else {
                console.error(result.message);
              }
            },
            err => {
              console.error(err);
            }
          );
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
    },
    getClusterById(clusterId) {
      let url = "/cluster/getCluster/" + clusterId;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            let cluster = result.data;
            let nodeList = this.nodesToNodeList(cluster.nodes);
            cluster.nodeList = nodeList;
            this.cluster = cluster;
          } else {
            console.log("Get clsuter failed.");
          }
        },
        err => {
          console.log(err);
        }
      );
    }
  },
  computed: {
    // 监听group变化
    currentGroup() {
      return store.getters.getCurrentGroup;
    }
  },
  mounted() {
    if (isEmpty(this.clusterId)) {
      this.cluster = {
        nodeList: [{ value: "" }]
      };
    } else {
      this.getClusterById(this.clusterId);
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