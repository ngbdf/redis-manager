<template>
  <el-col class="chart-item">
    <el-card shadow="hover" class="box-card">
     <div class="text-div">
       <span class="text-span">Top 1000 Largest Keys By Type</span>
      </div>
      <el-tabs v-model="activeName" @tab-click="handleClick">
          <el-tab-pane label="String" name="String">
          <Top1000KeysByTypeTable tabKey="0" :resultId="resultId"></Top1000KeysByTypeTable>
        </el-tab-pane>
        <el-tab-pane label="Hash" name="Hash">
          <Top1000KeysByTypeTable tabKey="5" :resultId="resultId"></Top1000KeysByTypeTable>

        </el-tab-pane>
        <el-tab-pane label="List" name="List">
          <Top1000KeysByTypeTable tabKey="10" :resultId="resultId"></Top1000KeysByTypeTable>
        </el-tab-pane>
        <el-tab-pane label="Set" name="Set">
          <Top1000KeysByTypeTable tabKey="15" :resultId="resultId"></Top1000KeysByTypeTable>
        </el-tab-pane>

      </el-tabs>
    </el-card>
  </el-col>
</template>
<script>
import Top1000KeysByTypeTable from '@/components/rct/chart/Top1000KeysByTypeTable'
export default {
  props: {
    resultId: {
      type: String
    }
  },
  components: {
    Top1000KeysByTypeTable
  },
  data () {
    return {
      activeName: 'String'
    }
  },
  methods: {
    handleClick (tab, event) {
    }
  },
  mounted () {
  }
}
</script>
<style scoped>
.box-card {
  margin: 5px;
}

.chart {
  min-height: 400px;
  width: 100%;
}

.chart-no-data {
  height: 0 !important;
}

.text-span{
    font-size: 18px;
}

.text-div{
    text-align: center;
}
</style>
