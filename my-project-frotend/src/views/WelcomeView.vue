<script setup>

</script>
<template>
<div style="width: 100vw;height: 100vh;display: flex;overflow: hidden">
<!--  流式布局-->
 <div style="flex: 1">
<el-image style="width: 100%;height: 100%" fit="cover"
    src="https://gss0.baidu.com/9fo3dSag_xI4khGko9WTAnF6hhy/zhidao/wh%3D600%2C800/sign=ccc17af090529822056631c5e7fa57f3/0b55b319ebc4b745c94bf121cffc1e178a82152d.jpg"/>
 </div>

  <div class="welcome-title">
    <div style="font-size: 30px;font-weight: bold">欢迎来到校园论坛</div>
    <div style="margin-top: 10px">在这里你可以学习如何使用Java，搭建网站</div>
  </div>
<!--  定长-->
  <div class="right-card">
    <router-view v-slot="{ Component }">
      <transition name="el-fade-in-linear" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </div>

</div>

</template>
<style scoped>
.right-card{
  width: 600px;
  z-index: 1;
  background-color: var(--el-bg-color);
}

/*用绝对布局否则会独占一片区域*/
.welcome-title{
  position: absolute;
  color: white;
  text-shadow: 0 0 10px black;
  bottom: 40px;
  left: 40px;
}
</style>