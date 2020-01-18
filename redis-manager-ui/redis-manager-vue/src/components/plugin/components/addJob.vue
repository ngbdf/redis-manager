<template>
  <el-form
    :model="analyseisJobFrom"
    :rules="rules"
    ref="analyseisJobFrom"
    label-width="30%"
    class="demo-ruleForm"
  >
    <el-form-item label="Cluster Name" prop="clusterId">
      <el-select v-model="analyseisJobFrom.clusterId" :disabled="this.edit" placeholder="Select Redis Cluster" class="input" >
        <el-option
          v-for="item in this.redisClusterList"
          :key="item.clusterId"
          :label="item.clusterName"
          :value="item.clusterId"
        ></el-option>
      </el-select>
    </el-form-item>
    <el-form-item label="Auto Analyze" prop="autoAnalyze">
    <el-switch v-model="analyseisJobFrom.autoAnalyze"></el-switch>
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
              <el-button
                size="mini"
                type="primary"
                @click="saveAnalyzeJob('analyseisJobFrom')"
              >Confirm</el-button>
            </div>
    </el-form-item>
  </el-form>
</template>>

<script>
import { addAnalysisList, updateAnalyzeList, getCluster } from '@/api/rctapi.js'
import message from '@/utils/message.js'
export default {
  props: {
    groupId: {
      type: Number
    },
    from: {
      type: Object
    },
    edit: {
      type: Boolean
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
        autoAnalyze: false,
        schedule: '',
        analyzer: [],
        dataPath: '',
        prefixes: '',
        report: false,
        groupId: this.groupId,
        mailTo: ''
      },
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
      scheduleCron: ['2020-01-21 07:10:00', '2020-01-21 07:10:10'],
      labelPosition: 'right'
    }
  },
  methods: {
    cancelAnalyzeJobDialog (analyseisJobFrom) {
      this.$refs[analyseisJobFrom].resetFields()
      this.$emit('cancel', false)
    },
    saveAnalyzeJob (analyseisJobFrom) {
      this.$refs[analyseisJobFrom].validate(valid => {
        if (valid) {
          const body = Object.assign({}, this.analyseisJobFrom)
          body.analyzer = body.analyzer.toString()
          debugger
          if (body.id === '') {
            addAnalysisList(body).then(response => {
              message.success(response.message)
              this.$emit('cancel', false)
              this.$emit('refresh')
            })
          } else {
            updateAnalyzeList(body).then(response => {
              message.success(response.message)
              this.$emit('cancel', false)
              this.$emit('refresh')
            })
          }
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    getRedisClusterList () {
      getCluster(this.groupId).then(response => {
        this.redisClusterList = response.data
      })
    }
  },
  watch: {
    from: {
      immediate: true,
      handler (newValue, old) {
        this.$nextTick(() => {
          if (newValue !== old && this.edit) {
            this.analyseisJobFrom = newValue
          }
        }
        )
      }
    },
    edit: {
      immediate: true,
      handler (newValue, old) {
        this.$nextTick(() => {
          if (newValue !== old) {
            this.edit = newValue
          }
        }
        )
      }
    }
  },
  mounted () {
    // this.getRedisClusterList()
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
