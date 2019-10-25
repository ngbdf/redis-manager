<template>
  <div id="user-manage" class="body-wrapper">
    <div class="operation-wrapper">
      <el-button size="mini" type="success" @click="createGroupVisible = true">Create</el-button>
    </div>
    <el-table :data="groupList">
      <el-table-column type="index" width="50"></el-table-column>
      <el-table-column property="groupName" label="Group Name"></el-table-column>
      <el-table-column property="groupInfo" label="Info"></el-table-column>
      <el-table-column property="time" label="Time"></el-table-column>
      <el-table-column label="Operation" width="150px;">
        <template slot-scope="scope">
          <el-button size="mini" type="primary" @click="editGroup(scope.$index, scope.row)">Edit</el-button>
          <!-- <el-button size="mini" type="danger" @click="deleteGroup(scope.$index, scope.row)">Delete</el-button> -->
        </template>
      </el-table-column>
    </el-table>
    <el-dialog
      title="Create Group"
      :visible.sync="createGroupVisible"
      :close-on-click-modal="false"
      @close="closeHandler()"
      v-loading="saveGroupLoading"
    >
      <el-form :model="group" ref="group" label-width="120px" size="small">
        <el-form-item label="Group Name" prop="groupName" :rules="rules.groupName">
          <el-input v-model="group.groupName" maxlength="50" show-word-limit></el-input>
        </el-form-item>
        <el-form-item label="Group Info" prop="groupInfo">
          <el-input v-model="group.groupInfo"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" type="primary" @click="saveGroup('group')">Confirm</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { store } from "@/vuex/store.js";
import { isEmpty } from "@/utils/validate.js";
import { getGroupList } from "@/components/group/group.js";
import API from "@/api/api.js";
import message from "@/utils/message.js";
export default {
  data() {
    var validateGroupName = (rule, value, callback) => {
      if (isEmpty(value) || isEmpty(value.trim())) {
        return callback(new Error("Please enter group name"));
      } else {
        let url = "/group/validateGroupName/" + value;
        API.get(
          url,
          null,
          response => {
            let code = response.data.code;
            let existGroup = response.data.data;
            let groupId = this.group.groupId;
            if (code != 0 && groupId != existGroup.groupId) {
              return callback(new Error(value + " has exist"));
            } else {
              callback();
            }
          },
          err => {
            return callback(new Error("Network error, " + err));
          }
        );
      }
    };
    return {
      createGroupVisible: false,
      group: {
        groupName: ""
      },
      rules: {
        groupName: [
          { required: true, validator: validateGroupName, trigger: "blur" }
        ]
      },
      saveGroupLoading: false
    };
  },
  methods: {
    saveGroup(group) {
      this.$refs[group].validate(valid => {
        if (valid) {
          this.saveGroupLoading = true;
          let user = store.getters.getUser;
          this.group.userId = user.userId;
          this.group.updateTime = new Date(this.group.updateTime);
          let url;
          if (isEmpty(this.group.groupId)) {
            // for grant
            this.group.groupId = user.groupId;
            url = "/group/addGroup";
          } else {
            url = "/group/updateGroup";
          }
          API.post(
            url,
            this.group,
            response => {
              if (response.data.code == 0) {
                this.createGroupVisible = false;
                this.group = {};
                getGroupList(user);
              } else {
                message.error("Save group failed");
              }
              this.saveGroupLoading = false;
            },
            err => {
              this.saveGroupLoading = false;
              message.error(err);
            }
          );
        }
      });
    },
    closeHandler() {
      this.group = {};
    },
    editGroup(index, row) {
      this.group = row;
      this.createGroupVisible = true;
    },
    deleteGroup(index, row) {}
  },
  computed: {
    groupList() {
      return store.getters.getGroupList;
    }
  },
  mounted() {}
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