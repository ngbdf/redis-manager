<template>
  <el-col :xl="12" :lg="12" :md="24" :sm="24" class="chart-item">
    <el-card shadow="hover" class="box-card">
      <highcharts :options="chartOptions" :callback="myCallback"></highcharts>
    </el-card>
  </el-col>
</template>
<script>
import { getPieByType } from '@/api/rctapi.js'
import { Chart } from 'highcharts-vue'
import Highcharts from 'highcharts'
import { formatBytes, formatterInput } from '@/utils/format.js'

export default {
  components: {
    highcharts: Chart
  },
  props: {
    pieType: {
      type: String
    },
    resultId: {
      type: String
    }
  },
  data () {
    return {
      echartsData: [],
      chartOptions: {}
    }
  },
  methods: {
    myCallback () {
    },
    async initCharts () {
      const res = await getPieByType(this.resultId)
      const type = this.pieType
      this.echartsData = res.data.map(value => {
        return {
          name: value.dataType,
          y: this.pieType === 'count' ? parseInt(value['itemCount']) : parseInt(value['bytes'])
        }
      })
      this.chartOptions = {
        chart: {
          height: 300,
          plotBackgroundColor: null,
          plotBorderWidth: null,
          plotShadow: false,
          type: 'pie'
        },
        title: {

          text: this.pieType === 'count' ? 'Key Count By Type' : 'Key Memory By Type'
        },
        credits: {
          enabled: false
        },
        tooltip: {
          shadow: true,
          animation: true,
          formatter () {
            if (type === 'count') {
              return ` ${this.point.name}:<b>${Highcharts.numberFormat(this.point.percentage, 1)}%</b><br/>count:${formatterInput(this.point.y)}`
            } else {
              return ` ${this.point.name}:<b>${Highcharts.numberFormat(this.point.percentage, 1)}%</b><br/>memory:${formatBytes(this.point.y)}`
            }
          }
        },
        plotOptions: {
          pie: {
            allowPointSelect: true,
            cursor: 'pointer',
            colors: ['#18BFA6', '#84BF18'],
            dataLabels: {
              enabled: true,
              format: '<b>{point.name}</b>: {point.percentage:.1f} %',
              style: {
                color:
                        (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
              }
            }
          }
        },
        series: [
          {
            colorByPoint: true,
            data: this.echartsData
          }
        ]
      }
    }
  },
  watch: {
    pieType: {
      immediate: true,
      handler (newValue, old) {
        this.$nextTick(() => {
          if (newValue !== old) {
            this.pieType = newValue
          }
        }
        )
      }
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
