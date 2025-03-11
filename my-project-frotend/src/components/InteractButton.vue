<script setup>
defineProps({
  name: String,
  color: String,
  check: Boolean,
  checkName: String
})

const emit = defineEmits( ['check'] )
</script>

<template>
  <div class="interact-button" >
    <span class="icon"  :style="{'color': check ? color : 'unset' }"  @click="emit('check' )">
      <slot></slot>
    </span>
    <span class="name"  :style="{'color':  color  }">
      {{ check ? checkName : name }}
    </span>
  </div>
</template>

<style lang="less" scoped>
.interact-button {
  display: inline-block;
  height: 20px;
}

.name {
  font-size: 13px;
  margin-left: 5px;
  opacity: 0.7;
}

.icon {
  vertical-align: middle;
  transition: .3s;
}

.icon:hover {
  cursor: pointer;
  font-size: 18px;
}
</style>