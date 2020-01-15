<template>
  <div id="analysis" class="body-wrapper">
    <div class="header-wrapper">
      <div>{{ currentGroup.groupName }}</div>
      <div>
        <el-button size="mini" type="success" @click="addAnalysisJob()">New Job</el-button>
      </div>
    </div>
    <div>
      <el-table :data="analyseisJobs">
        <el-table-column type="index" width="50"></el-table-column>
        <el-table-column label="Cluster Name" property="name">
          <!-- <template slot-scope="scope">
            <span v-if="scope.row.clusterAlert">
              <el-tag size="small" type="primary">Cluster Alert</el-tag>
            </span>
            <span v-else>{{scope.row.ruleKey}} {{scope.row.compareSign}} {{scope.row.ruleValue}}</span>
          </template>-->
        </el-table-column>
        <el-table-column label="Schedule" property="schedule">
          <!-- <template slot-scope="scope">
            <el-tag size="small" type="success" v-if="scope.row.valid">valid</el-tag>
            <el-tag size="small" type="danger" v-else>Invalid</el-tag>
          </template>-->
        </el-table-column>
        <el-table-column label="Analyzer" property="analyzer">
          <!-- <template slot-scope="scope">
            <el-tag size="small" v-if="scope.row.global">Global</el-tag>
          </template>-->
        </el-table-column>

        <el-table-column label="Data Path" property="dataPath"></el-table-column>
        <el-table-column label="Prefixed" property="prefixes"></el-table-column>
        <el-table-column label="Report" property="report"></el-table-column>
        <el-table-column label="Status">
          <template slot-scope="scope">
            <i class="el-icon-loading" v-if="scope.row.status"></i>
            <i class="el-icon-circle-check" v-else></i>
          </template>
        </el-table-column>
        <el-table-column label="Operation" width="250px;">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="primary"
              @click="editAnalysisJob(scope.$index, scope.row)"
            >Edit</el-button>
            <el-button
              size="mini"
              type="danger"
              @click="handleDelete(scope.$index, scope.row)"
            >Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- dialog: add analyze job-->
    <el-dialog
      title="Analyse Job"
      :visible.sync="jobDialogVisible"
      width="30%"
      :close-on-click-modal="false"
      v-loading="jobDialogLoading"
    >
      <el-form :model="analyseisJobFrom" ref="analyseisJobFrom" :rules="rules" label-width="135px">
          <!-- <el-input
            size="small"
            v-model="analyseisJobFrom.name"
            placeholder="Please Select Redis Cluster"
            style="width: 215px;"
          ></el-input> -->
          <el-form-item label="Cluster Name" prop="name">
            <el-select size="small" v-model="analyseisJobFrom.name" placeholder="Select Redis Cluster">
              <el-option
                v-for="item in redisClusterList"
                :key="item.clusterId"
                :label="item.clusterName"
                :value="item.clusterId"
              ></el-option>
            </el-select>
          </el-form-item>
        <el-form-item label="schedule" prop="schedule">
          <el-input
            size="small"
            v-model="analyseisJobFrom.schedule"
            placeholder="Please schedule"
            style="width: 215px;"
          ></el-input>
        </el-form-item>
        <el-form-item label="analyzer" prop="analyzer">
          <el-input
            size="small"
            v-model="analyseisJobFrom.analyzer"
            placeholder="Please analyzer"
            style="width: 215px;"
          ></el-input>
        </el-form-item>
        <el-form-item label="dataPath" prop="dataPath">
          <el-input
            size="small"
            v-model="analyseisJobFrom.dataPath"
            placeholder="Please dataPath"
            style="width: 215px;"
          ></el-input>
        </el-form-item>
        <el-form-item label="prefixes" prop="prefixes">
          <el-input
            size="small"
            v-model="analyseisJobFrom.prefixes"
            placeholder="Please prefixes"
            style="width: 215px;"
          ></el-input>
        </el-form-item>
        <el-form-item label="report" prop="report">
          <el-input
            size="small"
            v-model="analyseisJobFrom.report"
            placeholder="Please report"
            style="width: 215px;"
          ></el-input>
        </el-form-item>
        <el-form-item label="Mail" prop="mailTo">
          <el-input
            type="textarea"
            autosize=true
            style="width: 215px;"
            v-model="analyseisJobFrom.mailTo">
          </el-input>
        </el-form-item>
        <!-- <el-form-item label="Cluster Alert" prop="clusterAlert">
          <el-switch v-model="alertRule.clusterAlert"></el-switch>
        </el-form-item>-->
        <!-- <div v-if="!alertRule.clusterAlert">
          <el-form-item label="Rule Key" prop="ruleKey">
            <el-select size="small" v-model="alertRule.ruleKey" placeholder="Select rule key">
              <el-option
                v-for="item in ruleKeyList"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="Compare Type" prop="compareType">
            <el-select
              size="small"
              v-model="alertRule.compareType"
              placeholder="Select compare type"
            >
              <el-option
                v-for="item in compareTypeList"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="Rule Value" prop="ruleValue">
            <el-input
              size="small"
              v-model="alertRule.ruleValue"
              placeholder="Please enter rule value"
              style="width: 215px;"
            ></el-input>
          </el-form-item>
        </div>
        <el-form-item label="Check Cycle" prop="checkCycle">
          <el-select size="small" v-model="alertRule.checkCycle" placeholder="Select check circle">
            <el-option
              v-for="item in checkCycleList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Is Valid" prop="isValid">
          <el-switch v-model="alertRule.valid"></el-switch>
        </el-form-item>
        <el-form-item label="Is Global" prop="isGlobal">
          <el-switch v-model="alertRule.global"></el-switch>
        </el-form-item>
        <el-form-item label="Rule Info" prop="ruleInfo">
          <el-input size="small" v-model="alertRule.ruleInfo" placeholder="Please enter rule info"></el-input>
        </el-form-item> -->
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button size="small" @click="cancelAnalyzeJobDialog('analyseisJobFrom')">Cancel</el-button>
        <el-button size="small" type="primary" @click="saveAnalyzeJob('analyseisJobFrom')">Confirm</el-button>
      </div>
    </el-dialog>

    <el-dialog title="Delete Alert Rule" :visible.sync="deleteVisible" width="30%">
      <span>
        Are you sure to delete
        <b>{{ analyseisJobFrom.name }}</b> ?
      </span>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="deleteVisible = false">Cancel</el-button>
        <el-button size="small" type="danger" @click="deleteAnalyzeJob()">Delete</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { store } from '@/vuex/store.js'
import { isEmpty } from '@/utils/validate.js'
import { formatTime } from '@/utils/time.js'
import API from '@/api/api.js'
import message from '@/utils/message.js'
export default {
  data () {
    return {
      analyseisJobs: [],
      // job detail 是否显示
      jobDialogVisible: false,
      // 是编辑还是新增
      isEdit: false,
      // 控制删除框是否显示
      deleteVisible: false,
      analyseisJobFrom: {},
      // 控制是否new Job和Edit时, job detail是否显示加载
      jobDialogLoading: false,
      rules: {
        name: [
          {
            required: true,
            message: "name can't be empty",
            trigger: 'blur'
          }
        ],
        schedule: [
          {
            required: true,
            message: "schedule can't be empty",
            trigger: 'blur'
          }
        ],
        analyzer: [
          {
            required: true,
            message: "analyzer can't be empty",
            trigger: 'blur'
          }
        ],
        dataPath: [
          {
            required: true,
            message: "dataPath can't be empty",
            trigger: 'blur'
          }
        ]
      },
      redisClusterList: []
    }
  },
  methods: {
    cancelAnalyzeJobDialog (analyseisJobFrom) {
      this.jobDialogVisible = false
      this.$refs[analyseisJobFrom].resetFields()
    },
    saveAnalyzeJob (analyseisJobFrom) {
      this.$refs[analyseisJobFrom].validate((valid) => {
        if (valid) {
          alert('submit!')
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    editAnalysisJob (index, row) {
      // this.getAlertRule(row.ruleId)
      this.isEdit = true
      this.jobDialogVisible = true
      this.analyseisJobFrom = row
    },
    handleDelete (index, row) {
      this.deleteVisible = true
      this.analyseisJobFrom = row
    },
    deleteAnalyzeJob () {
      console.log('delete analyze job ' + this.analyseisJobFrom.id)
      this.deleteVisible = false
      this.analyseisJobFrom = {}
      this.getAnalysisJobList()
    },
    addAnalysisJob () {
      this.analyseisJobFrom = {}
      this.isEdit = false
      this.jobDialogVisible = true
    },
    getRedisClusterList () {
      this.redisClusterList = [
        {
          clusterId: 1,
          clusterName: 'wwredis1'
        },
        {
          clusterId: 2,
          clusterName: 'wwredis2'
        }
      ]
    },
    getAnalysisJobList (groupId) {
      console.log('getAnalysisJobList')
      this.analyseisJobs = [
        {
          id: 10,
          name: 'wwredis1',
          schedule: '123456',
          analyzer: 'report',
          dataPath: 'dafdsfd',
          prefixes: 'a,b',
          report: 'true',
          status: true,
          mailTo: 'kyle.k.zhao@newegg.com;lola.l.gou@newegg.com'
        },
        {
          id: 20,
          name: 'wwredis2',
          schedule: '123456',
          analyzer: 'report',
          dataPath: 'dafdsfd',
          prefixes: 'a,b,c',
          report: 'true',
          status: false,
          mailTo: 'kyle.k.zhao@newegg.com;lola.l.gou@newegg.com;kyle.k.zhao@newegg.com;lola.l.gou@newegg.com;kyle.k.zhao@newegg.com;lola.l.gou@newegg.com'
        }
      ]
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
    }
  },
  mounted () {
    let groupId = this.currentGroup.groupId
    this.getAnalysisJobList(groupId)
    this.getRedisClusterList()
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
