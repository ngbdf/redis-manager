<template>
  <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
    <el-card shadow="hover" class="box-card">
      <div>
        <span style="position: 'relative';textAlign: 'center';display: 'block';fontSize: '20px'">Top 1000 Largest Keys By Perfix</span>
      </div>
      <el-table :data="pageData" @sort-change='sortChange'>
        <el-table-column label="PrefixKey" property="prefixKey"></el-table-column>
        <el-table-column label="Count" sortable property="keyCount" :formatter="formatterCount"></el-table-column>
        <el-table-column label="Memory Size" sortable property="memorySize" :formatter="formatMemory"></el-table-column>
      </el-table>
      <div>
        <el-pagination
          background
          @current-change="handleCurrentChange"
          :current-page.sync="currentPage"
          :page-size="pagesize"
          layout="prev, pager, next, jumper"
          :total="tableData.length"
        >
        </el-pagination>
      </div>
    </el-card>
  </el-col>
</template>
<script>
import { getTop1000KeysByPrefix } from '@/api/rctapi.js'
import { formatBytes, formatterInput } from '@/utils/format.js'
export default {
  data () {
    return {
      tableData: [],
      currentPage: 1,
      pagesize: 10,
      pageData: []
    }
  },
  methods: {
    async initTable () {
      let res = await getTop1000KeysByPrefix(2, 1579484079247)
      this.tableData = res.data.map(value => {
        return {
          keyCount: parseInt(value.keyCount),
          memorySize: parseInt(value.memorySize),
          prefixKey: value.prefixKey
        }
      })
      this.pageData = this.tableData.slice((this.currentPage - 1) * this.pagesize, this.currentPage * this.pagesize)
    },
    sortCountData (value1, values2) {
      if (value1 - values2 > 0) {
        return 1
      }
      if (value1 - values2 < 0) {
        return -1
      }
      return 0
    },
    sortMemoryData (value) {
      console.log('sortMemoryData', value)
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
      console.log(column)
      // 操作表格数据
      if (!column.order) {
        console.log('nullllll', column.order)
        return
      }
      this.tableData = this.tableData.sort(this.compareValue(column.prop, column.order))
      console.log('tableData', this.tableData)
      console.log('tableData slice', this.tableData.slice(0, 10))
      this.pageData = this.tableData.slice((this.currentPage - 1) * this.pagesize, this.currentPage * this.pagesize)
      this.currentPage = this.currentPage
    },
    handleCurrentChange (val) {
      this.currentPage = val
    },
    formatterCount (row, column, cellValue) {
      return formatterInput(cellValue)
    },
    formatMemory (row, column, cellValue) {
      return formatBytes(cellValue)
    }
  },
  mounted () {
    this.initTable()
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
