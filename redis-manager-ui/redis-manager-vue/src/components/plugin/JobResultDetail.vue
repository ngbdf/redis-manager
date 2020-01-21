<template>
  <div id="analysis" class="body-wrapper">
    <div class="header-wrapper">
      <div>{{ currentGroup.groupName }}</div>
    </div>
    <div style="margin-top: 20px;">
      <el-row class="echart-wrapper" id="monitor-charts">
        <KeyByTypePie pieType="count" :resultId="resultId" :clusterId="clusterId" :scheduleId="scheduleId"></KeyByTypePie>
        <KeyByTypePie pieType="memory" :resultId="resultId"></KeyByTypePie>
        <PrefixKeysCount :resultId="resultId"></PrefixKeysCount>
        <PrefixKeysMemory :resultId="resultId"></PrefixKeysMemory>
        <Top1000KeysByPrefix :resultId="resultId"></Top1000KeysByPrefix>
        <KeysTTLInfo :resultId="resultId"></KeysTTLInfo>
        <Top1000KeysByType :resultId="resultId"></Top1000KeysByType>
      </el-row>
  </div>
  </div>
</template>
<script>
import { formatTime } from '@/utils/time.js'
import { store } from '@/vuex/store.js'
import PrefixKeysCount from '@/components/plugin/chart/PrefixKeysCount'
import PrefixKeysMemory from '@/components/plugin/chart/PrefixKeysMemory'
import KeyByTypePie from '@/components/plugin/chart/KeyByTypePie'
import Top1000KeysByPrefix from '@/components/plugin/chart/Top1000LargestKeysByPrefix'
import KeysTTLInfo from '@/components/plugin/chart/KeysTTLInfo'
import Top1000KeysByType from '@/components/plugin/chart/Top1000KeysByType'
export default {
  components: {
    PrefixKeysCount,
    PrefixKeysMemory,
    KeyByTypePie,
    Top1000KeysByPrefix,
    KeysTTLInfo,
    Top1000KeysByType
  },
  data () {
    return {
      analyseResults: [],
      //clusterId: String(this.$route.query.clusterId),
      //resultId: String(this.$route.query.detailId)
      clusterId: '2',
      scheduleId: '1579481459916',
      resultId: String(this.$route.query.detailId)
    }
  },
  methods: {
    dateFormatter (row) {
      return formatTime(row.scheduleId)
    }
  },
  mounted () {
    console.log(this.$route.query.detailId)
    console.log(this.$route.query.clusterId)
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
  background-color: #ffffff;
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
</style>
