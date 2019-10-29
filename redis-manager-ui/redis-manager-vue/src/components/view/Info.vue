<template>
  <div class="info-wrapper">
    <el-input v-model="search" size="mini" placeholder="search" style="margin-bottom: 20px;" />
    <div class="info-item" v-for="infoItem in filterInfoList" :key="infoItem.key">
      <div class="description" v-if="infoItem.description != null"># {{ infoItem.description }}</div>
      <span>
        <span class="key">{{ infoItem.key }}:</span>
        <span class="value">{{ infoItem.value }}</span>
      </span>
    </div>
  </div>
</template>

<script>
import API from "@/api/api.js";
import { isEmpty } from "@/utils/validate.js";
import message from "@/utils/message.js";
export default {
  props: {
    redisNode: {}
  },
  data() {
    return {
      infoList: [],
      search: ""
    };
  },
  methods: {
    getNodeInfo() {
      let url = "/nodeManage/getNodeInfo";
      API.post(
        url,
        this.redisNode,
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.infoList = result.data;
          } else {
            message.error("Get node info failed");
          }
        },
        err => {
          message.error(err);
        }
      );
    }
  },
  computed: {
    filterInfoList() {
      return this.infoList.filter(
        item =>
          !this.search ||
          item.key.toLowerCase().includes(this.search.toLowerCase())
      );
    }
  },
  mounted() {
    this.getNodeInfo();
  }
};
</script>

<style scoped>
.info-item {
  font-family: Consolas, Monaco, Menlo, "Courier New", monospace !important;
  margin: 0;
  word-wrap: break-word;
  word-break: break-all;
  white-space: pre-wrap;
  margin-bottom: 5px;
}
.description {
  color: #c0c4cc;
  font-size: 13px;
}

.key {
  color: #303133;
  font-size: 13px;
}
.value {
  color: #409eff;
  font: bold;
  font-size: 13px;
}
</style>