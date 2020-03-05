<template>
  <div>
    <div id="analysis" class="body-wrapper" v-if="!jobDialogVisible">
      <div class="header-wrapper">
          <div/>
        <div>
          <el-button size="small" type="primary" plain @click="addAnalysisJob()" icon="el-icon-plus"
            >New Job</el-button>
        </div>
      </div>
      <div>
        <el-table :data="analyseisJobs" stripe class="table"  v-loading="loading">
          <el-table-column
            label="Cluster Name"
            property="cluster.clusterName"

          ></el-table-column>
          <el-table-column label="Schedule" property="schedule" >
            <template slot-scope="scope">
              <span
                v-if="scope.row.schedule !== '' && scope.row.schedule !== null"
                >{{ scope.row.schedule }}</span
              >
              <span v-else type="success">-</span>
            </template>
          </el-table-column>
          <el-table-column
            label="AutoAnalyze"
            property="autoAnalyze"
          >
            <template slot-scope="scope">
              <el-image v-if="scope.row.autoAnalyze" src="../../../static/on.svg"> </el-image>
              <el-image v-else src="../../../static/off.svg"> </el-image>
            </template>
          </el-table-column>
          <el-table-column label="Operation" >
            <template slot-scope="scope">
              <el-button
                type="primary"
                size="mini"
                icon="el-icon-edit"
                @click="editAnalysisJob(scope.$index, scope.row)"
              >Edit</el-button>
              <el-button
                size="mini"
                type="danger"
                plain
                icon="el-icon-delete"
                @click="handleDelete(scope.$index, scope.row)"
              >Del</el-button>
              <el-button
                size="mini"
                type="warning"
                @click="handleAnalyze(scope.$index, scope.row)"
                icon="el-icon-s-data"
              >Analyze</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <div class="body-wrapper" v-if="jobDialogVisible">
      <div class="header-wrapper">
        <div v-if="this.isEdit">Edit Job</div>
        <div v-if="this.analyseisVisable">Analyze Job</div>
        <div v-if="!this.isEdit && !this.analyseisVisable">Add Job</div>
        <!-- <div>{{ isEdit ? "Edit Job" : "Add Job" }}</div> -->
      </div>
      <!-- dialog: add analyze job-->
      <div class="addJob">
        <addJob
          v-on:cancel="cancel"
          @refresh="getAnalysisJobList"
          :groupId="this.currentGroup.groupId"
          :from="this.analyseisJobFrom"
          :edit="this.isEdit"
          :redisClusterList="this.redisClusterList"
          v-if="!this.analyseisVisable"
        />
        <analyzeJob
         v-on:cancel="cancel"
         :from="this.analyseisJobFrom"
          :redisClusterList="this.redisClusterList"
          :analyze="this.analyseisVisable"
         v-else />
      </div>
    </div>
    <el-dialog
      title="Delete Alert Rule"
      :visible.sync="deleteVisible"
      width="30%"
    >
      <span>
        Are you sure to delete
        <b>{{ analyseisJobFrom.name }}</b> ?
      </span>
      <span slot="footer" class="dialog-footer">
        <el-button size="mini" type="warning" @click="deleteVisible = false"
          >Cancel</el-button
        >
        <el-button
          size="mini"
          type="danger"
          @click="deleteAnalyzeJob(analyseisJobFrom.id)"
          >Delete</el-button
        >
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { store } from '@/vuex/store.js'
import { getAnalysisList, deletAnalyze, getCluster } from '@/api/rctapi.js'
import AddJob from './components/addJob'
import AnalyzeJob from './components/analyzeJob'
export default {
  components: {
    addJob: AddJob,
    analyzeJob: AnalyzeJob
  },
  data () {
    return {
      analyseisJobs: [],
      // job detail 是否显示
      jobDialogVisible: false,
      // 是编辑还是新增
      isEdit: false,

      analyseisJobFrom: {
        id: '',
        clusterId: '',
        cluster: '',
        autoAnalyze: false,
        schedule: '',
        analyzer: '0',
        dataPath: '',
        prefixes: '',
        report: false,
        groupId: '',
        mailTo: ''
      },
      // 控制删除框是否显示
      deleteVisible: false,

      loading: false,
      // 控制是否new Job和Edit时, job detail是否显示加载
      jobDialogLoading: false,

      analyseisVisable: false,

      redisClusterList: []
    }
  },
  methods: {
    editAnalysisJob (index, row) {
      this.isEdit = true
      this.jobDialogVisible = true
      this.analyseisJobFrom = Object.assign({}, row)
    },
    handleDelete (index, row) {
      this.deleteVisible = true
      this.analyseisJobFrom = row
    },
    handleAnalyze (index, row) {
      this.jobDialogVisible = true
      this.analyseisVisable = true
      this.isEdit = false
      this.analyseisJobFrom = Object.assign({}, row)
    },
    deleteAnalyzeJob (id) {
      deletAnalyze(id).then(response => {
        this.deleteVisible = false
        this.analyseisJobFrom = {}
        this.getAnalysisJobList(this.currentGroup.groupId)
      })
    },
    addAnalysisJob () {
      this.analyseisJobFrom = {}
      this.isEdit = false
      this.jobDialogVisible = true
    },
    changeAnalyzeToArray (analyzer) {
      if (analyzer != null && analyzer !== '') {
        if (!(analyzer instanceof Array)) {
          analyzer = analyzer.split(',')
        }
      } else {
        analyzer = []
      }
      return analyzer
    },
    async getAnalysisJobList (groupId) {
      this.loading = true
      await getAnalysisList(groupId).then(response => {
        const data = response.data
        this.analyseisJobs = data
        this.loading = false
      })
    },
    cancel (visable) {
      this.jobDialogVisible = visable
      if (this.analyseisVisable) {
        this.analyseisVisable = visable
      }
    },
    getRedisClusterList (groupId) {
      getCluster(groupId).then(response => {
        this.redisClusterList = response.data
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
      this.getAnalysisJobList(group.groupId)
      this.getRedisClusterList(group.groupId)
    },
    visable: {
      immediate: true,
      handler (newValue, old) {
        this.$nextTick(() => {
          this.jobDialogVisible = newValue
        })
      }
    }
  },
  mounted () {
    let groupId = this.currentGroup.groupId
    this.getAnalysisJobList(groupId)
    this.getRedisClusterList(groupId)
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
.addJob {
  margin-top: 20px;
}
.input {
  width: 70%;
}
.footer {
  margin-left: 20%;
}
.tags {
  margin-right: 5px;
}
.table {
  text-align: center;
  width: 100%;
}
</style>
