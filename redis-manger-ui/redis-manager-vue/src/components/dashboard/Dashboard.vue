<template>
  <div>
    <el-row :gutter="24" class="card-panel-group">
      <el-col :xl="6" :lg="12" :md="12" :sm="12">
        <div class="card-panel">
          <div class="card-panel-icon-wrapper card-panel-icon-user">
            <i class="el-icon-user"></i>
          </div>

          <div class="card-panel-info-wrapper">
            <div class="card-panel-info-key">User Number</div>
            <div class="card-panel-info-value">6</div>
          </div>
        </div>
      </el-col>
      <el-col :xl="6" :lg="12" :md="12" :sm="12">
        <div class="card-panel">
          <div class="card-panel-icon-wrapper card-panel-icon-health">
            <i class="el-icon-sunny"></i>
          </div>
          <div class="card-panel-info-wrapper">
            <div class="card-panel-info-key">Health Number</div>
            <div class="card-panel-info-value">23</div>
          </div>
        </div>
      </el-col>
      <el-col :xl="6" :lg="12" :md="12" :sm="12">
        <div class="card-panel">
          <div class="card-panel-icon-wrapper card-panel-icon-bad">
            <i class="el-icon-heavy-rain"></i>
          </div>

          <div class="card-panel-info-wrapper">
            <div class="card-panel-info-key">Bad Number</div>
            <div class="card-panel-info-value">1</div>
          </div>
        </div>
      </el-col>
      <el-col :xl="6" :lg="12" :md="12" :sm="12">
        <div class="card-panel">
          <div class="card-panel-icon-wrapper card-panel-icon-other">
            <i class="el-icon-bell"></i>
          </div>

          <div class="card-panel-info-wrapper">
            <div class="card-panel-info-key">Other Number</div>
            <div class="card-panel-info-value">18</div>
          </div>
        </div>
      </el-col>
    </el-row>
    <el-row>
      <el-row :gutter="24" id="cluster-list-wrapper">
        <el-col :xl="4" :lg="6" :md="8" :sm="12">
          <div class="redis-card-wrapper">
              
          </div>
        </el-col>
      </el-row>
    </el-row>
  </div>
</template>

<script>
export default {};
</script>

<style scoped>
.card-panel {
  margin-bottom: 1.5rem;
  padding: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 4px;
  min-height: 36px;
  background-color: #ffffff;
}

.card-panel-icon-wrapper {
  font-size: 3rem;
  width: 5rem;
  height: 5rem;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  border-radius: 4px;
  /* 背景色变化动画 */
  transition: all 0.38s ease-out;
}


.card-panel-icon-user {
  color: #36a3f7;
}

.card-panel-icon-user:hover {
  background-color: #36a3f7;
  color: #ffffff;
}

.card-panel-icon-health {
  color: #40c9c6;
}

.card-panel-icon-health:hover {
  background-color: #40c9c6;
  color: #ffffff;
}

.card-panel-icon-bad {
  color: #f4516c;
}

.card-panel-icon-bad:hover {
  background-color: #f4516c;
  color: #ffffff;
}

.card-panel-icon-other {
  color: #ffb980;
}

.card-panel-icon-other:hover {
  background-color: #ffb980;
  color: #ffffff;
}


.card-panel-info-wrapper {
  padding: 0 0.5rem;
  font-weight: bold;
  text-align: center;
}

.card-panel-info-key {
  color: #8c8c8c;
  padding: 4px 0;
  font-size: 14px;
}

.card-panel-info-value {
  color: #666;
  padding: 4px 0;
  font-size: 20px;
}

.redis-card-wrapper {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1)
}
.el-row {
  margin-bottom: 20px;
}
.el-col {
  border-radius: 4px;
}
</style>