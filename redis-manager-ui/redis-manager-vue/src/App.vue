<template>
  <div id="app">
    <transition name="fade" mode="out-in">
      <router-view></router-view>
    </transition>
  </div>
</template>

<script>
export default {
  name: "App"
};
</script>

<style>
html,
body {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
}

.body-wrapper {
  padding: 20px;
  background-color: #ffffff;
  border-radius: 4px;
}

.el-textarea__inner,
.el-textarea__inner::-webkit-input-placeholder {
  font-family: "Microsoft YaHei", sans-serif, "微软雅黑", Arial,
    "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB" !important;
}

#app {
  font-family: "Microsoft YaHei", sans-serif, "微软雅黑", Arial,
    "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB";
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  height: 100%;
  width: 100%;
}
img {
  border: none;
}
a {
  text-decoration: none;
  color: #333333;
}
a:hover {
  text-decoration: none;
  color: #a43130;
}
li {
  list-style: none;
}

.health {
  color: #40c9c6;
}
.bad {
  color: #f4516c;
}

.health,
.bad {
  font-weight: bold;
}

.pointer {
  cursor: pointer;
}

.title-wrapper {
  font-size: 18px;
  padding-bottom: 20px;
  margin-bottom: 20px;
  border-bottom: 1px solid #dcdfe6;
}

</style>
