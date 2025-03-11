<script setup>

import Card from "@/components/Card.vue";
import {Message, Select,User,Refresh} from "@element-plus/icons-vue";
import {useStore} from "@/store/index.js";
import {computed} from "vue";
import { ref,reactive } from 'vue';
import {ElMessage} from "element-plus";
import {post, get, accessHeader} from "@/net";
import axios from "axios";
const store=useStore()
const registerTime=computed(()=> new Date(store.user.registerTime).toLocaleString())

const baseForm = reactive({
  username: '',
  gender: 1,
  phone: '',
  qq: '',
  wx: '',
  desc: ''
});

const emailForm = reactive({
  email: '',
  code: ''
});

const validateUsername = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入用户名'));
  } else if (!/^[a-zA-Z0-9\u4e00-\u9fa5]+$/.test(value)) {
    callback(new Error('用户名不能包含特殊字符，只能是中文/英文'));
  } else {
    callback();
  }
}

const rules = {
  username: [
    { validator: validateUsername, trigger: ['blur', 'change'] },
    { min: 2, max: 10, message: '用户名的长度必须在2-10个字符之间', trigger: ['blur', 'change'] }
  ],
  email: [
    { required: true, message: '请输入邮件地址', trigger: 'blur' },
    { type: 'email', message: '请输入合法的电子邮件地址', trigger: ['blur', 'change'] }
  ]
}

const baseFormRef=ref()
const emailFormRef=ref()
/*ref('')：这里 ref 函数用于将一个普通的值（在这个例子中是一个空字符串 ''）转换为一个“响应式”的引用对象。这个引用对象允许你在应用的任何地方使用它，并且当它的值发生变化时，所有使用该引用的地方都会自动更新以反映最新的值。
desc：这是你给这个响应式引用对象起的名字。之后你可以通过 desc.value 来访问或修改这个引用对象内部的实际值。 */
const desc=ref('')

const loading=reactive({
  form: true,
  base: false
})

function saveDetails() {
  baseFormRef.value.validate(isValid => {
    if (isValid) {
      loading.base=true
      post('/api/user/save-details', baseForm, () => {
        ElMessage.success('用户信息保存成功');
        store.user.username = baseForm.username;
        desc.value = baseForm.desc;
        loading.base=false
      },(message)=>{
        ElMessage.warning(message)
        loading.base=false
      });
    }
  });
}

get('/api/user/details', data => {
  baseForm.username = store.user.username;
  baseForm.gender = data.gender;
  baseForm.phone = data.phone;
  baseForm.wx = data.wx;
  baseForm.qq = data.qq;
  baseForm.desc = desc.value = data.desc;
  emailForm.email=store.user.email
  loading.form=false
});

const coldTime = ref(0);

function sendEmailCode() {
  emailFormRef.value.validate(isValid => {
    if (isValid) {
      coldTime.value = 60;
      get(`/api/auth/ask-code?email=${encodeURIComponent(emailForm.email)}&type=modify`, () => {
        ElMessage.success(`验证码已成功发送到邮箱：${emailForm.email}，请注意查收`);
        const handle = setInterval(() => {
          coldTime.value--;
          if (coldTime.value === 0) {
            clearInterval(handle);
          }
        }, 1000);
      }, (message) => {
        ElMessage.warning(message);
        coldTime.value = 0;
      });
    }
  });
}

const isEmailValid = ref(true);
const onValidate = (prop, isValid) => {
  if (prop === 'email') {
    isEmailValid.value = isValid;
  }
}

function modifyEmail() {
  emailFormRef.value.validate(isValid => {
    if (isValid) {
      post('/api/user/modify-email', emailForm, () => {
        ElMessage.success('邮件修改成功');
        store.user.email = emailForm.email;
        emailForm.code = '';
      });
    }
  });
}

function beforeAvatarUpload(rawFile) {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    ElMessage.error({ message: '头像只能是JPG/PNG 格式的' });
    return false;
  } else if (rawFile.size / 1024 > 200) {
    ElMessage.error({ message: '头像大小不能大于 200KB' });
    return false;
  }
  return true;
}

function uploadSuccess(response) {
  ElMessage.success( '头像上传成功' );
  store.user.avatar = response.data;
}
</script>

<template>
  <div style="display: flex; max-width: 1300px; margin: auto">

    <div class="settings-left" style="width: 70%; flex: 1">

      <card :icon="User" title="账号信息设置" desc="在这里编辑您的个人信息，您可以在隐私设置中选择是否展示这些信息" v-loading="loading.form">

        <el-form label-position="top" style="margin: 0 10px 10px 10px" :model="baseForm" :rules="rules.username" ref="baseFormRef">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="baseForm.username" maxlength="20"/>
          </el-form-item>
          <el-form-item label="性别" >
            <el-radio-group v-model="baseForm.gender">
              <el-radio :value="1">男</el-radio>
              <el-radio :value="0">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="baseForm.phone" maxlength="11"/>
          </el-form-item>
          <el-form-item label="QQ号" prop="qq">
            <el-input v-model="baseForm.qq" maxlength="13"/>
          </el-form-item>
          <el-form-item label="微信号" prop="wx">
            <el-input v-model="baseForm.wx" maxlength="20"/>
          </el-form-item>
          <el-form-item label="个人简介" prop="desc">
            <el-input type="textarea" :rows="6" v-model="baseForm.desc" maxlength="200"/>
          </el-form-item>
          <div>
            <el-button :icon="Select" type="success" @click="saveDetails" :loading="loading.base">保存用户信息</el-button>
          </div>
        </el-form>

      </card>

      <card style="margin-top: 10px" :icon="Message" title="电子邮件设置" desc="您可以在这里修改默认绑定的电子邮件地址">
        <el-form label-position="top" style="margin: 0 10px 10px 10px" :model="emailForm" :rules="rules.email&&rules.code" ref="emailFormRef" @validate="onValidate">
          <el-form-item label="电子邮件" prop="email">
            <el-input v-model="emailForm.email"/>
          </el-form-item>
          <el-form-item prop="code">
            <el-row style="width: 100%" :gutter="10">
              <el-col :span="18">
                <el-input placeholder="请获取验证码" v-model="emailForm.code"></el-input>
              </el-col>
              <el-col :span="6">
                <el-button type="success" style="width: 100%" @click="sendEmailCode" :disabled="!isEmailValid || coldTime > 0" plain>
                  {{ coldTime > 0 ? '请稍后' + coldTime + '秒 获取验证码' : '获取验证码' }}
                </el-button>
              </el-col>
            </el-row>
          </el-form-item>
          <div>
            <el-button :icon="Refresh" type="success" @click="modifyEmail">更新电子邮件</el-button>
          </div>
        </el-form>
      </card>

    </div>


    <div class="settings-right" style="width: 30%">
      <div style=" position:sticky ; margin: 20px">

        <card style="text-align: center; padding: 5px 15px 0 15px">
          <div>
            <el-avatar :size="70"
                       :src="store.avatarUrl">

            </el-avatar>
            <div style="margin: 5px 0">
              <el-upload
                  :action="axios.defaults.baseURL + '/api/image/avatar'"
                  :show-file-list="false"
                  :before-upload="beforeAvatarUpload"
                  :on-success="uploadSuccess"
                  :headers="accessHeader()">
                <el-button size="small" round>修改头像</el-button>
              </el-upload>
            </div>
            <div style="font-weight: bold">你好，{{ store.user.username }}</div>
          </div>
          <el-divider style="margin: 10px 0"></el-divider>
          <div style="font-size: 14px; color: grey; padding: 10px">
            {{desc || '这个用户很懒，没有填写用户信息'}}
          </div>
        </card>


        <card style="margin-top: 10px; font-size: 14px">
          <div>账号注册时间：{{ registerTime }}</div>
          <div style="color: grey">欢迎加入我们的学习论坛！</div>
        </card>

      </div>
    </div>

  </div>
</template>

<style scoped>
.settings-left {
  flex: 1;
  margin: 20px;
}

.settings-right {
  width: 300px;
  margin: 20px 30px 0 20px;
  /* 添加以下样式 */
  height: calc(100vh - 50px); /* 根据实际情况调整高度 */
  overflow-y: auto; /* 确保有足够的内容可以滚动 */
}


</style>