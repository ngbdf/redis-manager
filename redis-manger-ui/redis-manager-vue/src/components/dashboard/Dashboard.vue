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
          <div class="card-panel-icon-wrapper card-panel-icon-alert">
            <i class="el-icon-bell"></i>
          </div>

          <div class="card-panel-info-wrapper">
            <div class="card-panel-info-key">Alertz Number</div>
            <div class="card-panel-info-value">18</div>
          </div>
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="24" id="cluster-list-wrapper">
      <el-col :xl="6" :lg="8" :md="12" :sm="12">
        <el-card class="box-card">
          <div slot="header" class="clearfix box-card-title">
            <span>Shanghai</span>
            <i class="el-icon-sunny health" style="float: right; padding: 3px 0"></i>
          </div>
          <div class="text item">
            State:
            <b>ok</b>
          </div>
          <div class="text item">
            Master:
            <b>4</b>
          </div>
          <div class="text item">
            Nodes:
            <b>12</b>
          </div>
          <div class="text item">
            Version:
            <b>4.0.14</b>
          </div>
          <div class="text item">
            Model:
            <b>cluster</b>
          </div>
          <div class="text item">
            Slots Assigned(ok/all):
            <b>16384/16384</b>
          </div>
          <div class="text item">
            Install:
            <b>docker</b>
          </div>
          <div class="card-bottom">
            <!-- <el-button type="text" class="button">
              <i class="el-icon-view"></i> Monitor
            </el-button>
            <el-button type="text" class="button">
              <i class="el-icon-search"></i> Query
            </el-button>
            <el-button type="text" class="button">
              <i class="el-icon-setting"></i> Manage
            </el-button>
            <el-button type="text" class="button">
              <i class="el-icon-bell"></i> Alert
            </el-button>-->
            <el-button size="mini" title="Query" icon="el-icon-search" circle></el-button>
            <el-button
              size="mini"
              title="Monitor"
              type="primary"
              icon="el-icon-view"
              circle
              @click="monitorHandler(1)"
            ></el-button>
            <el-button size="mini" title="Alert" type="warning" icon="el-icon-bell" circle></el-button>
            <el-button size="mini" title="Manage" type="danger" icon="el-icon-setting" circle></el-button>

            <el-dropdown trigger="click" class="more-operation">
              <el-button size="mini" title="Edit or delete" type="info" icon="el-icon-more" circle></el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item icon="el-icon-edit-outline" class="edit">Edit</el-dropdown-item>
                <el-dropdown-item icon="el-icon-delete" class="delete">Delete</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  methods: {
    monitorHandler(clusterId) {
      this.$router.push({ name: "monitor" });
    }
  }
};
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

.card-panel-icon-health,
.health {
  color: #40c9c6;
}

.health,
.bad {
  font-weight: bold;
}

.card-panel-icon-health:hover {
  background-color: #40c9c6;
  color: #ffffff;
}

.card-panel-icon-bad,
.bad {
  color: #f4516c;
}

.card-panel-icon-bad:hover {
  background-color: #f4516c;
  color: #ffffff;
}

.card-panel-icon-alert {
  color: #ffb980;
}

.card-panel-icon-alert:hover {
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

/* cluster card */
.text {
  font-size: 13px;
}

.item {
  margin-bottom: 16px;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}

.clearfix:after {
  clear: both;
}

.box-card {
  margin-bottom: 1.5rem;
}

.box-card-title {
  font-weight: bold;
}

.card-bottom {
  display: flex;
  justify-content: flex-end;
}

.more-operation {
  margin-left: 12px;
}

.edit {
  color: #409eff;
}

.delete {
  color: #f56c6c;
}
.delete:hover {
  background-color: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.el-row {
  margin-bottom: 20px;
}
.el-col {
  border-radius: 4px;
}
</style>