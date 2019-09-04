<template>
  <el-container id="index">
    <el-header class="header">
      <el-row type="flex" class="header-wrapper" justify="space-between">
        <el-col :span="3">
          <div class="grid-content bg-purple"></div>
        </el-col>
        <el-col :span="18">
          <div class="grid-content bg-purple-light"></div>
        </el-col>
        <el-col :span="3">
          <div class="grid-content bg-purple"></div>
        </el-col>
      </el-row>
    </el-header>
    <el-container class>
      <el-aside width="200px">
        <el-row class="tac">
          <el-col :span="24">
            <!-- <h5>默认颜色</h5> -->
            <el-menu
              default-active="1"
              class="el-menu-vertical-demo"
              @open="handleOpen"
              @close="handleClose"
            >
              <el-menu-item index="1">
                <i class="el-icon-discover"></i>
                <span slot="title">Dashboard</span>
              </el-menu-item>

              <el-submenu index="2">
                <template slot="title">
                  <i class="el-icon-setting"></i>
                  <span>Install</span>
                </template>
                <el-menu-item-group>
                  <el-menu-item index="2-1">Docker Install</el-menu-item>
                  <el-menu-item index="2-2">Machine Install</el-menu-item>
                  <el-menu-item index="2-3">Kubernetes Install</el-menu-item>
                </el-menu-item-group>
              </el-submenu>
              <el-submenu index="3">
                <template slot="title">
                  <i class="el-icon-bell"></i>
                  <span>Alert Manage</span>
                </template>
                <el-menu-item-group>
                  <el-menu-item index="3-1">Channel Manage</el-menu-item>
                  <el-menu-item index="3-2">Rule Manage</el-menu-item>
                </el-menu-item-group>
              </el-submenu>
              <el-menu-item index="4">
                <i class="el-icon-user"></i>
                <span slot="title">User Manage</span>
              </el-menu-item>
            </el-menu>
          </el-col>
        </el-row>
      </el-aside>
      <el-main class="main">
        <div></div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
export default {
  methods: {
    handleOpen(key, keyPath) {
      console.log(key, keyPath);
    },
    handleClose(key, keyPath) {
      console.log(key, keyPath);
    }
  }
};
</script>

<style>
#index {
  height: 100%;
}

.header-wrapper {
  padding: 10px 0;
}
/*页面下拉，header固定不动*/
.header {
  /* position: fixed; */
  width: 100%;
  /* text-align: center;
  z-index: 100;
  font-size: 16px;
  overflow: hidden; */
  -webkit-box-shadow: 0 0 5px rgba(102, 102, 102, 0.05);
  -moz-box-shadow: 0 0 5px rgba(102, 102, 102, 0.05);
  box-shadow: 0 0 5px rgba(102, 102, 102, 0.05);
  border-bottom: 1px solid #dcdfe6;
}

.aside-main,
.aside {
}

.main {
  background-color: #f1f2f7;
}

.el-row {
  margin-bottom: 20px;
  &:last-child {
    margin-bottom: 0;
  }
}
.el-col {
  border-radius: 4px;
}
.bg-purple-dark {
  background: #99a9bf;
}
.bg-purple {
  background: #d3dce6;
}
.bg-purple-light {
  background: #e5e9f2;
}
.grid-content {
  border-radius: 4px;
  min-height: 36px;
}
.row-bg {
  background-color: #f9fafc;
}
</style>