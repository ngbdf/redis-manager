<template>
  <div id="jobList" class="body-wrapper">
    <div class="header-wrapper">
      <!-- <div>{{ currentGroup.groupName }}</div> -->
    </div>

      <el-table   v-loading="loading" style="cursor: pointer" :data="pageData" @sort-change="sortChange" @row-click="handdleRowClick" highlight-current-row >
        <el-table-column type="index" ></el-table-column>
        <el-table-column label="Cluster Name" property="clusterName" ></el-table-column>
        <el-table-column
            label="IsCluster"
          >
            <template slot-scope="scope">
            <el-tooltip placement="top" effect="light">
              <div style="width:300px; display:block;text-align:center" slot="content">
                <span v-if="!JSON.parse(scope.row.analyzeConfig).nodes || JSON.parse(scope.row.analyzeConfig).nodes[0]==='-1'"
                > All Cluster Nodes</span
                ><span size="small" v-else >{{JSON.stringify(JSON.parse(scope.row.analyzeConfig).nodes)}}</span>
              </div>
              <span v-if="!JSON.parse(scope.row.analyzeConfig).nodes || JSON.parse(scope.row.analyzeConfig).nodes[0]==='-1'"
                >Yes</span
              ><span size="small" v-else type="danger">No</span>
              </el-tooltip>
            </template>
        </el-table-column>
        <el-table-column
          label="Analyze Time"
          sortable
          property="scheduleId"
          :formatter="dateFormatter"
        ></el-table-column>
        <el-table-column label="Status">
          <template slot-scope="scope">
              <!-- <i v-if="!scope.row.result" class="el-icon-loading" style="font-size: 20px;color:orange"></i>
              <i v-else class="el-icon-circle-check" style="font-size: 20px;color:green"></i> -->
            <i class="el-icon-success status-icon normal-status" v-if="scope.row.done"></i>
            <i class="el-icon-loading status-icon bad-status" v-else></i>
           </template>
        </el-table-column>
        <el-table-column label="Config">
          <template slot-scope="scope">
          <a  style="cursor: pointer; color: rgba(49, 128, 253, 0.65)" @click.stop="getConfig(scope.$index, scope.row)">
            Detail
          </a>
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
          title="Job Config"
          :visible.sync="ConfigDetailVisible"
          :close-on-click-modal="false"
          width="30%"
          @close="closeHandler()">
          <div style="padding:10px 20px;text-align:center">
            <ul>
                  <li style="padding: 15px 0px; display: flex; borderTop: 1px solid #EEEFF3">
                    <div style="marginRight: 30px;textAlign: right;width: 120px;color:#999999">Cluster Name:</div>
                    <div >{{clusterName}}</div>
                  </li>
                  <li style="padding: 15px 0px; display: flex; borderTop: 1px solid #EEEFF3">
                    <div style="marginRight: 30px;textAlign: right;width: 120px;color:#999999">Nodes:</div>
                    <div class="icoFontlist" :title="nodes">{{nodes}}</div>
                  </li>
                  <li style="padding: 15px 0px; display: flex; borderTop: 1px solid #EEEFF3">
                    <div style="marginRight: 30px;textAlign: right;width: 120px;color:#999999">Auto Analyze:</div>
                    <el-switch  disabled v-model="ConfigDetail.autoAnalyze" active-color="#00a000" inactive-color="#404040"></el-switch>
                  </li>
                  <li style="padding: 15px 0px; display: flex; borderTop: 1px solid #EEEFF3">
                    <div style="marginRight: 30px;textAlign: right;width: 120px;color:#999999">Schedule:</div>
                    <div >{{ConfigDetail.schedule}}</div>
                  </li>

                  <li style="padding: 15px 0px; display: flex; borderTop: 1px solid #EEEFF3">
                    <div style="marginRight: 30px;textAlign: right;width: 120px;color:#999999">DataPath:</div>
                    <div class="icoFontlist" :title="ConfigDetail.dataPath">{{ConfigDetail.dataPath}}</div>
                  </li>
                  <li style="padding: 15px 0px; display: flex; borderTop: 1px solid #EEEFF3">
                    <div style="marginRight: 30px;textAlign: right;width: 120px;color:#999999">Custom Prefixes:</div>

                      <div class="icoFontlist" :title="ConfigDetail.prefixes">{{ConfigDetail.prefixes}}</div>

                  </li>
                  <li style="padding: 15px 0px; display: flex; borderTop: 1px solid #EEEFF3">
                    <div style="marginRight: 30px;textAlign: right;width: 120px;color:#999999">Report:</div>
                    <!-- <el-switch  disabled v-model="ConfigDetail.report"></el-switch> -->
                     <el-switch  disabled v-model="ConfigDetail.report" active-color="#00a000" inactive-color="#404040"></el-switch>
                  </li>
                  <li style="padding: 15px 0px; display: flex; borderTop: 1px solid #EEEFF3">
                    <div style="marginRight: 30px;textAlign: right;width: 120px;color:#999999">Mail:</div>
                   <div class="icoFontlist" :title="ConfigDetail.mailTo">{{ConfigDetail.mailTo}}</div>
                  </li>
                </ul>
                 <span slot="footer" class="dialog-footer">
        <!-- <el-button size="mini" type="warning" @click="deleteVisible = false"
          >Cancel</el-button
        > -->
         <el-button size="mini"  type="primary"  @click="ConfigDetailVisible=false">Ok</el-button>
        <!-- <el-button
          size="mini"
          type="danger"
          @click="deleteAnalyzeJob(analyseisJobFrom.id)"
          >Delete</el-button
        > -->
      </span>
          </div>
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
      ConfigDetail: {
      },
      nodes: '',
      clusterName: ''
    }
  },
  methods: {
    dateFormatter (row) {
      return formatTime(row.scheduleId)
    },
    handdleRowClick (row, event, column) {
      if (!row.done) {
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
        params: { clusterId: row.clusterId, clusterName: row.clusterName }
      })
    },
    toReportDetail (row) {
      this.$router.push({
        name: 'jobResultDetail',
        params: { analyzeConfig: row.analyzeConfig, clusterName: row.clusterName },
        // path: '/plugin/jobResultDetail',
        query: {
          detailId: row.id
        }
      })
    },
    closeHandler () {
      this.ConfigDetail = {}
      this.clusterName = ''
      this.nodes = ''
    },
    getConfig (index, row) {
      this.clusterName = row.clusterName
      this.ConfigDetail = JSON.parse(row.analyzeConfig)
      this.ConfigDetailVisible = true
      this.setNodes(this.ConfigDetail.nodes)
    },
    setNodes (value) {
      var temp = ''
      if (value == undefined) {
        temp = 'All Cluster Nodes'
      } else {
        for (let i in value) {
          if (value[0] === '-1') {
            temp = 'All Cluster Nodes'
          } else {
            temp = temp + value[i] + ' ,'
            temp = temp.substring(0, temp.lastIndexOf(','))
          }
        }
      }
      this.nodes = temp
    },
    getNodes (index, row) {
      this.getConfig(index, row)
      return this.nodes
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
      this.$router.push({
        name: 'jobList',
        params: { groupId: group.groupId }
      })
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
.normal-status {
  color: #40c9c6;
}

.bad-status {
  color: #f4516c;
}
 .link {
    margin: 0 5px;
    color: rgba(49, 128, 253, 0.65);
    cursor: pointer;
    text-decoration: none;
  }
  .icoFontlist:hover
{
    width: 225px;
    font-size: 12px;
    border: 0px solid #ddd;
    overflow: hidden;
    text-align: left;
    text-overflow: ellipsis;
    white-space: nowrap;
}
.icoFontlist{
    width: 225px;
    font-size: 12px;
    border: 0px solid #ddd;
    color:#5f5f5f;
    overflow: hidden;
    text-align: left;
    text-overflow: ellipsis;
    white-space: nowrap;
}
</style>
