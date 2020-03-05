<template>
  <el-container id="index">
    <el-header class="header header-wrapper">
      <div class="grid-content logo-wrapper">
        <i class="el-icon-s-fold aside-operation" @click="collapseHandler"></i>
        <img src="../assets/redis-manager.png" style="width: 150px; height: 15px" />
      </div>
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
          <el-button size="mini" type="text" @click="importVisible = true">Import Cluster</el-button>
        </div>

        <div class="links" id="links">
          <el-divider direction="vertical"></el-divider>
          <el-link
            :underline="false"
            href="https://github.com/ngbdf/redis-manager/"
            target="_blank"
          >
            <i class="el-icon-ali-git link-icon" title="Github"></i>
          </el-link>
          <el-divider direction="vertical"></el-divider>
          <el-link
            :underline="false"
            href="https://github.com/ngbdf/redis-manager/wiki"
            target="_blank"
          >
            <i class="el-icon-tickets link-icon" title="Document"></i>
          </el-link>
          <el-divider direction="vertical"></el-divider>
          <el-link
            :underline="false"
            href="https://github.com/ngbdf/redis-manager/issues/new"
            target="_blank"
          >
            <i class="el-icon-ali-feedback link-icon" title="Feedback"></i>
          </el-link>
        </div>
        <div class="user-info">
          <!-- <span class="user-name">Redis</span> -->
          <el-dropdown @command="handleCommand" class="user-dropdown">
            <el-image
              :src="currentUser.avatar"
              @error="errorHandler"
              fit="cover"
            >{{ currentUser.userName }}</el-image>
            <el-dropdown-menu slot="dropdown" style="min-width: 180px">
              <el-dropdown-item disabled>
                Signed in as
                <b>{{ currentUser.userName }}</b>
              </el-dropdown-item>
              <el-dropdown-item command="profile" divided>Profile</el-dropdown-item>
              <el-dropdown-item divided @click.native="signOut">Sign out</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
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
              <el-submenu index="2" v-if="currentUser.userRole < 2">
                <template slot="title">
                  <i class="el-icon-bell"></i>
                  <span>Alert Manage</span>
                </template>
                <el-menu-item-group>
                  <el-menu-item index="2-1" @click="toChannelManage()">Channel Manage</el-menu-item>
                  <el-menu-item index="2-2" @click="toRuleManage()">Rule Manage</el-menu-item>
                </el-menu-item-group>
              </el-submenu>
              <el-submenu index="3" v-if="currentUser.userRole < 2">
                <template slot="title">
                  <i class="el-icon-setting"></i>
                  <span slot="title">Installation</span>
                </template>
                <el-menu-item-group>
                  <el-menu-item index="3-1" @click="toInstallation()">Installation</el-menu-item>
                  <el-menu-item index="3-2" @click="toMachineManage()">Machine Manage</el-menu-item>
                </el-menu-item-group>
              </el-submenu>
               <el-submenu index="4" v-if="currentUser.userRole < 2">
                <template slot="title">
                  <i class="el-icon-cpu"></i>
                  <span>RCT</span>
                </template>
                <el-menu-item-group>
                  <el-menu-item index="4-1" @click="toRct()">Config</el-menu-item>
                </el-menu-item-group>
                <!-- <el-menu-item-group>
                  <el-menu-item index="9-2" @click="toTaskProgress()">TaskProgress</el-menu-item>
                </el-menu-item-group> -->
                <el-menu-item-group>
                  <el-menu-item index="4-2" @click="toJobList()">Job List</el-menu-item>
                </el-menu-item-group>
              </el-submenu>
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
              <el-menu-item index="8" @click="toEditHistory()" v-if="currentUser.userRole < 2">
                <!-- <i class="el-icon-odometer"></i> -->
                <i class="el-icon-edit"></i>
                <span slot="title">Edit History</span>
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
    <el-dialog
      title="Import Cluster"
      :visible.sync="importVisible"
      :close-on-click-modal="false"
      v-if="importVisible"
    >
      <editCluster @closeDialog="closeDialog"></editCluster>
    </el-dialog>
  </el-container>
</template>

<script>
var elementResizeDetectorMaker = require("element-resize-detector");
import { store } from "@/vuex/store.js";
import { isEmpty } from "@/utils/validate.js";
import { formatTime } from "@/utils/time.js";
import API from "@/api/api.js";
import editCluster from "@/components/manage/EditCluster";
import { getGroupList } from "@/components/group/group.js";
import message from "@/utils/message.js";
import { getUserIP } from "@/utils/ip.js";
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
    signOut() {
      let url = "/user/signOut";
      API.post(
        url,
        null,
        response => {
          this.$router.push({
            name: "login"
          });
        },
        err => {
          message.error(err);
        }
      );
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
      })
    },
    toJobList () {
      this.$router.push({
        name: 'jobList',
        params: { groupId: this.selectGroupId }
      })
    },
    toRct () {
      this.$router.push({
        name: 'Config'
      })
    },
    toTaskProgress () {
      this.$router.push({
        name: 'TaskProgress'
      })
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
    toEditHistory() {
      this.$router.push({
        name: "edit-history",
        params: { groupId: this.selectGroupId }
      });
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
      this.$router.push({
        name: "profile",
        params: { userId: this.currentUser.userId }
      });
    },
    toDataOperation() {
      this.$router.push({
        name: "data-operation",
        params: { groupId: this.selectGroupId }
      });
    },
    handleOpen(key, keyPath) {},
    handleClose(key, keyPath) {},
    errorHandler() {
      return true;
    },
    collapseHandler() {
      this.isCollapse = !this.isCollapse;
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
              group.time = formatTime(group.updateTime);
            });
            // TODO: 简化逻辑
            store.dispatch("setGroupList", groupList);
            let currentGroup = this.currentGroup;
            let user = store.getters.getUser;
            if (isEmpty(currentGroup) || isEmpty(currentGroup.groupId)) {
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
    },
    getUserFromSession() {
      let url = "/user/getUserFromSession";
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            store.dispatch("setUser", result.data);
          } else {
            this.signOut();
          }
        },
        err => {
          message.error("Auto get user failed.");
        }
      );
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
    this.getUserFromSession();
    this.getGroupList();
    let groupId = "";
    if (isEmpty(this.currentGroup) || isEmpty(this.currentGroup.groupId)) {
      groupId = this.currentUser.groupId;
    } else {
      groupId = this.currentGroup.groupId;
    }
    this.selectGroupId = groupId;
    if (this.$route.path == "/") {
      this.toDashboard();
    }
  },
  created() {
    getUserIP((ip) => {
      store.dispatch("setUserIp", ip);
    })
  }
};
</script>

<style>
#index {
  height: 100%;
}

/*页面下拉，header固定不动*/
.header {
  position: fixed;
  width: 100%;
  z-index: 100;
  overflow: hidden;
  padding: 0 !important;
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
  display: flex;
  justify-content: space-between;
}

.logo-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 180px;
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
  padding: 0 10px;
  display: flex;
  align-items: center;
}

.link-icon {
  font-size: 14px !important;
}

.group-select {
  margin: 0 1rem;
}

.user-info {
  margin: 0 20px;
}

.user-dropdown {
  display: flex !important;
  align-items: center;
}

.image-overflow {
  flex: 1;
  overflow: hidden;
}

.el-image {
  height: 36px;
  width: 36px;
  border-radius: 50%;
  display: flex;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 12px;
  white-space: nowrap;
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
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
