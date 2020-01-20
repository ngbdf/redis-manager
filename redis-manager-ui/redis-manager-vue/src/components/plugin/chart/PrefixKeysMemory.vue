<template>
  <!-- <div >
    <div id="prefixKeysCount"></div>
    <div>fdasfd</div>
  </div> -->
  <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
    <el-card shadow="hover" class="box-card">
      <div id="prefixKeysMemory" class="chart"></div>
      <!-- <div id="infoItem" class="chart" :class="{ 'chart-no-data' : noNodeInfoData }"></div> -->
    </el-card>
  </el-col>
</template>
<script>
import { getPrefixKeysCount } from '@/api/rctapi.js'
let echarts = require('echarts/lib/echarts')
// require('echarts/lib/chart/line')
// import echarts from 'echarts'
import { formatTime } from '@/utils/time.js'
export default {
  data () {
    return {
      echartsData: []
    }
  },
  methods: {
    initCharts () {
      console.log('init')
      let myChart = echarts.init(document.getElementById('prefixKeysMemory'))
      myChart.setOption({
        title: {
          text: 'Prefix Keys Memory'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'time',
          data: ['2019-04-16 15:59:21', '2019-04-18 15:59:21', '2019-04-10 15:59:21']
        },
        yAxis: [
          {
            title: {
              text: 'Count'
            }
          }
        ],
        series: [
          {
            name: 'pe*',
            type: 'line',
            data: ['441147957', '441147957', '630271092']
          },
          {
            name: 'pe1*',
            type: 'line',
            data: ['441147957', '441147957', '630271092']
          },
          {
            name: 'pe2*',
            type: 'line',
            data: ['441147957', '441147957', '630271092']
          },
          {
            name: 'pe3*',
            type: 'line',
            data: ['441147957', '441147957', '630271092']
          },
          {
            name: 'pe4*',
            type: 'line',
            data: ['441147957', '441147957', '630271092']
          },
          {
            name: 'pe5*',
            type: 'line',
            data: ['441147957', '441147957', '630271092']
          },
          {
            name: 'pe6*',
            type: 'line',
            data: ['441147957', '441147957', '630271092']
          }
        ]
      })
    },
    async refreshData () {
      console.log('refreshData')
      const res = await getPrefixKeysCount(2, 1550109815828)
      this.echartsData = res.data
    }
  },
  mounted () {
    this.initCharts()
  },
  watch: {
    // 深度监听 clusterId and schedule 变化
    clusterId: {
      handler: function () {
        this.refreshData()
      },
      deep: true
    }
  }
}
</script>
<style scoped>
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
