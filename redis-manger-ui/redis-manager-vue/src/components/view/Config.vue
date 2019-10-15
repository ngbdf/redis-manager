<template>
  <div class="config-wrapper">
    <div class="config-item" v-for="configItem in configList" :key="configItem.key">
      <div class="description" v-if="configItem.description != null && configItem.description != ''"># {{ configItem.description }}</div>
      <span>
        <span class="key">{{ configItem.key }}:</span>
        <span class="value">{{ configItem.value }}</span>
      </span>
    </div>
  </div>
</template>

<script>
import API from "@/api/api.js";
export default {
  props: {
    redisNode: {}
  },
  data() {
    return {
      configList: []
    };
  },
  methods: {
    getConfig() {
      let url = "/nodeManage/getConfig";
      API.post(
        url,
        this.redisNode,
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.configList = result.data;
          } else {
            console.log("get node config failed.");
          }
        },
        err => {
          console.log(err);
        }
      );
    }
  },
  mounted() {
    this.getConfig();
  }
};
</script>

<style scoped>
.config-item {
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