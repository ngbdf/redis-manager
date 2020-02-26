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
      <el-option  value="-1" label="Cluster"></el-option>
      <el-option v-for="item in this.redisNodeList" :key="item.host+':'+item.port" :label="item.flags+' '+item.host+':'+item.port" :value="item.host+':'+item.port" :disabled="isAllNodes"></el-option>
    </el-select>
      </el-form-item>
    <el-form-item label="DataPath" prop="dataPath">
        <!-- <span>{{analyseisJobFrom.dataPath }}</span> -->
      <el-input v-model="analyseisJobFrom.dataPath" placeholder="Please dataPath" class="input" :disabled="this.analyze"></el-input>
    </el-form-item>

    <el-form-item label="Report" prop="report">
      <el-switch v-model="analyseisJobFrom.report"></el-switch>
    </el-form-item>
    <el-form-item label="Mail" prop="mailTo" v-if="analyseisJobFrom.report">
      <el-input type="textarea" :rows="2" class="input" v-model="analyseisJobFrom.mailTo" placeholder="Please input email address with ';'"></el-input>
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
        <el-button size="mini" type="danger" @click="openAnalyzeDialog('analyseisJobFrom')">Analyze</el-button>
      </div>
    </el-form-item>
  </el-form>
   <el-dialog
      title="Analyze Job"
      :visible.sync="analyzeVisable"
      width="30%"
      v-loading.fullscreen.lock="loading"
      :close-on-click-modal="false"
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
    var validateMailTo = (rule, value, callback) => {
      if (this.analyseisJobFrom.report) {
        if (value === '' || value === null) {
          return callback(new Error('if you need open the report switch,you must input the email'))
        } else {
          //    const reg = /^\w+((.\w+)|(-\w+))@[A-Za-z0-9]+((.|-)[A-Za-z0-9]+).[A-Za-z0-9]+$/
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
        cluster: '',
        nodes: ['-1'],
        schedule: '',
        analyzer: '0',
        dataPath: '',
        prefixes: '',
        report: false,
        groupId: this.groupId,
        manual: false,
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
        mailTo: [
          {
            validator: validateMailTo,
            trigger: 'blur'
          }
        ]
      },
      redisNodeList: [],
      analyzeVisable: false,
      labelPosition: 'right'
    }
  },
  methods: {
    cancelAnalyzeJobDialog (analyseisJobFrom) {
      this.$refs[analyseisJobFrom].resetFields()
      this.$emit('cancel', false)
    },
    async AnalyzeJob (analyseisJobFrom) {
      this.loading = true
      const body = Object.assign({}, this.analyseisJobFrom)
      // body.analyzer = body.analyzer.toString()
      body.manual = true
      await analyzeJob(body).then(response => {
        if (response.data.status) {
          this.$router.push({
            name: 'TaskProgress',
            params: { clusterId: body.clusterId, clusterName: body.cluster.clusterName}
          })
        } else {
          message.error(response.data.message)
        }
        this.loading = false
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
      this.analyseisJobFrom.nodes = ['0']
      this.isAllNodes = true
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
            this.analyseisJobFrom.nodes = ['-1']
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
    // groupId: {
    //   immediate: true,
    //   handler (newValue, old) {
    //     this.$nextTick(() => {
    //       this.getRedisNodeList()
    //     })
    //   }
    // }
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
