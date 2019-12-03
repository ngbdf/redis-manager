<template>
  <div id="rule-manage" class="body-wrapper" style="display: none;" :style="{display: 'block'}">
    <!-- <div class="header-wrapper">
      <div>Bigdata</div>
    </div> -->
    <div>
      <el-table :data="historyList" stripe style="width: 100%">
        <el-table-column type="index" :index="getIndex" label="ID" min-width="15%"></el-table-column>
        <el-table-column prop="groupName" label="GroupName" min-width="15%"></el-table-column>
        <el-table-column prop="userName" label="UserName" min-width="15%"></el-table-column>
        <el-table-column prop="userIp" label="UserIP" min-width="15%"></el-table-column>
        <el-table-column prop="operationInfo" label="OperationInfo" min-width="20%"></el-table-column>
        <el-table-column prop="logTime" label="LogTime" min-width="20%"></el-table-column>
      </el-table>
    </div>
    <div class="block">
      <el-pagination
        v-if="paginationShow"
        @current-change="handleCurrentChange"
        :current-page.sync="currentPageNo"
        :page-size="pageSize"
        layout="prev, pager, next, jumper"
        :total="totalCount"
        style="text-align: center">
      </el-pagination>
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
      historyList: [],
      currentPageNo: 1,
      pageSize: 10,
      totalPage: 0,
      paginationShow: false,
      totalCount: 0
    };
  },
  methods: {
    loadHistoryData(groupId) {
      let url = "/operation/group/" + groupId + "?pageNo=" + this.currentPageNo;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            let historyList = result.data.logData;
            historyList.forEach(history => {
              history.logTime = formatTime(history.logTime);
            });
            this.historyList = historyList;
            this.totalCount = result.data.totalCount;
            this.totalPage = result.data.totalPage;
            if (this.totalPage > 1) {
              this.paginationShow = true;
            }
          } else {
            message.error("Get edit history list failed");
          }
        },
        err => {
          if (err.response.status === 401) {
            message.error("Session Timeout! Please Login again");
            this.$router.push({name: "login"})
          } else{
            message.error(err);
          }
        }
      );
    },
    //点击下一页和点击页码时执行
    handleCurrentChange(pageNo) {
      this.currentPageNo = pageNo;
      let groupId = this.currentGroupId;
      this.loadHistoryData(groupId);
    },
    getIndex(val) {
      return this.indexBasic + val + 1;
    },
  },
  computed: {
    currentGroupId() {
      return store.getters.getCurrentGroup.groupId;
    },
    indexBasic() {
        return (this.currentPageNo - 1) * this.pageSize;
    }
  },
  watch: {
    currentGroupId(groupId) {
      this.currentPageNo = 1;
      this.pageSize = 10;
      this.totalPage = 0;
      this.paginationShow = false;
      this.totalCount = 0;
      this.loadHistoryData(groupId);
    }
  },
  mounted() {
    let groupId = this.currentGroupId;
    this.loadHistoryData(groupId);
  }
};
</script>

<style scoped>

</style>