<template>
  <div id="user-manage" class="body-wrapper">
    <div class="operation-wrapper">
      <el-button size="mini" type="success" @click="createGroupVisible = true">Create</el-button>
    </div>
    <el-table :data="groupList">
      <el-table-column type="index" width="50"></el-table-column>
      <el-table-column property="groupName" label="Group Name"></el-table-column>
      <el-table-column property="groupInfo" label="Info"></el-table-column>
      <el-table-column property="updateTime" label="Time"></el-table-column>
      <el-table-column label="Operation" width="200px;">
        <template slot-scope="scope">
          <el-button size="mini" type="primary" @click="handleEdit(scope.$index, scope.row)">Edit</el-button>
          <el-button size="mini" type="danger" @click="handleDelete(scope.$index, scope.row)">Delete</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog title="Create Group" :visible.sync="createGroupVisible">
      <el-form :model="group">
        <el-form-item label="Group Name" label-width="100px">
          <el-input size="small" v-model="group.groupName"></el-input>
        </el-form-item>
        <el-form-item label="Group Info" label-width="100px">
          <el-input size="small" v-model="group.groupInfo"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" type="primary">Confirm</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      groupList: [
        {
          groupName: "bigdata",
          groupInfo: "test",
          updateTime: "2019-08-25"
        }
      ],
      createGroupVisible: false,
      group: {}
    };
  },
  methods: {
    handleEdit(index, row) {
      console.log(index, row);
    },
    handleDelete(index, row) {
      console.log(index, row);
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