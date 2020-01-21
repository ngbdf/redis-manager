<template>
  <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
    <el-card shadow="hover" class="box-card">
      <div class="text-div">
        <span class="text-span">Top 1000 Largest Keys By Perfix</span>
      </div>
      <!-- <el-input v-model="search" size="mini" placeholder="search" /> -->
      <div class="input">
           <el-input placeholder="search" v-model="search" class="input-with-select" size="small">
           <el-button  slot="append" icon="el-icon-search"></el-button>
           </el-input>
      </div>

      <el-table :data="pageData.filter(data => !search || data.prefixKey.toLowerCase().includes(search.toLowerCase()))" @sort-change='sortChange'>
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
          class="pagination"
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
  props: {
    resultId: {
      type: String
    }
  },
  data () {
    return {
      tableData: [],
      currentPage: 1,
      pagesize: 10,
      pageData: [],
      search: ''
    }
  },
  methods: {
    async initTable () {
      let res = await getTop1000KeysByPrefix(this.resultId)
      this.tableData = res.data.map(value => {
        return {
          keyCount: parseInt(value.keyCount),
          memorySize: parseInt(value.memorySize),
          prefixKey: value.prefixKey
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
.box-card {
  margin: 5px;
  height: 700px;
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
.input{
    width: 40%;
    float: right;
    margin-top: 20px;
}
.pagination{
    float: right;
    margin-top: 20px;
}
</style>
