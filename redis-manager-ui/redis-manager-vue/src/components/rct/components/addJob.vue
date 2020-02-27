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
      <el-input v-model="analyseisJobFrom.schedule" placeholder="Please input schedule like this: 0 0/1 * * * ? (It is executed every minute)" class="input" @input="inputChange"></el-input>
      <el-tooltip class="item"  placement="right" effect="light">
        <div slot="content">
          <ul>
            <li v-for="time in scheduleCron " :key="time">{{time}}</li>
          </ul>
        </div>
        <i class="el-icon-message-solid"></i>
      </el-tooltip>
      <!-- <div><a target="_blank" href="http://cron.qqe2.com/" style="text-decoration:underline;color:blue">Generate the cron expression</a></div> -->
    </el-form-item>

    <el-form-item label="DataPath" prop="dataPath">
      <el-input v-model="analyseisJobFrom.dataPath" placeholder="Please dataPath" class="input"></el-input>
    </el-form-item>

    <el-form-item label="Report" prop="report">
        <el-switch v-model="analyseisJobFrom.report"></el-switch>
    </el-form-item>
    <el-form-item label="Mail" prop="mailTo" v-if="analyseisJobFrom.report">
      <el-input type="textarea" :rows="2" class="input" v-model="analyseisJobFrom.mailTo"  placeholder="Please input email address with ';'"></el-input>
    </el-form-item>
      <el-form-item label="Custom Prefixes" prop="prefixes">
      <el-input type="textarea" :rows="2" v-model="analyseisJobFrom.prefixes" placeholder="Please input your custom prefixe with ','" class="input"></el-input>
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
import { addAnalysisList, updateAnalyzeList, getCluster, cronExpress } from '@/api/rctapi.js'
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
    var validateMailTo = (rule, value, callback) => {
      if (this.analyseisJobFrom.report) {
        if (value === '' || value === null) {
          return callback(new Error('if you need open the report switch,you must input the email'))
        } else {
          const reg = /^([a-zA-Z0-9]+[_|\\_|\\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\\_|\\.]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3}$/
          if (value.search(';') !== -1) {
            const data = value.split(';')
            data.map((obj) => {
              if (!reg.test(obj)) {
                return callback(new Error('email address is invaild,please check!'))
              }
            })
          } else {
            if (!reg.test(value)) {
              return callback(new Error('email address is invaild,please check!'))
            }
          }
        }
      }
      return callback()
    }
    return {
      analyseisJobFrom: {
        id: '',
        clusterId: '',
        autoAnalyze: false,
        schedule: '',
        analyzer: '0',
        dataPath: '',
        prefixes: '',
        report: false,
        groupId: this.groupId,
        mailTo: ''
      },

      rules: {
        clusterId: [
          {
            required: true,
            message: "cluster can't be empty",
            trigger: 'change'
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
        ],
        mailTo: [
          {
            validator: validateMailTo,
            trigger: 'blur'
          }
        ]
      },
      scheduleCron: [],
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
          if (body.id === '') {
            addAnalysisList(body).then(response => {
              message.success(response.data)
              this.$emit('cancel', false)
              this.$emit('refresh', this.groupId)
            })
          } else {
            updateAnalyzeList(body).then(response => {
              message.success(response.data)
              this.$emit('cancel', false)
              this.$emit('refresh', this.groupId)
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
    },
    inputChange (cron) {
      // const express = cron.replace(/\s+/g, '')
      const express = cron.trim()
      if (express) {
        const data = { schedule: express }
        cronExpress(data).then(response => {
          this.scheduleCron = response.data
        })
      }
    }
  },
  watch: {
    from: {
      immediate: true,
      handler (newValue, old) {
        this.$nextTick(() => {
          if (newValue !== old && this.edit) {
            this.analyseisJobFrom = newValue
            this.inputChange(this.analyseisJobFrom.schedule)
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
