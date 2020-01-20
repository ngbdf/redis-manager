<template>
  <div id="analysis" class="body-wrapper">
    <div class="header-wrapper">
      <div>{{ currentGroup.groupName }}</div>
    </div>
    <div>
      <el-table :data="analyseResults" :default-sort="{prop: 'scheduleId', order: 'descending'}">
        <el-table-column type="index" width="50"></el-table-column>
        <el-table-column label="Cluster Name" property="clusterName"></el-table-column>
        <el-table-column label="Analyse Time" sortable property="scheduleId" :formatter="dateFormatter"></el-table-column>
        <el-table-column label="Status">
          <template slot-scope="scope">
            <el-button type="warning" icon="el-icon-loading" circle v-if="!scope.row.result" size="small" @click="toTaskProgress(scope.row)"></el-button>
            <el-button type="success" icon="el-icon-circle-check" circle v-else size="small" @click="toReportDetail(scope.row)"></el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import { store } from '@/vuex/store.js'
import { getAnalyzeResults } from '@/api/rctapi.js'
import { formatTime } from '@/utils/time.js'
export default {
  data () {
    return {
      analyseResults: []
    }
  },
  methods: {
    dateFormatter (row) {
      return formatTime(row.scheduleId)
    },
    async getAnalyseResults (groupId) {
      const res = await getAnalyzeResults()
      // 按照分析时间排序
      this.analyseResults = res.data
    },
    // getAnalyseResults (groupId) {
    //   const res = getAnalyzeResults()
    //   // 按照分析时间排序
    //   console.log('res.data.data', res)
    //   this.analyseResults = res.data.data
    // },
    toTaskProgress (row) {
      this.$router.push({
        name: 'TaskProgress',
        params: { clusterId: row.clusterId }
      })
    },
    toReportDetail (row) {
      this.$router.push({
        name: 'jobResultDetail',
        // path: '/plugin/jobResultDetail',
        query: {
          clusterId: row.clusterId,
          detailId: row.id
        }
      })
    }
  },
  computed: {
    currentGroup () {
      return store.getters.getCurrentGroup
    }
  },
  watch: {
    currentGroup (group) {
      this.getAnalyseResults(group.groupId)
    }
  },
  mounted () {
    let groupId = this.currentGroup.groupId
    this.getAnalyseResults(groupId)
  }
}
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
