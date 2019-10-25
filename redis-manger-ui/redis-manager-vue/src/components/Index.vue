<template>
  <el-container id="index">
    <el-header>
      <el-row type="flex" class="header-wrapper" justify="space-between">
        <el-col class="grid-content logo-wrapper">
          <i class="el-icon-s-fold aside-operation" @click="collapseHandler"></i>
          <!-- <span class="logo">REDIS MANAGER</span> -->
          <img src="../assets/logo1.png" style="width: 150px; height: 20px" />
        </el-col>
        <el-col>
          <div class="grid-content right-content" id="right-content">
            <el-select
              v-model="selectGroupId"
              placeholder="Select Group"
              size="mini"
              class="group-select"
              @change="selectGroup()"
            >
              <el-option
                v-for="group in groupList"
                :key="group.groupId"
                :label="group.groupName"
                :value="group.groupId"
              ></el-option>
            </el-select>
            <div v-if="permission">
              <el-divider direction="vertical"></el-divider>
              <el-button size="mini" type="text" @click="importVisible = true">Import Cluster</el-button>
            </div>

            <span class="links" id="links">
              <el-divider direction="vertical"></el-divider>
              <el-link :underline="false">
                <i class="el-icon-ali-git link-icon" title="Github"></i>
              </el-link>
              <el-divider direction="vertical"></el-divider>
              <el-link :underline="false">
                <i class="el-icon-tickets link-icon" title="Document"></i>
              </el-link>
              <el-divider direction="vertical"></el-divider>
              <el-link :underline="false">
                <i class="el-icon-ali-feedback link-icon" title="Feedback"></i>
              </el-link>
            </span>
            <div class="user-info">
              <!-- <span class="user-name">Redis</span> -->
              <el-dropdown @command="handleCommand">
                <span>
                  <el-avatar :size="36" src="../assets/head.jpg" @error="errorHandler">
                    <img src="../assets/head.jpg" class="image-overflow" />
                  </el-avatar>
                </span>
                <el-dropdown-menu slot="dropdown" style="min-width: 180px">
                  <el-dropdown-item disabled>
                    Signed in as
                    <b>{{ currentUser.userName }}</b>
                  </el-dropdown-item>
                  <el-dropdown-item command="profile" divided>Profile</el-dropdown-item>
                  <el-dropdown-item>Help</el-dropdown-item>
                  <el-dropdown-item divided @click.native="toLogin">Sign out</el-dropdown-item>
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
              background-color="#2a3542"
              text-color="#909399"
              active-text-color="#fff"
            >
              <!-- background-color="#2a3542"
              text-color="#909399"
              active-text-color="#fff"-->
              <el-menu-item index="1" @click="toDashboard()">
                <i class="el-icon-discover"></i>
                <span slot="title">Dashboard</span>
              </el-menu-item>
              <el-menu-item index="2" @click="toInstallation()" v-if="currentUser.userRole < 2">
                <i class="el-icon-setting"></i>
                <span slot="title">Installation</span>
              </el-menu-item>
              <el-submenu index="3" v-if="currentUser.userRole < 2">
                <template slot="title">
                  <i class="el-icon-bell"></i>
                  <span>Alert Manage</span>
                </template>
                <el-menu-item-group>
                  <el-menu-item index="3-1" @click="toChannelManage()">Channel Manage</el-menu-item>
                  <el-menu-item index="3-2" @click="toRuleManage()">Rule Manage</el-menu-item>
                </el-menu-item-group>
              </el-submenu>
              <el-menu-item index="4" @click="toMachineManage()" v-if="currentUser.userRole < 2">
                <i class="el-icon-monitor"></i>
                <span slot="title">Machine Manage</span>
              </el-menu-item>
              <el-submenu index="5">
                <template slot="title">
                  <i class="el-icon-takeaway-box"></i>
                  <span>Tools</span>
                </template>
                <el-menu-item-group>
                  <el-menu-item
                    index="5-1"
                    @click="toDataOperation()"
                    v-if="currentUser.userRole < 2"
                  >Data Operation</el-menu-item>
                  <!-- <el-menu-item index="5-2">Other</el-menu-item> -->
                </el-menu-item-group>
              </el-submenu>
              <el-menu-item index="6" @click="toGroupManage()" v-if="currentUser.userRole < 1">
                <i class="el-icon-user-solid"></i>
                <span slot="title">Group Manage</span>
              </el-menu-item>
              <el-menu-item index="7" @click="toUserManage()" v-if="currentUser.userRole < 2">
                <i class="el-icon-user"></i>
                <span slot="title">User Manage</span>
              </el-menu-item>
            </el-menu>
          </el-col>
        </el-row>
      </el-aside>
      <el-main class="main" :class="{'main-margin':isCollapse}" style="margin-left: 200px;">
        <transition name="fade" mode="out-in">
          <router-view v-if="active"></router-view>
        </transition>
      </el-main>
    </el-container>
    <el-dialog title="Import Cluster" :visible.sync="importVisible" :close-on-click-modal="false">
      <editCluster @closeDialog="closeDialog"></editCluster>
    </el-dialog>
  </el-container>
</template>

<script>
var elementResizeDetectorMaker = require("element-resize-detector");
import { store } from "@/vuex/store.js";
import { isEmpty } from "@/utils/validate.js";
import { formatTime } from "@/utils/time.js";
import CONSTANT from "@/utils/constant.js";
import API from "@/api/api.js";
import editCluster from "@/components/manage/EditCluster";
import { getGroupList } from "@/components/group/group.js";
import message from "@/utils/message.js";
export default {
  components: {
    editCluster
  },
  data() {
    return {
      isCollapse: false,
      active: true,
      permission: true,
      importVisible: false,
      selectGroupId: ""
    };
  },
  methods: {
    toLogin() {
      this.$router.push({
        name: "login"
      });
    },
    toDashboard() {
      this.$router.push({
        name: "dashboard",
        params: { groupId: this.selectGroupId }
      });
    },
    toInstallation() {
      this.$router.push({
        name: "installation",
        params: { groupId: this.selectGroupId }
      });
    },
    toChannelManage() {
      this.$router.push({
        name: "channel-manage",
        params: { groupId: this.selectGroupId }
      });
    },
    toRuleManage() {
      this.$router.push({
        name: "rule-manage",
        params: { groupId: this.selectGroupId }
      });
    },
    toMachineManage() {
      this.$router.push({
        name: "machine-manage",
        params: { groupId: this.selectGroupId }
      });
    },
    toGroupManage() {
      this.$router.push({ name: "group-manage" });
    },
    toUserManage() {
      this.$router.push({
        name: "user-manage",
        params: { groupId: this.selectGroupId }
      });
    },
    selectGroup() {
      if (!isEmpty(this.selectGroupId)) {
        this.groupList.forEach(group => {
          if (this.selectGroupId === group.groupId) {
            this.getUserRole(group);
          }
        });
        let currentPath = this.$route.path;
        if (
          currentPath === "/" ||
          currentPath.indexOf("redis-monitor") > 0 ||
          currentPath.indexOf("group-manage") > 0
        ) {
          this.toDashboard();
        }
      } else {
        message.error("Select group failed");
      }
    },
    getUserRole(group) {
      let url = "/user/getUserRole";
      let user = {
        userId: this.currentUser.userId,
        groupId: group.groupId
      };
      API.post(
        url,
        user,
        response => {
          let result = response.data;
          if (result.code == 0) {
            store.dispatch("setUserRole", result.data);
            store.dispatch("setCurrentGroup", group);
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    handleCommand(command) {
      if (command == "profile") {
        this.toUserProfile();
      }
    },
    toUserProfile() {
      this.$router.push({ name: "profile" });
    },
    toDataOperation() {
      this.$router.push({ name: "data-operation" });
    },
    handleOpen(key, keyPath) {},
    handleClose(key, keyPath) {},
    errorHandler() {
      return true;
    },
    collapseHandler() {
      this.isCollapse = !this.isCollapse;
    },
    // 监听顶栏宽度
    listenHeaderWidth() {
      var erd = elementResizeDetectorMaker();
      erd.listenTo(document.getElementById("right-content"), function(element) {
        var width = element.offsetWidth;
        var links = document.getElementById("links");
        if (width > 500) {
          links.style.display = "block";
        } else {
          links.style.display = "none";
        }
      });
    },
    getGroupList() {
      let user = store.getters.getUser;
      let url = "/group/getGroupList";
      API.post(
        url,
        user,
        response => {
          if (response.data.code == 0) {
            let groupList = response.data.data;
            groupList.forEach(group => {
              group.updateTime = formatTime(group.updateTime);
            });
            store.dispatch("setGroupList", groupList);
            let currentGroup = store.getters.getCurrentGroup;
            let user = store.getters.getUser;
            let isContainCurrentGroup = false;
            groupList.forEach(group => {
              if (currentGroup.groupId == group.groupId) {
                isContainCurrentGroup = true;
              }
            });
            if (
              isEmpty(currentGroup) ||
              isEmpty(currentGroup.groupId) ||
              user.groupId != currentGroup.groupId ||
              !isContainCurrentGroup
            ) {
              groupList.forEach(group => {
                if (group.groupId == user.groupId) {
                  this.selectGroupId = user.groupId;
                  store.dispatch("setCurrentGroup", group);
                  this.getUserRole(group);
                }
              });
            }
          } else {
            message.error("Get group list failed");
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    closeDialog(importVisible) {
      this.active = false;
      this.$nextTick(() => (this.active = true));
      this.importVisible = importVisible;
    }
  },
  computed: {
    currentUser() {
      return store.getters.getUser;
    },
    currentGroup() {
      return store.getters.getCurrentGroup;
    },
    groupList() {
      return store.getters.getGroupList;
    }
  },
  mounted() {
    this.listenHeaderWidth();
    this.getGroupList();
    let groupId = this.currentGroup.groupId;
    if (isEmpty(groupId)) {
      groupId = this.currentUser.groupId;
    }
    this.selectGroupId = groupId;
  }
};
</script>

<style>
#index {
  height: 100%;
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

.header-wrapper {
  padding: 10px 0;
  overflow: hidden;
}

.logo-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 200px;
  min-width: 200px;
  cursor: pointer;
  padding-left: 22px;
}

.aside-operation {
  font-size: 22px;
  margin-right: 5px;
}

.right-content {
  display: flex;
  justify-content: flex-end;
}

.links {
  padding-right: 1rem;
}

.link-icon {
  font-size: 16px;
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
  z-index: 100;
  overflow: hidden;
  background-color: #2a3542;
}

.main {
  background-color: #f0f2f5;
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
