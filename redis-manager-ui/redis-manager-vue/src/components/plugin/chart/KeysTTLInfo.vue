<template>
  <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
    <el-card shadow="hover" class="box-card">
      <div>
        <span style="position: 'relative';textAlign: 'center';display: 'block';fontSize: '20px'">Keys TTL Info</span>
      </div>
      <el-input v-model="search" size="mini" placeholder="search" />
      <el-table :data="pageData.filter(data => !search || data.prefix.toLowerCase().includes(search.toLowerCase()))" @sort-change='sortChange'>
        <el-table-column label="Prefix" property="prefix"></el-table-column>
        <el-table-column label="TTL" sortable property="TTL" :formatter="formatterCount"></el-table-column>
        <el-table-column label="noTTL" sortable property="noTTL" :formatter="formatterCount"></el-table-column>
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
import { getKeysTTLInfo } from '@/api/rctapi.js'
import { formatterInput } from '@/utils/format.js'
export default {
  props: {
    clusterId: {
      type: String
    },
    scheduleId: {
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
      let res = await getKeysTTLInfo(26)
      this.tableData = res.data.map(value => {
        return {
          noTTL: parseInt(value.noTTL),
          TTL: parseInt(value.TTL),
          prefix: value.prefix
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
</style>
