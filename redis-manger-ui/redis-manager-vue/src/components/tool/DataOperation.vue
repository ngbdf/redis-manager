<template>
  <div id="data-operation" class="body-wrapper">
    <el-row>
      <el-col :xl="24" :lg="24" :md="24" :sm="24" class="select-wrapper">
        <el-select size="small" v-model="cluster" placeholder="Select cluster">
          <el-option
            v-for="item in clusterList"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          ></el-option>
        </el-select>
        <el-select size="small" v-model="db" placeholder="Select db">
          <el-option
            v-for="item in dbList"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          ></el-option>
        </el-select>
      </el-col>
      <el-col :xl="24" :lg="24" :md="24" :sm="24">
        <div class="console-wrapper">
          <div class="console-title">Redis Data Operation</div>
          <div class="console">
            <div class="connected">Redis connected</div>
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
                  v-model="currentCommnad"
                  @keyup.enter.native="keyUpEnter(currentCommnad)"
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
export default {
  data() {
    return {
      histrory: "",
      currentCommnad: "",
      clusterList: [
      ],
      cluster: "",
      dbList: [
        {
          value: "0",
          label: "db0"
        }
      ],
      db: ""
    };
  },
  methods: {
    keyUpEnter(command) {
      if (command == "") {
        return;
      }
      if (command == "clear") {
        this.histrory = "";
        this.currentCommnad = "";
        return;
      }
      console.log(command);
      this.histrory += command + "<br>" + "I'm result...<br>";
      this.currentCommnad = "";
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
              let clusterList = result.data;
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