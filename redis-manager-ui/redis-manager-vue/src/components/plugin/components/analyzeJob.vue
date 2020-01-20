<template>
<div>
  <el-form
    :model="analyseisJobFrom"
    :rules="rules"
    ref="analyseisJobFrom"
    label-width="30%"
    class="demo-ruleForm"
  >
    <el-form-item label="Cluster Name">
      <el-select
        v-model="analyseisJobFrom.clusterId"
        placeholder="Select Redis Cluster"
        class="input"
        :disabled="this.analyze"

      >
     <el-option
          v-for="item in this.redisClusterList"
          :key="item.clusterId"
          :label="item.clusterName"
          :value="item.clusterId"
        ></el-option>

     </el-select>
    </el-form-item>
      <el-form-item label="Nodes" prop="nodes">
    <el-select v-model="analyseisJobFrom.nodes" placeholder="Select Analyze Nodes" class="input" multiple @change="changeNodes" >
      <el-option key="-1" value="-1" label="ALL"></el-option>
      <el-option v-for="item in this.redisNodeList" :key="item.host+':'+item.port" :label="item.flags+' '+item.host+':'+item.port" :value="item.host+':'+item.port" :disabled="isAllNodes"></el-option>
    </el-select>
      </el-form-item>
    <el-form-item label="Schedule" prop="schedule" v-if="analyseisJobFrom.autoAnalyze">
      <el-input v-model="analyseisJobFrom.schedule" placeholder="Please schedule" class="input"></el-input>
      <el-tooltip class="item" effect="dark" placement="right">
        <div slot="content">
          <ul>
            <li v-for="time in scheduleCron " :key="time">{{time}}</li>
          </ul>
        </div>
        <i class="el-icon-message-solid"></i>
      </el-tooltip>
    </el-form-item>
    <el-form-item label="Analyzer" prop="analyzer">
      <el-checkbox-group v-model="analyseisJobFrom.analyzer">
        <el-checkbox label="0">Report</el-checkbox>
        <el-checkbox label="6">ExportKeyByFilter</el-checkbox>
        <el-checkbox label="5">ExportKeyByPrefix</el-checkbox>
      </el-checkbox-group>
    </el-form-item>
    <el-form-item label="DataPath" prop="dataPath">
      <el-input v-model="analyseisJobFrom.dataPath" placeholder="Please dataPath" class="input"></el-input>
    </el-form-item>
    <el-form-item label="Prefixes" prop="prefixes">
      <el-input v-model="analyseisJobFrom.prefixes" placeholder="Please prefixes" class="input"></el-input>
    </el-form-item>
    <el-form-item label="Report" prop="report">
      <el-switch v-model="analyseisJobFrom.report"></el-switch>
    </el-form-item>
    <el-form-item label="Mail" prop="mailTo">
      <el-input type="textarea" :rows="2" class="input" v-model="analyseisJobFrom.mailTo"></el-input>
    </el-form-item>
    <el-form-item>
      <div class="footer">
        <el-button
          size="mini"
          type="info"
          @click="cancelAnalyzeJobDialog('analyseisJobFrom')"
          plain
        >Cancel</el-button>
        <el-button size="mini" type="danger" @click="openAnalyzeDialog('analyseisJobFrom')">Analyze</el-button>
      </div>
    </el-form-item>
  </el-form>
   <el-dialog
      title="Analyze Job"
      :visible.sync="analyzeVisable"
      width="30%"
       v-loading="loading"
    >
      <span>
        Are you sure to Analyze this job?
      </span>
      <span slot="footer" class="dialog-footer">
        <el-button size="mini"  @click="analyzeVisable = false"
          >No</el-button
        >
        <el-button
          size="mini"
          type="danger"
          @click="AnalyzeJob(analyseisJobFrom)"
          >Yes</el-button
        >
      </span>
    </el-dialog></div>
</template>

<script>
import {
  getClusterNodes,
  analyzeJob
} from '@/api/rctapi.js'
import message from '@/utils/message.js'

export default {
  props: {
    groupId: {
      type: Number
    },
    analyze: {
      type: Boolean
    },
    from: {
      type: Object
    },
    redisClusterList: {
      type: Array
    }
  },

  data () {
    return {
      analyseisJobFrom: {
        id: '',
        clusterId: '',
        nodes: [],
        schedule: '',
        analyzer: [],
        dataPath: '',
        prefixes: '',
        report: false,
        groupId: this.groupId,
        mailTo: ''
      },
      isAllNodes: false,
      loading: false,
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
            type: 'array',
            required: true,
            message: "analyzer can't be empty",
            trigger: 'change'
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
      redisNodeList: [],
      analyzeVisable: false,
      scheduleCron: ['2020-01-21 07:10:00', '2020-01-21 07:10:10'],
      labelPosition: 'right'
    }
  },
  methods: {
    cancelAnalyzeJobDialog (analyseisJobFrom) {
      this.$refs[analyseisJobFrom].resetFields()
      this.$emit('cancel', false)
    },
    AnalyzeJob (analyseisJobFrom) {
      this.loading = true
      const body = Object.assign({}, this.analyseisJobFrom)
      body.analyzer = body.analyzer.toString()
      analyzeJob(body).then(response => {

      })
      this.$router.push({
        name: 'TaskProgress',
        params: { clusterId: body.clusterId }
      })
    },
    openAnalyzeDialog (analyseisJobFrom) {
      this.$refs[analyseisJobFrom].validate(valid => {
        if (valid) {
          this.analyzeVisable = true
        } else {
          return false
        }
      })
    },
    getRedisNodeList () {
      getClusterNodes(this.from.clusterId).then(response => {
        this.redisNodeList = response.data
      })
    },
    changeNodes (element) {
      if (element.length === 0) {
        this.isAllNodes = false
      }
      element.map((value, index) => {
        if (value === '-1') {
          this.isAllNodes = true
          this.analyseisJobFrom.nodes = ['-1']
        }
      })
    }
  },
  watch: {
    from: {
      immediate: true,
      handler (newValue, old) {
        this.$nextTick(() => {
          if (newValue !== old) {
            this.analyseisJobFrom = newValue
          }
        })
      }
    },
    analyze: {
      immediate: true,
      handler (newValue, old) {
        this.$nextTick(() => {
          if (newValue !== old) {
            this.analyze = newValue
          }
        })
      }
    }
  },
  mounted () {
    this.getRedisNodeList()
  }
}
</script>
<style  scoped>
.input {
  width: 70%;
}
.footer {
  margin-left: 20%;
}
</style>>
