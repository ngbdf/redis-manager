<template>
  <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
    <el-card shadow="hover" class="box-card">
      <div class="text-div" v-show="this.tableObj.title">
        <span class="text-span">{{this.tableObj.title}}</span>
      </div>

      <div class="input" v-show="this.tableObj.searchVis">
           <!-- <el-input placeholder="search" v-model="search" class="input-with-select" size="small">
           <el-button  slot="append" icon="el-icon-search"></el-button>
           </el-input> -->
            <el-input v-model="search" size="small" placeholder="search" @input="searchFilter"/>
      </div>

      <el-table :data="pageData" @sort-change='sortChange'>
        <el-table-column v-for="column in this.tableObj.columns" :key="column.label" :label="column.label" :sortable="column.sort" :formatter="column.formatter"  :prop="column.prop" ></el-table-column>
        <!-- <el-table-column label="PrefixKey" property="prefixKey"></el-table-column>
        <el-table-column label="Count" sortable property="keyCount" :formatter="formatterCount"></el-table-column>
        <el-table-column label="Memory Size" sortable property="memorySize" :formatter="formatMemory"></el-table-column> -->
      </el-table>
      <div>
        <el-pagination
          background
          @current-change="handleCurrentChange"
          :current-page.sync="currentPage"
          :page-size="pagesize"
          layout="prev, pager, next, jumper"
          :total="length"
          class="pagination"
        >
        </el-pagination>
      </div>
    </el-card>
  </el-col>
</template>
<script>
import { formatBytes, formatterInput } from '@/utils/format.js'
export default {
  props: {
    tableObj: {
      type: Object
    },
    initData: {
      type: Function
    }
  },
  data () {
    return {
      tableData: [],
      currentPage: 1,
      pagesize: 10,
      pageData: [],
      search: '',
      prefix: this.tableObj.searchColumn,
      length: 0
    }
  },
  methods: {
    async initTable () {
      await this.initData().then(response => {
        // this.tableData = data
        this.tableData = response.data.map(value => {
          this.tableObj.columns.forEach(colum => {
            if (colum.sort) {
              value[colum.prop] = parseInt(value[colum.prop])
            }
          })
          return value
        })
      })
      this.length = this.tableData.length
      this.pageData = this.tableData.slice((this.currentPage - 1) * this.pagesize, this.currentPage * this.pagesize)
    },
    searchFilter (value) {   
        const data = this.tableData.filter(data => !value || data[`${this.prefix}`].toLowerCase().includes(value.toLowerCase()))
        this.length = data.length
        this.pageData = data.slice((this.currentPage - 1) * this.pagesize, this.currentPage * this.pagesize)
    },
    compareValue (property, order) {
      return function (obj1, obj2) {
        if (order === 'ascending') {
          return parseInt(obj1[property]) - parseInt(obj2[property])
        }
        return parseInt(obj2[property]) - parseInt(obj1[property])
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
    },
    tableObj: {
      immediate: true,
      handler (newValue, old) {
        this.initTable(newValue)
      }
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
