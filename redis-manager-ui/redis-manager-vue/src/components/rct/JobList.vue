<template>
  <div id="jobList" class="body-wrapper">
    <div class="header-wrapper">
      <div>{{ currentGroup.groupName }}</div>
    </div>

      <el-table :data="pageData" @sort-change="sortChange" @row-click="handdleRowClick" highlight-current-row >
        <el-table-column type="index" ></el-table-column>
        <el-table-column label="Cluster Name" property="clusterName" ></el-table-column>
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
        <el-table-column label="Config">
          <template slot-scope="scope">
            <el-button
                type="primary"
                size="mini"
                icon="el-icon-search" circle plain
                @click.stop="getConfig(scope.$index, scope.row)"
              ></el-button>
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
        <el-dialog
          title="Config Detail"
          :visible.sync="ConfigDetailVisible"
          :close-on-click-modal="false"
          @close="closeHandler()">
          <el-form :model="ConfigDetail" ref="ConfigDetail" label-width="120px" size="small">
            <el-form-item label="Cluster Name " prop="clusterName">
              <el-input readonly  v-model="clusterName" maxlength="50" show-word-limit></el-input>
            </el-form-item>
            <el-form-item label="Nodes "  prop="nodes">
              <el-input readonly autoSize type ="textarea" v-model="nodes"  show-word-limit></el-input>
            </el-form-item>
            <el-form-item label="Auto Analyze " prop="autoAnalyze">
              <el-switch  disabled v-model="ConfigDetail.autoAnalyze"></el-switch>
            </el-form-item>
            <el-form-item label="Schedule " prop="schedule" >
              <el-input readonly v-model="ConfigDetail.schedule" maxlength="50" show-word-limit></el-input>
            </el-form-item>
            <el-form-item label="DataPath " prop="dataPath">
              <el-input readonly v-model="ConfigDetail.dataPath" maxlength="50" show-word-limit></el-input>
            </el-form-item>
            <el-form-item label="Custom Prefixes " prop="prefixes">
              <el-input readonly  v-model="ConfigDetail.prefixes"  show-word-limit></el-input>
            </el-form-item>
            <el-form-item label="Report " prop="report">
              <el-switch  disabled v-model="ConfigDetail.report"></el-switch>
            </el-form-item>
            <el-form-item label="Mail " prop="mailTo">
              <el-input readonly v-model="ConfigDetail.mailTo" maxlength="50" show-word-limit></el-input>
            </el-form-item>
          </el-form>
        </el-dialog>
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
      pageData: [],
      ConfigDetailVisible: false,
      ConfigDetail:{
      },
      nodes:'',
      clusterName:''
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
      const res = await getAnalyzeResults(groupId)
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
    },
    closeHandler() {
      this.ConfigDetail = {};
      this.clusterName = '';
      this.nodes = '';
    },
    getConfig(index, row) {
      this.clusterName =row.clusterName;
      this.ConfigDetail = JSON.parse(row.analyzeConfig);;
      this.ConfigDetailVisible = true;
      this.setNodes(this.ConfigDetail.nodes); 
    },
    setNodes(value) {
      var temp = "";
      for(let i in value){
        if(value[0]==='-1'){
          temp = "All Cluster Nodes";
        }else{
          temp = temp + value[i]+" ,";
          temp = temp.substring(0, temp.lastIndexOf(','));
        }
      }
      this.nodes = temp;
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
.el-table .el-table__body tr:hover td {
    cursor: pointer  !important;
}
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
/* 用来设置当前页面element全局table 选中某行时的背景色*/
.el-table__body tr.current-row>td{
  background-color: #f19944 !important;
  /* color: #f19944; */  /* 设置文字颜色，可以选择不设置 */
}
/* 用来设置当前页面element全局table 鼠标移入某行时的背景色*/
.el-table--enable-row-hover .el-table__body tr:hover>td {
  background-color: #f19944;
  /* color: #f19944; */ /* 设置文字颜色，可以选择不设置 */
}

</style>
