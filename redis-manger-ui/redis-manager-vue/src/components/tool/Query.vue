<template>
  <div id="query" class="query-wrapper">
    <div class="input-wrapper">
      <el-input size="small" placeholder="Redis Key" v-model="key" class="input-with-select">
        <el-select v-model="select" slot="prepend" placeholder="SELECT DB">
          <el-option label="db0" value="1"></el-option>
          <el-option label="db1" value="2"></el-option>
          <el-option label="db1" value="3"></el-option>
        </el-select>
      </el-input>
      <el-button
        size="small"
        type="primary"
        icon="el-icon-search"
        @click="queryRedis()"
        style="margin-left: 20px;"
      >Query</el-button>
      <el-button size="small" icon="el-icon-search">Scan</el-button>
    </div>
    <div class="tip-wrapper">
      <span>Limit 100</span>
    </div>
    
    <div class="result-wrapper"></div>
  </div>
</template>

<script>
export default {
  props: {
    clusterId: {
      type: Number
    }
  },
  data() {
    return { key: "", select: "" };
  },
  methods: {
    queryRedis() {}
  },
  mounted() {
    console.log(this.clusterId);
  }
};
</script>

<style scoped>
.query-wrapper {
  height: 600px;
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
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  height: 80%;
}
</style>