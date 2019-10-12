<template>
  <div class="info-wrapper">
    <div class="info-item" v-for="infoItem in infoList" :key="infoItem.key">
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
export default {
  props: {
    redisNode: {}
  },
  data() {
    return {
      infoList: []
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
            console.log("get node info failed.");
          }
        },
        err => {
          console.log(err);
        }
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
  line-height: 20px;
}
.description {
  color: #c0c4cc;
}

.key {
  color: #303133;
}
.value {
  color: #409eff;
  font: bold;
}
</style>