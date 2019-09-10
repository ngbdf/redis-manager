<template>
  <el-container id="index">
    <el-header>
      <el-row type="flex" class="header-wrapper" justify="space-between">
        <el-col class="grid-content logo-wrapper">
          <i class="el-icon-s-fold aside-operation" @click="collapseHandler"></i>
          <span class="logo">REDIS MANAGER</span>
        </el-col>
        <el-col>
          <div class="grid-content right-content">
            <el-select v-model="value" placeholder="Select Group" size="mini" class="group-select">
              <el-option
                v-for="item in options"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              ></el-option>
            </el-select>
            <span class="links">
              <el-link :underline="false">Github</el-link>
              <el-divider direction="vertical"></el-divider>
              <el-link :underline="false">Document</el-link>
              <el-divider direction="vertical"></el-divider>
              <el-link :underline="false">Feedback</el-link>
            </span>
            <div class="user-info">
              <!-- <span class="user-name">Redis</span> -->
              <el-dropdown>
                <span>
                  <el-avatar :size="36" src="../assets/head.jpg" @error="errorHandler">
                    <img src="../assets/head.jpg" class="image-overflow" />
                  </el-avatar>
                </span>
                <el-dropdown-menu slot="dropdown" style="min-width: 180px">
                  <el-dropdown-item disabled>
                    Signed in as
                    <b>Redis</b>
                  </el-dropdown-item>
                  <el-dropdown-item divided>Profile</el-dropdown-item>
                  <el-dropdown-item>Help</el-dropdown-item>
                  <el-dropdown-item divided>Sign out</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
          </div>
        </el-col>
        <!-- <el-col :span="3" class="user-info">
          <div class="grid-content bg-purple"></div>
        </el-col>-->
      </el-row>
    </el-header>
    <el-container class="aside-main-wrapper">
      <el-aside :class="{'is-collapse':isCollapse}" style="width: 200px;">
        <el-row>
          <el-col :span="24">
            <el-menu
              default-active="1"
              @open="handleOpen"
              @close="handleClose"
              :collapse="isCollapse"
              :collapse-transition="false"
            >
              <el-menu-item index="1" @click="toDashboard()">
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
              <el-submenu index="4">
                <template slot="title">
                  <i class="el-icon-takeaway-box"></i>
                  <span>Tools</span>
                </template>
                <el-menu-item-group>
                  <el-menu-item index="4-1">Console</el-menu-item>
                  <el-menu-item index="4-2">Other</el-menu-item>
                </el-menu-item-group>
              </el-submenu>
              <el-menu-item index="5">
                <i class="el-icon-user"></i>
                <span slot="title">User Manage</span>
              </el-menu-item>
            </el-menu>
          </el-col>
        </el-row>
      </el-aside>
      <div
        class="keep-alive-wrapper"
        :class="{'main-margin':isCollapse}"
        style="margin-left: 200px;"
      >
        <div style="display: flex; align-items: center;min-height: 36px; padding-left: 20px;">
          <el-tag key="1" closable :disable-transitions="false" size="medium">Dashboard</el-tag>
        </div>
      </div>
      <el-main class="main" :class="{'main-margin':isCollapse}" style="margin-left: 200px;">
        <transition name="fade" mode="out-in">
          <router-view></router-view>
        </transition>
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
      value: "",
      isCollapse: false
    };
  },
  methods: {
    toDashboard() {
      this.$router.push({ name: "dashboard" });
    },
    handleOpen(key, keyPath) {
      console.log(key, keyPath);
    },
    handleClose(key, keyPath) {
      console.log(key, keyPath);
    },
    errorHandler() {
      return true;
    },
    collapseHandler() {
      this.isCollapse = !this.isCollapse;
    }
  },
  mounted() {
    this.$router.push({ name: "dashboard" });
  }
};
</script>

<style>
#index {
  height: 100%;
  min-width: 800px;
}

/*页面下拉，header固定不动*/
.el-header {
  position: fixed;
  width: 100%;
  z-index: 100;
  overflow: hidden;
  padding: 0;
  line-height: 40px;
  -webkit-box-shadow: 0 0 5px rgba(102, 102, 102, 0.05);
  -moz-box-shadow: 0 0 5px rgba(102, 102, 102, 0.05);
  box-shadow: 0 0 5px rgba(102, 102, 102, 0.05);
  border-bottom: 1px solid #dcdfe6;
  background-color: #ffffff;
}

.keep-alive-wrapper {
  position: fixed;
  width: 100%;
  z-index: 100;
  overflow: hidden;
  padding: 0;
}

.header-wrapper {
  padding: 10px 0;
  overflow: hidden;
}

.logo-wrapper {
  width: 200px;
  min-width: 200px;
  cursor: pointer;
  padding-left: 22px;
}

.aside-operation {
  font-size: 18px;
  margin-right: 5px;
}

.right-content {
  display: flex;
  justify-content: flex-end;
}

.links {
  padding: 0 1rem;
}

.group-select {
  margin: 0 1rem;
}

.user-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 0 20px;
}

.el-avatar {
  display: flex;
}

.image-overflow {
  flex: 1;
  overflow: hidden;
}

.aside-main-wrapper {
  margin-top: 60px;
}

.el-aside {
  position: fixed;
  height: 100%;
}

.main {
  background-color: #f0f2f5;
  min-width: 400px;
  margin-top: 36px;
}

.is-collapse {
  width: auto !important;
}

.main-margin {
  margin-left: 65px !important;
}

.grid-content {
  min-height: 40px;
}
</style>