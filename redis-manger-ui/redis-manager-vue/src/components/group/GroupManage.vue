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
    >
      <el-form :model="group" ref="group" label-width="120px">
        <el-form-item label="Group Name" prop="groupName" :rules="rules.groupName">
          <el-input size="small" v-model="group.groupName" maxlength="50" show-word-limit></el-input>
        </el-form-item>
        <el-form-item label="Group Info" prop="groupInfo">
          <el-input size="small" v-model="group.groupInfo"></el-input>
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
import { formatTime } from "@/utils/time.js";
import API from "@/api/api.js";
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
           let code =  response.data.code
           let existGroup = response.data.data;
           let groupId = this.group.groupId
            if (code != 0 && groupId != existGroup.groupId) {
              return  callback(new Error(value + " has exist"));
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
      groupList: [],
      createGroupVisible: false,
      group: {
        groupName: ""
      },
      rules: {
        groupName: [
          { required: true, validator: validateGroupName, trigger: "blur" }
        ]
      }
    };
  },
  methods: {
    getGroupList() {
      let userId = store.getters.getUserId;
      let url = "/group/getGroupList/" + userId;
      API.get(
        url,
        null,
        response => {
          if (response.data.code == 0) {
            this.groupList = response.data.data;
            this.groupList.forEach(group => {
              group.updateTime = formatTime(group.updateTime)
            });
          } else {
            this.groupList = [
              {
                groupName: "Static",
                groupInfo: "Static",
                updateTime: "2019-08-25"
              }
            ];
            console.log("No data");
          }
        },
        err => {
          console.log(err);
        }
      );
    },
    saveGroup(group) {
      this.$refs[group].validate(valid => {
        if (valid) {
          this.group.userId = store.getters.getUserId;
          let url;
          if(isEmpty(this.group.groupId)) {
            url =  "/group/addGroup"
          } else {
            url = "/group/updateGroup"
          }
          API.post(
            url,
            this.group,
            response => {
              if (response.data.code == 0) {
                this.createGroupVisible = false;
                this.group = {};
                this.getGroupList();
              } else {
                console.log("Add group failed.");
              }
            },
            err => {
              console.log("Network error.");
            }
          );
        }
      });
    },
    editGroup(index, row) {
      this.group = row
      this.createGroupVisible = true;
      console.log(index, row);
    },
    deleteGroup(index, row) {
      console.log(index, row);
    }
  },
  mounted() {
    this.getGroupList();
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