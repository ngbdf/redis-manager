<template>
  <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
    <el-card shadow="hover" class="box-card">
      <div>
        <span style="position: 'relative';textAlign: 'center';display: 'block';fontSize: '20px'">Top 1000 Largest Keys By Type</span>
      </div>
      <el-tabs v-model="activeName" @tab-click="handleClick">
        <el-tab-pane label="Hash" name="Hash">
          <Top1000KeysByTypeTable tabKey="5" :clusterId="clusterId" :scheduleId="scheduleId"></Top1000KeysByTypeTable>
        </el-tab-pane>
        <el-tab-pane label="List" name="List">
          <Top1000KeysByTypeTable tabKey="10" :clusterId="clusterId" :scheduleId="scheduleId"></Top1000KeysByTypeTable>
        </el-tab-pane>
        <el-tab-pane label="Set" name="Set">
          <Top1000KeysByTypeTable tabKey="15" :clusterId="clusterId" :scheduleId="scheduleId"></Top1000KeysByTypeTable>
        </el-tab-pane>
        <el-tab-pane label="String" name="String">
          <Top1000KeysByTypeTable tabKey="0" :clusterId="clusterId" :scheduleId="scheduleId"></Top1000KeysByTypeTable>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </el-col>
</template>
<script>
import Top1000KeysByTypeTable from '@/components/plugin/chart/Top1000KeysByTypeTable'
export default {
  props: {
    clusterId: {
      type: String
    },
    scheduleId: {
      type: String
    }
  },
  components: {
    Top1000KeysByTypeTable
  },
  data () {
    return {
      activeName: 'Hash'
    }
  },
  methods: {
    handleClick (tab, event) {
      console.log(tab, event)
    }
  },
  mounted () {
  }
}
</script>
<style scoped>
.box-card {
  margin: 5px;
  height: 900px;
}

.chart {
  min-height: 400px;
  width: 100%;
}

.chart-no-data {
  height: 0 !important;
}
</style>
