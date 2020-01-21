<template>
  <div id="taskProgress" class="body-wrapper">
    <div class="header-wrapper">
      <div><el-button size="mini"  type="primary" icon="el-icon-back" @click="backHistory()">Back</el-button></div>
      <div class="filedStyle">instance:</div>
      <div class="searchStyle"><el-input size="mini" v-model="searchData" prefix-icon="el-icon-search" placeholder="input redis instance"></el-input></div>
      <div class="buttonStyle"><el-button size="mini" :disabled="this.cancelButtonDisabled" type="success" @click="cancelAnalysis()">Cancel</el-button></div>
    </div>
    <div>
      <el-table :data="analyseisJobDetail">
        <el-table-column type="index" width="50"></el-table-column>
        <el-table-column label="Redis Instance" property="instance"></el-table-column>
        <el-table-column label="Status" property="status">
          <template slot-scope="scope">
            <font v-if="scope.row.status === 'RUNNING'" color="#5485F7">RUNNING</font>
            <font v-else-if="scope.row.status === 'DONE'" color="#8000ff">DONE</font>
            <font v-else-if="scope.row.status === 'READY'" color="#0000a0">READY</font>
            <font v-else-if="scope.row.status === 'CANCELED'" color="999999">CANCELED</font>
            <font v-else-if="scope.row.status === 'NOT_START'" color="#FA7070">NOT_START</font>
            <font v-else-if="scope.row.status === 'ERROR'" color="#ff0000">ERROR</font>
            <font v-else-if="scope.row.status === 'NOTINIT'" color="#FA7070">NOTINIT</font>
            <font v-else-if="scope.row.status === 'CHECKING'" color="#FA7070">CHECKING</font>
          </template>
        </el-table-column>
        <el-table-column label="Process" property="process">
          <template slot-scope="scope">
            <el-progress  v-if="scope.row.status === 'CANCELED'" :percentage="scope.row.process"></el-progress>
            <el-progress  v-else :percentage="scope.row.process"></el-progress>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
<script>
import { store } from '@/vuex/store.js'
import { isEmpty } from '@/utils/validate.js'
import { formatTime } from '@/utils/time.js'
import API from '@/api/api.js'
import message from '@/utils/message.js'

import { getScheduleDetail, cancelAnalyzeTask } from '@/api/rctapi.js'

export default {
  data () {
    return {
      originalData: [],
      analyseisJobDetail: [],
      cancelButtonDisabled: false,
      searchData: ''
    }
  },
  created () {
    let clusterId = this.$route.params.clusterId
    this.timer = setInterval(() => {
      this.getAllScheduleDetail(clusterId)
    }, 3000)
  },
  beforeDestroy () {
    if (this.timer) {
      clearInterval(this.timer)
    }
  },
  methods: {
    async getAllScheduleDetail (id) {
      /**
       * 下面两组数据为测试数据，如果需要正式测试，把这两组数据注释掉，同时把下面三行注释放开
       */
    //   this.originalData = [
    //         {
    //             instance: "6.6.6.6:9002",
    //             status: "RUNNING",
    //             process: Math.floor(Math.random() * 100)
    //         },
    //         {
    //             instance: "8.8.8.8:9002",
    //             status: "CANCELED",
    //             process: Math.floor(Math.random() * 100)
    //         } ,
    //         {
    //             instance: "9.9.9.9:9002",
    //             status: "READY",
    //             process: Math.floor(Math.random() * 100)
    //         }
    //     ]
    //     this.analyseisJobDetail = [
    //         {
    //             instance: "6.6.6.6:9002",
    //             status: "RUNNING",
    //             process: Math.floor(Math.random() * 100)
    //         },
    //         {
    //             instance: "8.8.8.8:9002",
    //             status: "CANCELED",
    //             process: Math.floor(Math.random() * 100)
    //         } ,
    //         {
    //             instance: "9.9.9.9:9002",
    //             status: "READY",
    //             process: Math.floor(Math.random() * 100)
    //         }
    //     ]
      const result = await getScheduleDetail(id)
      this.originalData = result.data
      this.analyseisJobDetail = result.data
      if (this.searchData) {
        let list = this.originalData.filter((item, index) =>
          item.instance.includes(this.searchData)
        )
        this.analyseisJobDetail = list
      }
      let count = 0
      for (let i = 0; i < this.originalData.length; i++) {
        if (this.originalData[i].status === 'DONE') {
          count += 1
        }
      }
      if (count === this.originalData.length) {
        this.cancelButtonDisabled = true
      } else {
        this.cancelButtonDisabled = false
      }
    },
    backHistory () {
      this.$router.go(-1)
    },
    stopTimer () {
      if (this.timer) {
        clearInterval(this.timer)
      }
    },
    cancelAnalysis () {
      let clusterId = this.$route.params.clusterId
      this.$confirm('Are you sure you want to stop all task processes ?', 'Message', {
        confirmButtonText: 'Yes',
        cancelButtonText: 'No',
        type: 'warning'
      })
        .then(() => {
          const result = cancelAnalyzeTask(clusterId).then(result => {
            if (result.data.canceled) {
              //   if (true) {
              this.stopTimer()
              this.cancelButtonDisabled = true
              this.$message({
                type: 'success',
                message: 'Stop Success!'
              })
            } else {
              this.$message({
                type: 'success',
                message: 'Stop Error!'
              })
            }
          })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: 'Cancel'
          })
        })
    }
  },

  computed: {
    currentGroup () {
      return store.getters.getCurrentGroup
    }
  },

  watch: {
    searchData (val) {
      let list = this.originalData.filter((item, index) =>
        item.instance.includes(val)
      )
      this.analyseisJobDetail = list
    }
  },

  mounted () {
    let clusterId = this.$route.params.clusterId
    this.getAllScheduleDetail(clusterId)
  }
}
</script>

<style scoped>
.body-wrapper {
  min-width: 1000px;
}
.header-wrapper {
  float:left;
}
.filedStyle {
  margin-left: 20px
}
.searchStyle {
  margin-left: 10px
}
.buttonStyle {
  margin-left: 1200px
}
</style>
