<template>
  <div>
      <el-table :data="pageData" @sort-change='sortChange'>
        <el-table-column label="PrefixKey" property="key"></el-table-column>
        <el-table-column label="itemCount" sortable property="itemCount" :formatter="formatterCount"></el-table-column>
        <el-table-column label="Bytes" sortable property="bytes" :formatter="formatMemory"></el-table-column>
      </el-table>
      <div>
        <el-pagination
          background
          @current-change="handleCurrentChange"
          :current-page.sync="currentPage"
          :page-size="pagesize"
          layout="prev, pager, next, jumper"
          :total="tableData.length"
          class="pagination"
        >
        </el-pagination>
      </div>
  </div>
</template>
<script>
import { getTop1000KeysByType } from '@/api/rctapi.js'
import { formatBytes, formatterInput } from '@/utils/format.js'
export default {
  props: {
    tabKey: {
      type: String
    },
    resultId: {
      type: String
    }
  },
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
      let res = await getTop1000KeysByType(this.resultId, this.tabKey)
      this.tableData = res.data.map(value => {
        return {
          bytes: parseInt(value.bytes),
          itemCount: parseInt(value.itemCount),
          key: value.key
        }
      })
      this.pageData = this.tableData.slice((this.currentPage - 1) * this.pagesize, this.currentPage * this.pagesize)
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
        return
      }
      this.tableData = this.tableData.sort(this.compareValue(column.prop, column.order))
      this.pageData = this.tableData.slice((this.currentPage - 1) * this.pagesize, this.currentPage * this.pagesize)
      this.currentPage = this.currentPage
    },
    handleCurrentChange (val) {
      this.currentPage = val
      this.pageData = this.tableData.slice((this.currentPage - 1) * this.pagesize, this.currentPage * this.pagesize)
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
  },
  watch: {
    // 深度监听 schedule 变化
    scheduleId: {
      handler: function () {
        this.initTable()
      },
      deep: true
    }
  }
}
</script>
<style scoped>
.pagination{
    float: right;
    margin-top: 20px;
}
</style>
