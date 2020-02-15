<template>
  <div class="info-wrapper">
    <div class="info-item" v-for="infoItem in infoList" :key="infoItem.key">
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
    sentinelMaster: {}
  },
  data() {
    return {
      infoList: [],
      search: ""
    };
  },
  methods: {
    getSentinelMasterInfo() {
      let url = "/sentinel/getSentinelMasterSlaves";
      API.post(
        url,
        this.sentinelMaster,
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.infoList = result.data;
          } else {
            message.error("Get sentinel master slaves failed");
          }
        },
        err => {
          message.error(err);
        }
      );
    }
  },
  mounted() {
    this.getSentinelMasterInfo();
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