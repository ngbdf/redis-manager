<template>
  <el-container id="index">
    <el-header>
      <el-row type="flex" class="header-wrapper" justify="space-between">
        <el-col class="grid-content logo-wrapper">
          <i class="el-icon-s-fold aside-operation" @click="collapseHandler"></i>
          <span class="logo">REDIS MANAGER</span>
        </el-col>
        <el-col>
          <div class="grid-content right-content" id="right-content">
            <el-select v-model="value" placeholder="Select Group" size="mini" class="group-select">
              <el-option
                v-for="item in options"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              ></el-option>
            </el-select>
            <div>
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
              <el-menu-item index="2" @click="toInstallation()">
                <i class="el-icon-setting"></i>
                <span slot="title">Installation</span>
              </el-menu-item>
              <el-submenu index="3">
                <template slot="title">
                  <i class="el-icon-bell"></i>
                  <span>Alert Manage</span>
                </template>
                <el-menu-item-group>
                  <el-menu-item index="3-1" @click="toChannelManage()">Channel Manage</el-menu-item>
                  <el-menu-item index="3-2" @click="toRuleManage()">Rule Manage</el-menu-item>
                </el-menu-item-group>
              </el-submenu>
              <el-submenu index="4">
                <template slot="title">
                  <i class="el-icon-takeaway-box"></i>
                  <span>Tools</span>
                </template>
                <el-menu-item-group>
                  <el-menu-item index="4-1" @click="toDataOperation()">Data Operation</el-menu-item>
                  <!-- <el-menu-item index="4-2">Other</el-menu-item> -->
                </el-menu-item-group>
              </el-submenu>
              <el-menu-item index="5" @click="toGroupManage()">
                <i class="el-icon-user"></i>
                <span slot="title">Group Manage</span>
              </el-menu-item>
              <el-menu-item index="6" @click="toUserManage()">
                <i class="el-icon-user"></i>
                <span slot="title">User Manage</span>
              </el-menu-item>
            </el-menu>
          </el-col>
        </el-row>
      </el-aside>
      <el-main class="main" :class="{'main-margin':isCollapse}" style="margin-left: 200px;">
        <transition name="fade" mode="out-in">
          <router-view></router-view>
        </transition>
      </el-main>
    </el-container>
    <el-dialog title="Import Cluster" :visible.sync="importVisible">
      <el-form :model="cluster">
        <el-form-item label="Group Name" label-width="120px">
          <el-tag size="small">Bigdata</el-tag>
        </el-form-item>
        <el-form-item label="Cluster Name" label-width="120px">
          <el-input size="small" v-model="cluster.clusterName"></el-input>
        </el-form-item>
        <el-form-item label="Redis Password" label-width="120px">
          <el-input size="small" v-model="cluster.redisPassword"></el-input>
        </el-form-item>
        <el-form-item
          v-for="(node, index) in cluster.nodeList"
          :label="'Node ' + index"
          :key="node.key"
          label-width="120px"
        >
          <el-input size="small" v-model="node.value">
            <el-button slot="append" @click.prevent="removeNode(node)" icon="el-icon-delete"></el-button>
          </el-input>
        </el-form-item>

        <el-form-item label="Environment" label-width="120px">
          <el-radio-group v-model="cluster.installationEnvironment">
            <el-radio label="docker">Docker</el-radio>
            <el-radio label="machine">Machine</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="Cluster Info" label-width="120px">
          <el-input size="small" v-model="cluster.clusterInfo"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" @click="addNode()">New Node</el-button>
        <el-button size="small" type="primary">Confirm</el-button>
      </div>
    </el-dialog>
  </el-container>
</template>

<script>
var elementResizeDetectorMaker = require("element-resize-detector");
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
      isCollapse: false,
      importVisible: false,
      cluster: {
        clusterName: "",
        nodeList: [{ value: "" }]
      }
    };
  },
  methods: {
    toDashboard() {
      this.$router.push({ name: "dashboard" });
    },
    toInstallation() {
      this.$router.push({ name: "installation" });
    },
    toChannelManage() {
      this.$router.push({ name: "channel-manage" });
    },
    toRuleManage() {
      this.$router.push({ name: "rule-manage" });
    },
    toGroupManage() {
      this.$router.push({ name: "group-manage" });
    },
    toUserManage() {
      this.$router.push({ name: "user-manage" });
    },
    toDataOperation() {
      this.$router.push({ name: "data-operation" });
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
    removeNode(item) {
      var index = this.cluster.nodeList.indexOf(item);
      if (index !== -1) {
        this.cluster.nodeList.splice(index, 1);
      }
    },
    addNode() {
      if (this.cluster.nodeList.length >= 5) {
        return;
      }
      this.cluster.nodeList.push({
        value: "",
        key: Date.now()
      });
    }
  },
  mounted() {
    this.listenHeaderWidth();
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
  background-color: #ffffff;
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