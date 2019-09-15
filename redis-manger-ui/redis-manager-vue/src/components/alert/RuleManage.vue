<template>
  <div id="rule-manage" class="body-wrapper">
    <div class="header-wrapper">
      <div>Bigdata</div>
      <div>
        <el-button size="mini" type="success" @click="createVisible = true">Create</el-button>
      </div>
    </div>
    <div>
      <el-table :data="ruleList">
        <el-table-column type="index" width="50"></el-table-column>
        <el-table-column label="Alert Rule" width="200px;">
          <template
            slot-scope="scope"
          >{{scope.row.alertKey}}{{scope.row.compareType}}{{scope.row.alertValue}}</template>
        </el-table-column>
        <el-table-column label="Rule Status" width="100px;">
          <template slot-scope="scope">
            <el-tag size="small" type="success" v-if="scope.row.valid">valid</el-tag>
            <el-tag size="small" type="danger" v-else>Invalid</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Is Global" width="100px;">
          <template slot-scope="scope">
            <el-tag size="small" v-if="scope.row.global">Global</el-tag>
          </template>
        </el-table-column>

        <el-table-column property="checkCycle" label="Check Cycle"></el-table-column>
        <el-table-column property="ruleInfo" label="Info"></el-table-column>
        <el-table-column property="updateTime" label="Time"></el-table-column>
        <el-table-column label="Operation" width="250px;">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleView(scope.$index, scope.row)">View</el-button>
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

    <!-- dialog: create rule-->
    <el-dialog
      title="Create Rule"
      :visible.sync="createVisible"
      width="40%"
      :close-on-click-modal="false"
    >
      <el-form :model="alertRule">
        <el-form-item label="Rule Key" label-width="120px">
          <el-select size="small" v-model="alertRule.ruleKey" placeholder="Select rule key">
            <el-option
              v-for="item in ruleKeyList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Compare Type" label-width="120px">
          <el-select size="small" v-model="alertRule.compareType" placeholder="Select compare type">
            <el-option
              v-for="item in compareTypeList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Rule Value" label-width="120px">
          <el-input size="small" v-model="alertRule.ruleValue" placeholder="Please enter rule value"></el-input>
        </el-form-item>
        <el-form-item label="Check Cycle" label-width="120px">
          <el-select size="small" v-model="alertRule.checkCycle" placeholder="Select check circle">
            <el-option
              v-for="item in checkCycleList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Is Valid" label-width="120px">
          <el-switch v-model="alertRule.valid"></el-switch>
        </el-form-item>
        <el-form-item label="Is Global" label-width="120px">
          <el-switch v-model="alertRule.global"></el-switch>
        </el-form-item>
        <el-form-item label="Rule Info" label-width="120px">
          <el-input size="small" v-model="alertRule.ruleInfo" placeholder="Please enter rule info"></el-input>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button size="small" @click="createVisible = false">Cancel</el-button>
        <el-button size="small" type="primary" @click="dialogFormVisible = false">Confirm</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      ruleList: [
        {
          alertKey: "test",
          alertValue: "12",
          compareType: ">",
          checkCycle: "5",
          valid: true,
          global: true,
          ruleInfo: "test",
          updateTime: "2019-08-25"
        }
      ],
      createVisible: false,
      alertRule: {
          valid: true,
          global: false
      },
      ruleKeyList: [
        {
          value: "test",
          label: "test"
        }
      ],
      compareTypeList: [
        {
          value: ">",
          label: ">"
        }
      ],
      checkCycleList: [
        {
          value: "5",
          label: "5min"
        }
      ]
    };
  },
  methods: {
    handleView(index, row) {
      console.log(index, row);
    },
    handleEdit(index, row) {
      console.log(index, row);
    },
    handleDelete(index, row) {
      console.log(index, row);
    },
    handleClick() {}
  }
};
</script>

<style scoped>
.body-wrapper {
  min-width: 1000px;
}
.header-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 1px solid #dcdfe6;
}
</style>