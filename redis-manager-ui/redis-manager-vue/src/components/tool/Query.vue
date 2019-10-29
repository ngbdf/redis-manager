<template>
  <div id="query" class="query-wrapper" v-loading="queryLoading">
    <div class="input-wrapper">
      <el-input
        size="small"
        placeholder="Redis Key"
        v-model.trim="autoCommandParam.key"
        class="input-with-select"
        clearable
      >
        <el-select
          v-model="autoCommandParam.database"
          slot="prepend"
          placeholder="Select DB"
          style="width: 50px;"
        >
          <el-option v-for="db in dbList" :key="db.database" :label="db.label" :value="db.database">
            <span style="float: left">{{ db.label }}</span>
            <span style="float: right; color: #8492a6; font-size: 13px">{{ db.keys }}</span>
          </el-option>
        </el-select>
      </el-input>
      <el-button
        size="small"
        type="primary"
        icon="el-icon-search"
        @click="queryRedis(autoCommandParam)"
        style="margin-left: 20px;"
      >Query</el-button>
      <el-button size="small" icon="el-icon-search" @click="scanRedis(autoCommandParam)">Scan</el-button>
    </div>
    <codemirror class="result-wrapper" v-model="result" :options="codemirrorOptions"></codemirror>
  </div>
</template>

<script>
import API from "@/api/api.js";
import { isEmpty } from "@/utils/validate.js";
import { codemirror } from "vue-codemirror-lite";
require("codemirror/lib/codemirror.js");
require("codemirror/lib/codemirror.css");
require("codemirror/mode/javascript/javascript");
require("codemirror/addon/hint/show-hint.js");
require("codemirror/addon/hint/show-hint.css");
require("codemirror/addon/hint/javascript-hint.js");
require("codemirror/theme/xq-light.css");
import message from "@/utils/message.js";
export default {
  components: {
    codemirror
  },
  props: {
    clusterId: {
      type: Number
    }
  },
  data() {
    return {
      dbList: [],
      autoCommandParam: {
        clusterId: this.clusterId,
        count: 100
      },
      result: "",
      codemirrorOptions: {
        mode: "application/json",
        readOnly: true,
        autoRefresh: true,
        scrollbarStyle: "null",
        // https://codemirror.net/demo/theme.html#xq-light
        theme: "xq-light",
        lineNumbers: true
      },
      queryLoading: false
    };
  },
  methods: {
    getDBList(clusterId) {
      let url = "/data/getDBList/" + clusterId;
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
    queryRedis(autoCommandParam) {
      this.result = "";
      let url = "/data/query";
      if (isEmpty(autoCommandParam.database)) {
        message.waring("Please select database");
        return;
      }
      if (isEmpty(autoCommandParam.key)) {
        message.waring("Please enter redis key");
        return;
      }
      this.queryLoading = true;
      API.post(
        url,
        autoCommandParam,
        response => {
          let result = response.data;
          if (result.code == 0) {
            let autoCommandResult = result.data;
            let queryResult = {};
            queryResult.ttl = autoCommandResult.ttl;
            queryResult.type = autoCommandResult.type;
            queryResult.value = autoCommandResult.value;
            this.result = JSON.stringify(queryResult, null, 2);
          } else {
            message.error(result.message);
          }
          this.queryLoading = false;
        },
        err => {
          this.queryLoading = false;
          message.error(err);
        }
      );
    },
    scanRedis(autoCommandParam) {
      this.result = "";
      let url = "/data/scan";
      if (isEmpty(autoCommandParam.database)) {
        message.warning("Please select database");
        return;
      }
      this.queryLoading = true;
      API.post(
        url,
        autoCommandParam,
        response => {
          let result = response.data;
          if (result.code == 0) {
            let scanResult = result.data;
            scanResult.forEach(key => {
              this.result += key + "\n";
            });
          } else {
            message.error(result.message);
          }
          this.queryLoading = false;
        },
        err => {
          this.queryLoading = false;
          message.error(err);
        }
      );
    }
  },
  mounted() {
    this.getDBList(this.clusterId);
  }
};
</script>

<style scoped>
.query-wrapper {
  min-height: 500px;
}

.el-select {
  width: 150px !important;
}

.input-with-select .el-input-group__prepend {
  background-color: #fff;
}

.input-wrapper {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.tip-wrapper {
  margin-left: 5px;
  margin-bottom: 10px;
  color: #909399;
  font-size: 12px;
}

.result-wrapper {
  padding: 0;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  line-height: 20px;
  height: 460px;
}
.vue-codemirror-wrap >>> .CodeMirror {
  height: 460px;
}
</style>