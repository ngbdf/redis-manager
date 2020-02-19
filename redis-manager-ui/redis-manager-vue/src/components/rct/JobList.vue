<template>
  <div id="jobList" class="body-wrapper">
    <div class="header-wrapper">
      <div>{{ currentGroup.groupName }}</div>
    </div>

      <el-table :data="pageData" @sort-change="sortChange" @row-click="handdleRowClick">
        <el-table-column type="index" width="50"></el-table-column>
        <el-table-column label="Cluster Name" property="clusterName"></el-table-column>
        <el-table-column
          label="Analyse Time"
          sortable
          property="scheduleId"
          :formatter="dateFormatter"
        ></el-table-column>
        <el-table-column label="Status">
          <template slot-scope="scope">
              <i v-if="!scope.row.result" class="el-icon-loading" style="font-size: 20px;color:orange"></i>
              <i v-else class="el-icon-circle-check" style="font-size: 20px;color:green"></i>
            <!-- <el-button type="warning" icon="el-icon-loading" circle v-if="!scope.row.result" size="small" @click="toTaskProgress(scope.row)"></el-button>
            <el-button type="success" icon="el-icon-circle-check" circle v-else size="small" @click="toReportDetail(scope.row)"></el-button> -->
          </template>
        </el-table-column>

        <el-table-column
            label="IsCluster"
          >
            <template slot-scope="scope">
              <el-tag size="small" v-if="JSON.parse(scope.row.analyzeConfig).nodes[0]==='-1'" type="success"
                >Yes</el-tag
              >
              <el-tag size="small" v-else type="danger">No</el-tag>
            </template>
        </el-table-column>

      </el-table>
        <el-pagination
          background
          @current-change="handleCurrentChange"
          :current-page.sync="currentPage"
          :page-size="pagesize"
          layout="prev, pager, next, jumper"
          :total="analyseResults.length"
          class="pagination"
        ></el-pagination>
  </div>
</template>

<script>
import { store } from '@/vuex/store.js'
import { getAnalyzeResults } from '@/api/rctapi.js'
import { formatTime } from '@/utils/time.js'
export default {
  data () {
    return {
      loading: false,
      analyseResults: [],
      currentPage: 1,
      pagesize: 10,
      pageData: []
    }
  },
  methods: {
    dateFormatter (row) {
      return formatTime(row.scheduleId)
    },
    handdleRowClick (row, event, column) {
      if (!row.result) {
        this.toTaskProgress(row)
      } else {
        this.toReportDetail(row)
      }
    },
    handleCurrentChange (val) {
      this.currentPage = val
      this.pageData = this.analyseResults.slice(
        (this.currentPage - 1) * this.pagesize,
        this.currentPage * this.pagesize
      )
    },
    compareValue (property, order) {
      return function (obj1, obj2) {
        if (order === 'ascending') {
          return obj1[property] - obj2[property]
        }
        return obj2[property] - obj1[property]
      }
    },
    sortChange (column) {
      // 操作表格数据
      if (!column.order) {
        console.log('nullllll', column.order)
        return
      }
      this.analyseResults = this.analyseResults.sort(
        this.compareValue(column.prop, column.order)
      )
      this.pageData = this.analyseResults.slice(
        (this.currentPage - 1) * this.pagesize,
        this.currentPage * this.pagesize
      )
      this.currentPage = this.currentPage
    },
    async getAnalyseResults (groupId) {
      this.loading = true
      const res = await getAnalyzeResults()
      // 按照分析时间排序
      this.analyseResults = res.data
      this.pageData = res.data.slice(
        (this.currentPage - 1) * this.pagesize,
        this.currentPage * this.pagesize
      )
      this.loading = false
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
        params: { analyzeConfig: row.analyzeConfig },
        // path: '/plugin/jobResultDetail',
        query: {
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

.header-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 1px solid #dcdfe6;
}
.pagination {
  text-align:right;
  margin-top: 15px;
}
</style>
