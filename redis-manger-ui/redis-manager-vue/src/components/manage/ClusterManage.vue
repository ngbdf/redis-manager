<template>
  <div id="cluster-manage" class="body-wrapper">
    <div class="manage-header-wrapper">
      <div class="title-wrapper">
        <span>Shanghai online</span>
        <i class="el-icon-sunny health" title="Status"></i>
      </div>

      <div class="base-info-operation-wrapper">
        <span class="base-info-item">
          Master:
          <el-tag size="mini">4</el-tag>
        </span>
        <span class="base-info-item">
          Node:
          <el-tag size="mini">12</el-tag>
        </span>
        <span class="base-info-item">
          Type:
          <el-tag size="mini">Redis Manager</el-tag>
        </span>
        <span class="base-info-item">
          Environment:
          <el-tag size="mini">docker</el-tag>
        </span>
      </div>
    </div>

    <div class="nodes-wrapper">
      <div class="batch-operation-wrapper">
        <div class="batch-title">Batch Operation</div>
        <el-row>
          <el-button size="small" type="text" icon="el-icon-finished">Purge</el-button>
          <el-divider direction="vertical"></el-divider>
          <el-button size="small" type="text" icon="el-icon-zoom-out">Forget</el-button>
          <el-divider direction="vertical"></el-divider>
          <el-button size="small" type="text" icon="el-icon-video-play">Start</el-button>
          <el-button size="small" type="text" icon="el-icon-switch-button">Stop</el-button>
          <el-button size="small" type="text" icon="el-icon-refresh-left">Restart</el-button>
          <el-divider direction="vertical"></el-divider>
          <el-button size="small" type="text" icon="el-icon-circle-close">Delete</el-button>
          <el-divider direction="vertical"></el-divider>
          <el-button size="small" type="text" icon="el-icon-edit">Edit Config</el-button>
        </el-row>
      </div>
      <div class="table-wrapper">
        <el-table
          :data="redisNodeList"
          style="width: 100%; margin-bottom: 10px;"
          row-key="nodeId"
          size="medium"
          default-expand-all
          :row-class-name="tableRowClassName"
          @selection-change="handleSelectionChange"
          :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
        >
          <el-table-column type="selection" width="55px"></el-table-column>
          <!-- status 有多种 -->
          <el-table-column prop="status" label="Status" sortable width="120"></el-table-column>
          <el-table-column prop="role" label="Role" width="100px"></el-table-column>
          <el-table-column prop="address" label="Address" sortable width="200px"></el-table-column>
          <el-table-column label="Slot">
            <template slot-scope="scope" v-if="scope.row.slot">
              {{ scope.row.slot }}
              <el-tag
                size="small"
                class="pointer"
                v-if="scope.row.slotStatus == ''"
              >{{ scope.row.slotNumber }}</el-tag>
              <el-tag size="small" class="pointer" v-else type="warning">{{ scope.row.slotNumber }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="Meta" width="130px;">
            <el-tag size="small" class="pointer">Info</el-tag>
            <el-tag size="small" class="pointer">Config</el-tag>
          </el-table-column>
          <el-table-column prop="updateTime" label="time"></el-table-column>
          <el-table-column label="Operation" width="220px">
            <template slot="header">
              <el-input v-model="search" size="mini" placeholder="Search" />
            </template>
            <template slot-scope="scope">
              <el-dropdown size="mini" split-button type="warning">
                Cluster
                <el-dropdown-menu slot="dropdown" v-if="scope.row.role == 'master'">
                  <el-dropdown-item>Move Slot</el-dropdown-item>
                  <el-dropdown-item>Memory Purge</el-dropdown-item>
                </el-dropdown-menu>
                <el-dropdown-menu slot="dropdown" v-else>
                  <el-dropdown-item>Forget</el-dropdown-item>
                  <el-dropdown-item>BeSlave</el-dropdown-item>
                  <el-dropdown-item>Failover</el-dropdown-item>
                  <el-dropdown-item>Memory Purge</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
              <el-dropdown size="mini" split-button type="danger">
                Node
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item>Start</el-dropdown-item>
                  <el-dropdown-item>Stop</el-dropdown-item>
                  <el-dropdown-item>Restart</el-dropdown-item>
                  <el-dropdown-item>Delete</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      search: "",
      redisNodeList: [
        {
          nodeId: 1,
          status: "ok",
          role: "master",
          address: "192.16.15.2:8600",
          slot: "0-100",
          slotNumber: 100,
          slotStatus: "",
          updateTime: "2019-08-25"
        },
        {
          nodeId: 2,
          status: "ok",
          role: "master",
          address: "192.16.15.4:8600",
          slot: "101-200",
          slotNumber: 100,
          slotStatus: "",
          updateTime: "2019-08-25"
        },
        {
          nodeId: 3,
          status: "ok",
          role: "master",
          address: "192.16.15.5:8600",
          slot: "201-300",
          slotNumber: 100,
          slotStatus: "",
          updateTime: "2019-08-25"
        },
        {
          nodeId: 4,
          status: "ok",
          role: "master",
          address: "192.16.15.6:8600",
          slot: "301-5461",
          slotNumber: 5462,
          slotStatus: "warning",
          updateTime: "2019-08-25",
          children: [
            {
              nodeId: 5,
              status: "ok",
              role: "slave",
              address: "192.16.15.6:8601",
              updateTime: "2019-08-25"
            }
          ]
        }
      ]
    };
  },
  methods: {
    tableRowClassName({ row, rowIndex }) {
      if (rowIndex === 1) {
        return "warning-row";
      } else if (rowIndex === 3) {
        return "success-row";
      }
      return "";
    },
    handleSelectionChange(val) {},
    handleEdit(index, row) {
      console.log(index, row);
    },
    handleDelete(index, row) {
      console.log(index, row);
    },
    handleSlot(slot) {
      return "";
    }
  }
};
</script>

<style scoped>
#cluster-manage {
  min-width: 1000px;
}

.manage-header-wrapper {
  padding-bottom: 20px;
  border-bottom: 1px solid #dcdfe6;
}

.base-info-operation-wrapper {
  margin-top: 20px;
  display: flex;
  align-items: center;
}

.health {
  margin-left: 10px;
}

.base-info-item {
  margin-right: 20px;
}

.batch-title {
  margin-bottom: 10px;
  color: #909399;
  font-size: 14px;
}

.nodes-wrapper,
.table-wrapper {
  margin-top: 20px;
}

.el-table .warning-row {
  background: oldlace;
}

.el-table .success-row {
  background: #f0f9eb;
}
</style>