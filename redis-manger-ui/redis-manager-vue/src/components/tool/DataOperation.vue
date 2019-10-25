<template>
  <div id="data-operation" class="body-wrapper">
    <el-row>
      <el-col :xl="24" :lg="24" :md="24" :sm="24" class="select-wrapper">
        <el-select
          size="small"
          v-model="cluster"
          placeholder="Select cluster"
          @change="getDBList(cluster.clusterId)"
        >
          <el-option
            v-for="cluster in clusterList"
            :key="cluster.clusterId"
            :label="cluster.clusterName"
            :value="cluster.clusterId"
          ></el-option>
        </el-select>
        <el-select
          size="small"
          v-model="dataCommandsParam.database"
          placeholder="Select db"
          :disabled="dbListDisabled"
        >
          <el-option v-for="db in dbList" :key="db.database" :label="db.label" :value="db.database"></el-option>
        </el-select>
      </el-col>
      <el-col :xl="24" :lg="24" :md="24" :sm="24">
        <div class="console-wrapper">
          <div class="console-title">Redis Command</div>
          <div class="console">
            <div class="connected" v-if="connected">Redis connected...</div>
            <div class="command-history-wrapper">
              <div class="history" v-html="histrory"></div>
              <div class="command">
                <span>
                  <b>></b>
                </span>
                <el-input
                  size="small"
                  type="text"
                  class="command-input"
                  v-model="dataCommandsParam.command"
                  @keyup.enter.native="keyUpEnter(dataCommandsParam)"
                  :disabled="consoleDisabled"
                />
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import message from "@/utils/message.js";
export default {
  data() {
    return {
      histrory: "",
      clusterList: [],
      cluster: "",
      dbList: [],
      connected: false,
      dataCommandsParam: {},
      dbListDisabled: true,
      consoleDisabled: true
    };
  },
  methods: {
    keyUpEnter(dataCommandsParam) {
      let command = dataCommandsParam.command;
      if (command == "") {
        return;
      }
      if (command == "clear") {
        this.histrory = "";
        this.dataCommandsParam.command = "";
        return;
      }
      this.histrory += command + "<br>" + "I'm result...<br>";
      this.sendCommand();
    },
    getClusterList(groupId) {
      let url = "/cluster/getClusterList/" + groupId;
      this.clusterListLoading = true;
      if (!isEmpty(groupId)) {
        API.get(
          url,
          null,
          response => {
            let result = response.data;
            if (result.code == 0) {
              this, (clusterList = result.data);
            } else {
              message.error("Get cluster list failed");
            }
          },
          err => {
            message.error(err);
          }
        );
      }
    },
    getDBList(clusterId) {
      let url = "/data/getDBList/" + clusterId;
      this.dbList = [];
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            let dbList = result.data;
            dbList.forEach(db => {
              let database = db.database;
              this.dbList.push({
                label: database,
                keys: db.keys,
                database: database.slice(2)
              });
            });
          } else {
            message.error(result.message);
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    sendCommand() {
      this.dataCommandsParam.clusterId = this.cluster.clusterId;
    }
  },
  computed: {
    currentGroup() {
      return store.getters.getCurrentGroup;
    }
  }
};
</script>

<style scoped>
.body-wrapper {
  padding: 20;
}

.select-wrapper {
  margin-bottom: 20px;
}

.console-wrapper {
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

.connected {
  margin-bottom: 10px;
}

.command {
  display: flex;
  align-items: center;
  margin-top: 10px;
}

.command-input >>> .el-input__inner {
  margin-left: 5px;
  font-family: Consolas, Monaco, Menlo, "Courier New", monospace !important;
  border: none !important;
  font-size: 14px;
  background-color: #000000;
  color: #ffffff;
  padding: 0;
}
.command-input >>> .el-input__inner:focus {
  outline: none;
}
.command-input >>> .el-input__inner ::-webkit-input-placeholder {
  caret-color: red;
}

.history {
  word-break: normal;
  width: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
  line-height: 24px;
}
</style>