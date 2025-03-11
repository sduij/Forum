<script setup>
import { User,Lock} from '@element-plus/icons-vue'
import { reactive,ref } from 'vue';
import {login} from "@/net/index.js";
import router from "@/router/index.js";
const form = reactive({
  username: '',
  password: '',
  remember: false
})
const formRef=ref();
const rule = {
  username: [
    { required: true, message: '请输入用户名' }
  ],
  password: [
    { required: true, message: '请输入密码' }
  ]
};

function userLogin() {
  /*这里可以直接引用表单的名字formRef*/
  formRef.value.validate((valid) => {
    if (valid) {
      login(form.username, form.password, form.remember, () => router.push('/index'));
    }
  });
}
</script>

<template>
  <div style="text-align: center; margin: 0 20px">
    <div style="margin-top: 250px">
      <div style="font-size: 40px; font-weight: bold">登录</div>
      <div style="font-size: 17px; color: grey">在进入系统之前，请先输入用户名和密码进行登录</div>
    </div>

    <div style="margin-top: 80px">
      <el-form :model="form" :rules="rule" ref="formRef" >

        <el-form-item prop="username">
          <el-input v-model="form.username" maxlength="10" type="text" placeholder="用户名/邮箱"
                    style="width: 400px;height: 50px;margin-left: 80px"  >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="form.password" maxlength="20" placeholder="密码" type="password"
                    style="width: 400px;height: 50px;margin-left: 80px;margin-top: 30px">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-row>
          <el-col :span="12" style="text-align: left">
            <el-form-item prop="remember">
              <el-checkbox v-model="form.remember" label="记住我" style="margin-left: 80px"/>
            </el-form-item>
          </el-col>
          <el-col :span="12"  >
            <el-link @click="router.push('/reset')">忘记密码?</el-link>
          </el-col>
        </el-row>

      </el-form>
    </div>
    <div style="margin-top: 60px" >
      <el-button style="width: 270px" type="success" @click="userLogin" plain>立即登录</el-button>
      <el-divider>
        <span style="font-size: 13px; color: grey">没有账号</span>
      </el-divider>
      <div>
        <el-button style="width: 270px" type="warning" @click="router.push('/register')" plain>立即注册</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
</style>