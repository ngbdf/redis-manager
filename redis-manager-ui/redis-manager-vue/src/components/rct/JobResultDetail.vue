<template>
  <div id="analysis">
   <el-card >
    <div ><el-image src="../../../static/back.svg" @click="backHistory()" class="images"></el-image><span style="margin-left:30px;font-size:20px">{{ name }}</span></div>
     </el-card>
    <!-- <div> -->
    <div  class="main-body" style="margin-top: 20px;">
      <!-- <el-row class="echart-wrapper" id="monitor-charts"> -->
        <KeyByTypePie pieType="count" :resultId="resultId"></KeyByTypePie>
        <KeyByTypePie pieType="memory" :resultId="resultId"></KeyByTypePie>
        <PrefixKeysCount v-show="isCluster" :resultId="resultId"></PrefixKeysCount>
        <PrefixKeysMemory v-show="isCluster" :resultId="resultId"></PrefixKeysMemory>
        <Tables :resultId="resultId" :tableObj="top1000keysPrefix" :initData="initTop1000Keys"/>
        <Tables :resultId="resultId" :tableObj="keysTTL" :initData="initKeysTTL"/>
        <Top1000KeysByType :resultId="resultId"></Top1000KeysByType>
      <!-- </el-row> -->
  </div>
    <!-- </div> -->
  </div>
</template>
<script>
import { formatTime } from '@/utils/time.js'
import { store } from '@/vuex/store.js'
import PrefixKeysCount from '@/components/rct/chart/PrefixKeysCount'
import PrefixKeysMemory from '@/components/rct/chart/PrefixKeysMemory'
import KeyByTypePie from '@/components/rct/chart/KeyByTypePie'
import Top1000KeysByType from '@/components/rct/chart/Top1000KeysByType'
import Tables from '@/components/rct/chart/Table'
import { formatBytes, formatterInput } from '@/utils/format.js'
import { getTop1000KeysByPrefix, getKeysTTLInfo } from '@/api/rctapi.js'
export default {
  beforeCreate: function () {
    document.querySelector('body').setAttribute('style', 'overflow:auto')
  },
  beforeDestroy: function () {
    document.querySelector('body').removeAttribute('style')
  },
  components: {
    PrefixKeysCount,
    PrefixKeysMemory,
    KeyByTypePie,
    Top1000KeysByType,
    Tables
  },

  data () {
    return {
      isCluster: '',
      analyseResults: [],
      name: '',
      top1000keysPrefix: {
        columns: [{
          label: 'Prefix',
          type: 'String',
          prop: 'prefixKey'
        }, {
          label: 'Count',
          sort: true,
          prop: 'keyCount',
          type: Number,
          formatter: this.formatterCount
        }, {
          label: 'Memory Size',
          sort: true,
          prop: 'memorySize',
          type: Number,
          formatter: this.formatMemory
        }],
        searchVis: true,
        searchColumn: 'prefixKey',
        title: 'Top 1000 Largest Keys By Custom Prefixes'
      },
      keysTTL: {
        columns: [{
          label: 'Prefix',
          prop: 'prefix',
          type: 'String'
        }, {
          label: 'TTL',
          sort: true,
          prop: 'TTL',
          type: Number,
          formatter: this.formatterCount
        }, {
          label: 'noTTL',
          sort: true,
          prop: 'noTTL',
          type: Number,
          formatter: this.formatterCount
        }],
        searchVis: true,
        searchColumn: 'prefix',
        title: 'Keys TTL Info'
      },
      resultId: String(this.$route.query.detailId)
    }
  },
  methods: {
    backHistory () {
      this.$router.push({
        name: 'jobList'
      })
    },
    dateFormatter (row) {
      return formatTime(row.scheduleId)
    },
    formatterCount (row, column, cellValue) {
      return formatterInput(cellValue)
    },
    formatMemory (row, column, cellValue) {
      return formatBytes(cellValue)
    },
    initTop1000Keys () {
      let res = getTop1000KeysByPrefix(this.resultId)
      return res
    },
    initKeysTTL () {
      let res = getKeysTTLInfo(this.resultId)
      return res
    }

  },
  mounted () {
    let analyzeConfig = this.$route.params.analyzeConfig
    if (analyzeConfig) {
      let analyzeConfigObj = JSON.parse(analyzeConfig)
      if (!analyzeConfigObj.nodes || analyzeConfigObj.nodes[0] === '-1') {
        this.isCluster = true
      } else {
        this.isCluster = false
      }
      let clusterName = this.$route.params.clusterName
      this.name = clusterName
    } else {
      this.$router.push({
        name: 'jobList'
      })
    }
  },
  watch: {
    currentGroup (group) {
      this.$router.push({
        name: 'jobList',
        params: { groupId: group.groupId }
      })
    }
  },
  computed: {
    currentGroup () {
      return store.getters.getCurrentGroup
    }
  }
}
</script>
<style scoped>
.monitor-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 1px solid #dcdfe6;
}

.cluster-name,
.health,
.bad {
  font-size: 18px;
}

.bad {
  color: #f4516c;
}

.base-info-wrapper {
  font-size: 14px;
  padding-top: 20px;
}

.base-info-title-wrapper {
  display: flex;
  margin-bottom: 10px;
  align-items: center;
}

.base-info-title {
  color: #909399;
  font-size: 16px;
  margin-right: 10px;
}

.base-info-item {
  padding-bottom: 10px;
}

.monitor-condition-wrapper {
  padding: 20px;
  font-size: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;

  background-color: #f0f2f5;
  border-radius: 4px;
}

.refresh {
  font-size: 20px;
  cursor: pointer;
  color: #909399;
}

.refresh:hover {
  color: #2c3e50;
}

.condition-wrapper {
  display: flex;
  justify-content: flex-start;
  align-items: center;
}

.condition-item {
  margin-right: 20px;
}

.time-picker-wrapper {
  float: right;
}

.echart-wrapper {
  margin: 10px -5px;
}

.box-card {
  margin: 5px;
}

.chart {
  min-height: 400px;
  width: 100%;
}

.chart-no-data {
  height: 0 !important;
}

.main-body{
    background-color: #ffffff !important;
}
.fieldStyle {
  margin-top: 8px;
}

.images{
    height: 26px;
    width: 26px;
    position: absolute;
    cursor: pointer;
}
</style>
