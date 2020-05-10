<template>
  <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
    <el-card shadow="hover" class="box-card">
      <div :id="id" class="chart"></div>
    </el-card>
  </el-col>
</template>
<script>
import { getPrefixKeysMemory } from '@/api/rctapi.js'
import { formatTime } from '@/utils/time.js'
import { formatBytes } from '@/utils/format.js'
var echarts = require('echarts/lib/echarts')

require('echarts/lib/chart/line')
// 以下的组件按需引入
require('echarts/lib/component/tooltip')
// tooltip组件
require('echarts/lib/component/title')
//  title组件
require('echarts/lib/component/legend')
// legend组件
export default {
  props: {
    resultId: {
      type: String
    },
    selectPrefixValue: {
      type: String
    }
  },
  data () {
    return {
      id: 'prefixKeysMemory'
    }
  },
  methods: {
    async initCharts () {
      const response = await getPrefixKeysMemory(this.resultId, this.selectPrefixValue)
      let timeList = response.data.time.map(value => {
        return formatTime(parseInt(value, 10))
      })
      //
      let echartsData = response.data.data.map(value => {
        return {
          name: value.key,
          type: 'line',
          symbol: 'circle',
          data: value.value.split(',').map(value => parseInt(value, 10))
        }
      })
      let chartOptions = {
        title: {
          text: 'Prefix Keys Memory'
        },
        tooltip: {
          trigger: 'axis',
          formatter: function (params) {
            var result = ''
            params.forEach(function (item) {
              result += item.marker + ' ' + item.seriesName + ' : ' + formatBytes(item.value) + '</br>'
            })
            return result
          }
        },
        grid: {
          height: '400px',
          containLabel: true
        },
        legend: {
          icon: 'circle',
          bottom: 0,
          padding: [0, 50],
          data: echartsData.map(item => item.name),
          formatter: (name) => {
            return echarts.format.truncateText(name, 100, '10px Microsoft Yahei', '…')
          },
          tooltip: {
            show: true
          }
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: timeList
        },
        yAxis: {
          type: 'value',
          scale: true,
          axisLabel: {
            formatter: function (value) {
              return formatBytes(value)
            }
          }
        },
        series: echartsData
      }
      let infoItemObj = document.getElementById(this.id)
      var chart = echarts.init(infoItemObj)
      chart.clear()
      chart.setOption(chartOptions)
    }
  },
  mounted () {
    this.initCharts()
  },
  watch: {
    selectPrefixValue: function () {
      this.initCharts()
    }
  }
}
</script>
<style scoped>
.box-card {
  margin: 5px;
  height: 600px;
}

.chart {
  height: 500px;
  width: 100%;
}

.chart-no-data {
  height: 0 !important;
}
</style>
