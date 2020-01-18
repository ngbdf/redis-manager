<template>
  <div id="taskProgress" class="body-wrapper">
    <div class="header-wrapper">
      <div>{{ currentGroup.groupName }}</div>
      <div><el-input size="mini" v-model="searchData" prefix-icon="el-icon-search" placeholder="input redis instance"></el-input></div>
      <div><el-button size="mini" :disabled="this.cancelButtonDisabled" type="success" @click="cancelAnalysis()">Cancel</el-button></div>
    </div>
    <div>
      <el-table :data="analyseisJobDetail">
        <el-table-column type="index" width="50"></el-table-column>
        <el-table-column label="Redis Instance" property="instance"></el-table-column>
        <el-table-column label="Status" property="status">
          <template slot-scope="scope">
            <font v-if="scope.row.status === 'RUNNING'" color="#5485F7">RUNNING</font>
            <font v-else-if="scope.row.status === 'DONE'" color="#8000ff">DONE</font>
            <font v-else-if="scope.row.status === 'READY'" color="#0000a0">READY</font>
            <font v-else-if="scope.row.status === 'CANCELED'" color="999999">CANCELED</font>
            <font v-else-if="scope.row.status === 'NOT_START'" color="#FA7070">NOT_START</font>
            <font v-else-if="scope.row.status === 'ERROR'" color="#ff0000">ERROR</font>
            <font v-else-if="scope.row.status === 'NOTINIT'" color="#FA7070">NOTINIT</font>
            <font v-else-if="scope.row.status === 'CHECKING'" color="#FA7070">CHECKING</font>
          </template>
        </el-table-column>
        <el-table-column label="Process" property="process">
          <template slot-scope="scope">
            <el-progress  v-if="scope.row.status === 'CANCELED'" :percentage="scope.row.process"></el-progress>
            <el-progress  v-else :percentage="scope.row.process"></el-progress>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
<script>
import { store } from "@/vuex/store.js";
import { isEmpty } from "@/utils/validate.js";
import { formatTime } from "@/utils/time.js";
import API from "@/api/api.js";
import message from "@/utils/message.js";

export default {
  data() {
    return {
      originalData: [],
      analyseisJobDetail: [],
      cancelButtonDisabled: false,
      searchData: "",
    };
  },
  methods: {
    getData1(clusterId) {
        this.originalData = [
            {
                instance: "6.6.6.6:9002",
                status: "RUNNING",
                process: Math.floor(Math.random() * 100)
            },
            {
                instance: "8.8.8.8:9002",
                status: "CANCELED",
                process: Math.floor(Math.random() * 100)
            } ,
            {
                instance: "9.9.9.9:9002",
                status: "READY",
                process: Math.floor(Math.random() * 100)
            }
        ]
        this.analyseisJobDetail = [
            {
                instance: "6.6.6.6:9002",
                status: "RUNNING",
                process: Math.floor(Math.random() * 100)
            },
            {
                instance: "8.8.8.8:9002",
                status: "CANCELED",
                process: Math.floor(Math.random() * 100)
            } ,
            {
                instance: "9.9.9.9:9002",
                status: "READY",
                process: Math.floor(Math.random() * 100)
            }
        ]
        if(this.searchData) {
              let list = this.originalData.filter((item, index) =>
                item.instance.includes(this.searchData)
              )
              this.analyseisJobDetail = list;
        }
        let count = 0;
        for(let i = 0; i < this.originalData.length; i++) {
          if(this.originalData[i].status === 'DONE') {
            count += 1;
          }
        }
        if (count === this.originalData.length) {
          this.cancelButtonDisabled = true
        } else {
          this.cancelButtonDisabled = false
        }

    },
    cancelAnalysis() {
      this.$confirm("Are you sure you want to stop all task processes ?", "Message", {
        confirmButtonText: "Yes",
        cancelButtonText: "No",
        type: "warning"
      })
        .then(() => {
          this.$message({
            type: "success",
            message: "Stop Success!"
          });
        })
        .catch(() => {
          this.$message({
            type: "info",
            message: "Cancel"
          });
        });
    },
  },
  computed: {
    currentGroup() {
      let clusterId = this.$route.params.clusterId;
      this.timer = setInterval(() => {
        this.getData1(clusterId);
      }, 3000);
      return store.getters.getCurrentGroup;
    }
  },

  watch: {
    searchData(val) {
        let list = this.originalData.filter((item, index) =>
            item.instance.includes(val)
        )
        this.analyseisJobDetail = list
    }
  },

  mounted() {
    let clusterId = this.$route.params.clusterId;
    this.getData1(clusterId);
  }
};
</script>

<style scoped>
.body-wrapper {
  min-width: 1000px;
}
.header-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 1px solid #dcdfe6;
}
</style>