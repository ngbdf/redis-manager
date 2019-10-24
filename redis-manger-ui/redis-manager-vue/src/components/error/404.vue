<template>
  <div class="page-container">
    <p class="code">404</p>
    <div>
      <el-link @click="toDashboard()">Back to home page</el-link>
    </div>
  </div>
</template>

<script>
import { store } from "@/vuex/store.js";
export default {
  methods: {
    toDashboard() {
      this.$router.push({
        name: "dashboard",
        params: { groupId: this.currentGroup.groupId }
      });
    }
  },
  computed: {
    // 监听group变化
    currentGroup() {
      return store.getters.getCurrentGroup;
    }
  }
};
</script>

<style>
/** <style lang="scss" scoped> */
.page-container {
  font-size: 32px;
  text-align: center;
  color: rgb(192, 204, 218);
}
.code {
  color: #007fff;
  font-size: 68px;
}
</style>