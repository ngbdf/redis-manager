<template>
  <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
    <el-card shadow="hover" class="box-card">
      <!-- <div id="prefixKeysCount" class="chart"></div> -->
      <highcharts :options="chartOptions" :callback="myCallback"></highcharts>
    </el-card>
  </el-col>
</template>
<script>
import { getPrefixKeysCount, getTimeData } from '@/api/rctapi.js'
import { formatTime } from '@/utils/time.js'
import { formatBytes } from '@/utils/format.js'
import { Chart } from 'highcharts-vue'
// let echarts = require('echarts/lib/echarts')
export default {
  props: {
    resultId: {
      type: String
    }
  },
  components: {
    highcharts: Chart
  },
  data () {
    return {
      echartsData: [],
      // legendData: [],
      xAxisData: [],
      chartOptions: {}
    }
  },
  methods: {
    myCallback () {
    },
    async initCharts () {
      const res = await getTimeData(this.resultId)

      let timeList = res.data.map(value => {
        return formatTime(parseInt(value.value, 10))
      })
      //
      this.xAxisData = timeList

      const response = await getPrefixKeysCount(this.resultId)
      //
      this.echartsData = response.data.map(value => {
        return {
          name: value.key,
          type: 'line',
          pointStart: 0,
          data: value.value.split(',').map(value => parseInt(value, 10))
        }
      })
      //
      // this.legendData = response.data.map(value => value.key)

      this.chartOptions = {
        credits: {
          enabled: false
        },
        chart: {
          type: 'line'
        },
        title: {
          text: 'Prefix Keys Count'
        },
        tooltip: {
          formatter () {
            return [`<b>${this.x}</b>`].concat(
              this.points.map((point) => {
                return `${point.series.name}: ${formatBytes(point.y)}`
              })
            )
          },
          split: true
        },
        xAxis: {
          title: {
            text: 'time'
          },
          categories: this.xAxisData
        },
        yAxis: [
          {
            title: {
              text: 'Bytes'
            },
            labels: {
              formatter () {
                return formatBytes(this.value)
              }
            }
          }
        ],
        series: this.echartsData
      }
    },
    async getXAxisData () {
      const res = await getTimeData(this.resultId)
      let timeList = res.data.map(value => value.value)
      this.xAxisData = timeList
    },
    async refreshData () {
      const response = await getPrefixKeysCount(this.resultId)
      this.echartsData = response.data.map(value => {
        return {
          name: value.key,
          type: 'line',
          data: value.value
        }
      })
      this.legendData = response.data.map(value => {
        return value.key
      })
    }
  },
  mounted () {
    this.initCharts()
  }
}
</script>
<style scoped>
.box-card {
  margin: 5px;
  height: 450px;
}

.chart {
  min-height: 400px;
  width: 100%;
}

.chart-no-data {
  height: 0 !important;
}
</style>
