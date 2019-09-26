<template>
  <div id="query" class="query-wrapper">
    <div class="input-wrapper">
      <el-input
        size="small"
        placeholder="Redis Key"
        v-model="autoCommandParam.key"
        class="input-with-select"
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
      <el-button size="small" icon="el-icon-search">Scan</el-button>
    </div>
    <div class="tip-wrapper">
      <span>Limit 100</span>
    </div>

    <codemirror class="result-wrapper" v-model="result" :options="codemirrorOptions"></codemirror>
  </div>
</template>

<script>
import API from "@/api/api.js";
import { codemirror } from "vue-codemirror-lite";
require("codemirror/lib/codemirror.js");
require("codemirror/lib/codemirror.css");
require("codemirror/mode/javascript/javascript");
require("codemirror/addon/hint/show-hint.js");
require("codemirror/addon/hint/show-hint.css");
require("codemirror/addon/hint/javascript-hint.js");
require("codemirror/theme/xq-light.css");
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
        clusterId: this.clusterId
      },
      autoCommandResult: {
        ttl: -2,
        type: "none",
        cursor: null,
        completeIteration: false,
        value: null
      },
      result: "",
      codemirrorOptions: {
        mode: "application/json",
        readOnly: "nocursor",
        autoRefresh: true,
        // https://codemirror.net/demo/theme.html#xq-light
        theme: "xq-light"
      }
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
            result.data.forEach(db => {
              let database = db.database;
              this.dbList.push({
                label: database,
                keys: db.keys,
                database: database.slice(2)
              });
            });
          } else {
            console.log(result.message);
          }
        },
        err => {
          console.log(err);
        }
      );
    },
    queryRedis(autoCommandParam) {
      let url = "/data/query";
      API.post(
        url,
        autoCommandParam,
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.autoCommandResult = result.data;
            console.log(this.autoCommandResult);
            this.result = JSON.stringify(this.autoCommandResult, null, 2);
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
  mounted() {
    this.getDBList(this.clusterId);
    console.log("==");
    this.result = JSON.stringify(this.autoCommandResult, null, 2);
  }
};
</script>

<style scoped>
.query-wrapper {
  height: 400px;
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
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  height: 85%;
}
</style>