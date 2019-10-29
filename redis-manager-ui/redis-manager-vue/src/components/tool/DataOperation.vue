<template>
  <div id="data-operation" class="body-wrapper">
    <el-row>
      <el-col :xl="24" :lg="24" :md="24" :sm="24" class="select-wrapper">
        <el-select
          size="small"
          v-model="selectedClusterId"
          placeholder="Select cluster"
          @change="getDBList(selectedClusterId)"
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
          v-model="database"
          placeholder="Select db"
          filterable
          allow-create
          @change="handleSelectDB(database)"
        >
          <el-option v-for="database in dbList" :key="database" :label="database" :value="database"></el-option>
        </el-select>
      </el-col>
      <el-col :xl="24" :lg="24" :md="24" :sm="24">
        <div class="console-wrapper">
          <div class="console-title">Redis Command</div>
          <div class="console">
            <div class="connected" v-if="connected">Redis connected...</div>
            <div class="command-history-wrapper">
              <div class="history" v-html="histrory"></div>
              <!-- <codemirror class="result-wrapper" v-model="histrory" :options="codemirrorOptions"></codemirror> -->
            </div>
            <div class="command">
              <div style="max-width: 50%; margin-right: 5px;"></div>
              <el-input
                size="small"
                type="text"
                class="command-input"
                v-model="command"
                @keyup.enter.native="keyUpEnter(command)"
                :disabled="consoleDisabled"
              >
                <template slot="prepend">[{{ selectedCluster.clusterName }}]$</template>
              </el-input>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { store } from "@/vuex/store.js";
import API from "@/api/api.js";
import { isEmpty } from "@/utils/validate.js";
import { codemirror } from "vue-codemirror-lite";
// require("codemirror/lib/codemirror.js");
// require("codemirror/lib/codemirror.css");
// require("codemirror/mode/javascript/javascript");
// require("codemirror/addon/hint/show-hint.js");
// require("codemirror/addon/hint/show-hint.css");
// require("codemirror/addon/hint/javascript-hint.js");
// require("codemirror/theme/seti.css");
import message from "@/utils/message.js";
export default {
  components: {
    codemirror
  },
  data() {
    return {
      histrory: "",
      clusterList: [],
      dbList: [],
      selectedClusterId: "",
      selectedCluster: {},
      database: "",
      command: "",
      connected: false,
      consoleDisabled: true,
      codemirrorOptions: {
        mode: "application/json",
        readOnly: "nocursor",
        autoRefresh: true,
        scrollbarStyle: "null",
        // https://codemirror.net/demo/theme.html#xq-light
        theme: "seti"
      }
    };
  },
  methods: {
    getClusterList(groupId) {
      let url = "/cluster/getClusterList/" + groupId;

      if (!isEmpty(groupId)) {
        API.get(
          url,
          null,
          response => {
            let result = response.data;
            if (result.code == 0) {
              this.clusterList = result.data;
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
    resetStatus() {
      this.consoleDisabled = true;
      this.connected = false;
      this.dbList = [];
      this.database = "";
      this.histrory = "";
    },
    getDBList(clusterId) {
      if (isEmpty(clusterId)) {
        message.warning("Please select cluster");
        return;
      }
      this.clusterList.forEach(cluster => {
        if (cluster.clusterId == clusterId) {
          this.selectedCluster = cluster;
        }
      });
      this.resetStatus();
      if (this.selectedCluster.redisMode == "cluster") {
        this.dbList.push(0);
        return;
      }
      let url = "/data/getDBList/" + clusterId;
      API.get(
        url,
        null,
        response => {
          this.dbList = [];
          let result = response.data;
          if (result.code == 0) {
            let dbList = result.data;
            dbList.forEach(db => {
              let database = db.database;
              this.dbList.push(database.slice(2));
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
    handleSelectDB(database) {
      if (!isEmpty(database)) {
        this.consoleDisabled = false;
        this.connected = true;
      }
    },
    sendCommand(dataCommandsParam) {
      let url = "/data/sendCommand";
      API.post(
        url,
        dataCommandsParam,
        response => {
          let result = response.data;
          if (result.code == 0) {
            let data = result.data;
            if (typeof data != "string") {
              data = JSON.stringify(data);
            }
            this.histrory +=
              "[" +
              this.selectedCluster.clusterName +
              "]$ " +
              this.command +
              "<br/>" +
              data +
              "<br/>";
          } else {
            this.histrory +=
              "[" +
              this.selectedCluster.clusterName +
              "]$ " +
              this.command +
              "\n operation failed or command not support \n";
          }
          this.command = "";
        },
        err => {
          message.error(err);
        }
      );
    },
    keyUpEnter() {
      if (isEmpty(this.command)) {
        return;
      }
      if (this.command == "clear") {
        this.histrory = "";
        this.command = "";
        return;
      }
      if (isEmpty(this.selectedClusterId)) {
        message.warning("Please select cluster");
        return;
      }
      if (isEmpty(this.database)) {
        message.warning("Please select database");
        return;
      }
      let dataCommandsParam = {
        clusterId: this.selectedClusterId,
        database: this.database,
        command: this.command
      };
      this.sendCommand(dataCommandsParam);
    }
  },
  computed: {
    currentGroup() {
      return store.getters.getCurrentGroup;
    }
  },
  mounted() {
    this.getClusterList(this.currentGroup.groupId);
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
  padding: 10px 0;
  background-color: black;
  color: #ffffff;

  font-family: Consolas, Monaco, Menlo, "Courier New", monospace !important;
  margin: 0;
}
.console-title {
  padding: 10px 20px;
  border-bottom: 1px solid #dcdfe6;
  background: #f0f2f5;
}
.command-history-wrapper {
  word-break: break-all;
  word-wrap: break-word;
}
.connected {
  margin-bottom: 10px;
}

.command-input >>> .el-input__inner,
.command-input >>> .el-input-group__prepend {
  font-family: Consolas, Monaco, Menlo, "Courier New", monospace !important;
  border: none !important;
  font-size: 14px;
  background-color: #000000;
  color: #ffffff;
  padding: 0;
}

.command-input >>> .el-input-group__prepend {
  padding-right: 5px;
}

.command-input >>> .el-input__inner:focus {
  outline: none;
}
.command-input >>> .el-input__inner ::-webkit-input-placeholder {
  caret-color: red;
}

.history {
  width: auto;
  line-height: 20px;
  word-break: normal;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: Consolas, Monaco, Menlo, "Courier New", monospace !important;
  line-height: 24px;
}

.result-wrapper {
  height: auto;
  padding: 0;
  line-height: 20px;
  word-break: normal;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.cm-s-seti.CodeMirror {
  background: #000000 !important;
}
.cm-s-seti .CodeMirror-activeline-background {
  background: #000000;
}
.cm-s-seti .CodeMirror-selected {
  background: #d7d4f0;
}
.cm-s-seti .CodeMirror-linenumber {
  color: #aeaeae;
} /*行号数字*/
.cm-s-seti .cm-quote {
  color: #090;
} /*引号*/
.cm-s-seti .cm-keyword {
  color: #3300cc;
} /*关键字，例如：SELECT*/
.cm-s-seti .cm-number {
  color: #333333;
} /*数字*/
.cm-s-seti .cm-variable-2 {
  color: #333333;
} /*变量2，例如：a.id中的.id*/
.cm-s-seti .cm-comment {
  color: #009933;
} /*注释*/
.cm-s-seti .cm-string {
  color: #009933;
} /*字符串*/
.cm-s-seti .cm-string-2 {
  color: #009933;
} /*字符串*/
</style>