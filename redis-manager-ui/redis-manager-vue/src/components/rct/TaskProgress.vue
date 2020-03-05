<template>
  <div id="taskProgress" class="body-wrapper">
    <div class="header-wrapper">
      <!-- <div><el-image src="../../../static/back.svg" @click="backHistory()"></el-image></div>
      <div class="fieldStyle">instance:</div>
      <div class="searchStyle"><el-input size="mini" v-model="searchData" prefix-icon="el-icon-search" placeholder="input redis instance"></el-input></div> -->
      <div class="fieldStyle"><el-image src="../../../static/back.svg" @click="backHistory()" class="images"> </el-image><span style="margin-left:30px;font-size:20px">{{clusterName}}</span></div>
      <div class="searchStyle"><el-input size="mini" v-model="searchData" prefix-icon="el-icon-search" placeholder="input redis instance"></el-input></div>
         <!-- </div> -->
    </div>
    <div class="cancelStyle"><el-button size="mini" :disabled="this.cancelButtonDisabled" type="success" @click="cancelAnalysis()">Cancel</el-button></div>
    <div>
      <el-table v-loading="loading" :data="analyseisJobDetail">
        <el-table-column type="index" width="50"></el-table-column>
        <el-table-column sortable label="Redis Instance" property="instance"></el-table-column>
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
      searchData: '',
      loading: false,
      clusterName: ''
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
    getAllScheduleDetail (id) {
      const result = getScheduleDetail(id).then(result => {
        // for (var i = 0; i < result.data.length; i++) {
        //   result.data[i].clusterName = this.clusterName
        // }
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
          if (this.originalData[i].status === 'CANCELED') {
            this.originalData[i].process = 0
            this.cancelButtonDisabled = true
            this.loading = false
            this.stopTimer()
          }
        }
        if (count === this.originalData.length) {
          this.cancelButtonDisabled = true
          this.stopTimer()
        }
      })
    },
    backHistory () {
      this.$router.push({
        name: 'jobList'
      })
    },
    stopTimer () {
      if (this.timer) {
        clearInterval(this.timer)
      }
    },
    cancelAnalysis () {
      let scheduleID = this.originalData[0].scheduleID
      let clusterId = this.$route.params.clusterId
      this.$confirm('Are you sure you want to stop all task processes ?', 'Message', {
        confirmButtonText: 'Yes',
        cancelButtonText: 'No',
        type: 'warning'
      })
        .then(() => {
          this.loading = true
          cancelAnalyzeTask(clusterId, scheduleID).then(result => {
            if (result.data.canceled) {
              this.cancelButtonDisabled = true
              this.$message({
                type: 'success',
                message: 'cancel job success!'
              })
              this.loading = false
            } else {
              this.$message({
                type: 'warning',
                message: 'cancel job has error,Please pay attention to the task status!'
              })
              this.loading = false
            }
            //  this.loading = false
          })
        })
        .catch(() => {
          this.$message({
            type: 'error',
            message: 'cancel job has error,please check!'
          })
          this.loading = false
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
    this.clusterName = this.$route.params.clusterName
    this.getAllScheduleDetail(clusterId)
  }
}
</script>

<style scoped>
.body-wrapper {
  min-width: 1000px;
}
.header-wrapper {
float: left;
}
.fieldStyle {
  margin-top: 8px;
  display: inline-flex;
}
.searchStyle {
  margin-top: 8px;
  margin-left: 10px
}
.cancelStyle {
  margin-top: 18px;
  margin-left: 95%
}
.images{
    height: 26px;
    width: 26px;
    position: absolute;
    cursor: pointer;
}
</style>
