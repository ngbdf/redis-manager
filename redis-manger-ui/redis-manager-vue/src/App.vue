<template>
  <div id="app">
    <router-view />
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
</style>
