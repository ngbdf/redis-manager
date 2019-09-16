<template>
  <div class="body-wrapper">
    <div class="operation-wrapper">
      <el-button size="mini" type="success" @click="importMachineVisible = true">Import Machine</el-button>
    </div>
    <div>
      <el-table :data="machineList" style="width: 100%" center>
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column property="machineGroupName" label="Machine Group Name" width="200"></el-table-column>
        <el-table-column property="host" label="Host"></el-table-column>
        <el-table-column property="userName" label="User Name"></el-table-column>
        <el-table-column label="Password" align="center">
          <template slot-scope="scope">
            <el-popover trigger="hover" placement="top">
              <p>Password: {{ scope.row.password }}</p>
              <div slot="reference" class="name-wrapper">
                <el-tag size="small" type="info">******</el-tag>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column label="Token"  align="center">
          <template slot-scope="scope">
            <el-popover trigger="hover" placement="top">
              <p>Token: {{ scope.row.password }}</p>
              <div slot="reference" class="name-wrapper">
                <el-tag size="small" type="info">******</el-tag>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column property="machineInfo" label="Info"></el-table-column>
        <el-table-column property="updateTime" label="Time"></el-table-column>
        <el-table-column label="Operation" width="200px;">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="handleEdit(scope.$index, scope.row)">Edit</el-button>
            <el-button
              size="mini"
              type="danger"
              @click="handleDelete(scope.$index, scope.row)"
            >Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-dialog title="Create User" :visible.sync="importMachineVisible">
      <el-form :model="machines" label-width="120px">
        <el-form-item label="Machine Group">
          <el-select
            size="small"
            v-model="machines.machineGroupName"
            filterable
            allow-create
            default-first-option
            placeholder="Select machine group"
          >
            <el-option
              v-for="item in machineGroupNameList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
          <!-- <el-input size="small" v-model="machines.machineGroupName"></el-input> -->
        </el-form-item>
        <el-form-item label="User Name">
          <el-input size="small" v-model="machines.userName"></el-input>
        </el-form-item>
        <el-form-item label="Password" width="180">
          <el-input size="small" v-model="machines.password"></el-input>
        </el-form-item>
        <el-form-item label="Token" width="180">
          <el-input size="small" v-model="machines.token"></el-input>
        </el-form-item>
        <el-form-item
          v-for="(host, index) in machines.hostList"
          :label="'Host ' + (index + 1)"
          :key="host.key"
        >
          <el-input size="small" v-model="host.value">
            <el-button slot="append" @click.prevent="removeItem(host)" icon="el-icon-delete"></el-button>
          </el-input>
        </el-form-item>

        <el-form-item label="Cluster Info">
          <el-input size="small" v-model="machines.machineInfo"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" @click="addItem()">New Host</el-button>
        <el-button size="small" type="primary">Confirm</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
export default {
  data() {
    return {
      machineList: [
        {
          machineGroupName: "huadong",
          host: "192.168.53.26",
          userName: "root",
          password: "1234",
          token: "xGea2fg4kj4hjeino;u303asa",
          machineInfo: "info",
          updateTime: ""
        }
      ],
      machineGroupNameList: [
        {
          value: "E1",
          label: "E1"
        },
        {
          value: "E2",
          label: "E2"
        },
        {
          value: "E3",
          label: "E3"
        }
      ],
      importMachineVisible: false,
      machines: {
        hostList: [{ value: "" }]
      }
    };
  },
  methods: {
    handleEdit(index, row) {
      console.log(index, row);
    },
    handleDelete(index, row) {
      console.log(index, row);
    },
    removeItem(item) {
      if (this.machines.hostList.length == 1) {
        return;
      }
      var index = this.machines.hostList.indexOf(item);
      if (index !== -1) {
        this.machines.hostList.splice(index, 1);
      }
    },
    addItem() {
      if (this.machines.hostList.length >= 20) {
        return;
      }
      this.machines.hostList.push({
        value: "",
        key: Date.now()
      });
    }
  }
};
</script>
<style scoped>
.body-wrapper {
  min-width: 1000px;
}
.operation-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-bottom: 20px;
  border-bottom: 1px solid #dcdfe6;
}
</style>