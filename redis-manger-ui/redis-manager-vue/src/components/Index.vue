<template>
  <el-container id="index">
    <el-header class="header">
      <el-row type="flex" class="header-wrapper" justify="space-between">
        <el-col class="logo-wrapper">
          <div class="grid-content bg-purple logo">REDIS MANAGER</div>
        </el-col>
        <el-col>
          <div class="grid-content bg-purple-light right-content">
            <el-select v-model="value" placeholder="Select Group" size="small">
              <el-option
                v-for="item in options"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              ></el-option>
            </el-select>

            <div class="user-info">
              <el-avatar :size="36" src="../assets/logo.png" @error="errorHandler">
                <img src="../assets/logo.png" />
              </el-avatar>
            </div>
          </div>
        </el-col>
        <!-- <el-col :span="3" class="user-info">
          <div class="grid-content bg-purple"></div>
        </el-col>-->
      </el-row>
    </el-header>
    <el-container class="aside-main-wrapper">
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
  data() {
    return {
      options: [
        {
          value: "Bigdata",
          label: "Bigdata"
        },
        {
          value: "Test",
          label: "Test"
        }
      ],
      value: ""
    };
  },
  methods: {
    handleOpen(key, keyPath) {
      console.log(key, keyPath);
    },
    handleClose(key, keyPath) {
      console.log(key, keyPath);
    },
    errorHandler() {
      return true;
    }
  }
};
</script>

<style>
#index {
  height: 100%;
}

.header {
  padding: 0;
  line-height: 40px;
}
.header-wrapper {
  padding: 10px 0;
  min-width: 600px;
}

.logo-wrapper {
  width: 200px;
  min-width: 200px;
}

.logo {
  padding-left: 20px;
}

.right-content {
  display: flex;
  justify-content: flex-end;
}

.user-info {
  display: flex;
  align-items: center;
  margin: 0 20px;
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

.main {
  background-color: #f1f2f7;
  min-width: 400px;
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
  min-height: 40px;
}

.row-bg {
  background-color: #f9fafc;
}
</style>