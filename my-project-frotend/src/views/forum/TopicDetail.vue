<script setup>
import { useRoute } from "vue-router";
import {get, post} from "@/net";
import axios from "axios";
import {ArrowLeft, ChatSquare, CircleCheck, Delete, EditPen, Female, Male, Plus, Star} from "@element-plus/icons-vue";
import {reactive, computed,ref} from "vue";
import { QuillDeltaToHtmlConverter } from 'quill-delta-to-html';
import Card from "@/components/Card.vue";
import router from "@/router/index.js";
import TopicTag from "@/components/TopicTag.vue";
import InteractButton from "@/components/InteractButton.vue";
import {ElMessage} from "element-plus";
import {useStore} from "@/store/index.js";
import TopicEditor from "@/components/TopicEditor.vue";
import TopicCommentEditor from "@/components/TopicCommentEditor.vue";

const route = useRoute();

const tid = route.params.tid;

const topic = reactive({
  data: null,
  comments: null,
  like: false,
  collect: false,
  page: 1
});

const init=() =>get(`api/forum/topic?tid=${tid}`,
    data => {
      topic.data=data
      topic.like=data.interact.like
      topic.collect=data.interact.collect
      loadComments(1)
      console.log(data)
    }
)

init()


function convertToHtml(content) {
  const ops = JSON.parse(content).ops;
  const converter = new QuillDeltaToHtmlConverter(ops, { inlineStyles: true });
  return converter.convert();
}

function interact(type, message) {
  get(`/api/forum/interact?tid=${tid}&type=${type}&state=${!topic[type]}`,
      () => {
    topic[type] = !topic[type];
    if (topic[type]) {
      ElMessage.success(`${message}成功!` );
    } else {
      ElMessage.success( `已取消${message}!` );
    }
  }
);
}

const edit=ref(false)
const store=useStore()


function updateTopic(editor) {
  post(
   '/api/forum/update-topic',
    {
      id: tid,
      type: editor.type.id,
      title: editor.title,
      content: editor.text
    },  () => {
      ElMessage.success('帖子内容更新成功!' )
      edit.value = false;
      init();
    }
  )
}

const comment=reactive({
  show: false,
  text: '',
  quote: null
})

function loadComments(page) {
  topic.comments = null
  topic.page = page
  get(
    `/api/forum/comments?tid=${tid}&page=${page - 1}`,
   data => topic.comments = data
  )
}

function onCommentAdd() {
  comment.show = false;
  loadComments( Math.floor(++topic.data.comments / 10) +1)
}

function deleteComment(id) {
  get(`/api/forum/delete-comment?id=${id}`,
       () => {
    ElMessage.success(
      '删除评论成功!'
    )
    loadComments({ page: topic.page })
  }
)
}
</script>

<template>
  <div class="topic-page" v-if="topic.data">

    <div class="topic-main" style="position: sticky;top: 0;z-index: 10">
      <card style="display: flex; width: 100%;">
        <el-button :icon="ArrowLeft" type="info" size="small" plain round @click="router.push('/index')">返回列表</el-button>
        <div style="text-align: center; flex: 1;">
          <topic-tag   :type="topic.data.type" />
          <span style="font-weight: bold;margin-left: 10px">{{ topic.data.title }}</span>
        </div>
      </card>
    </div>

    <div class="topic-main">

      <div class="topic-main-left">

        <el-avatar :src="store.avatarUserUrl(topic.data.user.avatar) " :size="60"/>

        <div>
          <div style="font-size: 18px; font-weight: bold">
            {{topic.data.user.username}}
            <span style="color: hotpink" v-if="topic.data.user.gender == 1">
        <el-icon><Female/></el-icon>
      </span>
            <span style="color: dodgerblue" v-if="topic.data.user.gender == 0">
        <el-icon><Male/></el-icon>
      </span>
          </div>
          <div class="desc">{{topic.data.user.email}}</div>
        </div>

        <el-divider style="margin: 10px 0"/>
        <div style="text-align: left;margin: 0 5px">
          <div class="desc">微信号：{{topic.data.user.wx || '已隐藏或未填写'}}</div>
          <div class="desc">QQ号：{{topic.data.user.qq || '已隐藏或未填写'}}</div>
          <div class="desc">手机号：{{topic.data.user.phone || '已隐藏或未填写'}}</div>
        </div>

        <el-divider style="margin: 10px 0"/>
        <div class="desc" style="margin: 0 5px">{{topic.data.user.desc}}</div>

      </div>

      <div class="topic-main-right">
            <div class="topic-content "  v-html="convertToHtml(topic.data.content)"></div>
        <el-divider/>
        <div style="font-size: 13px; color: grey; text-align: center ">
          <div>发帖时间：{{new Date(topic.data.time).toLocaleString()}}</div>
        </div>
        <div style="text-align: right; margin-top: 30px">

          <interact-button name="编辑帖子" color="dodgerblue" :check="false" @check="edit = true"
                      style="margin-right: 20px" v-if="store.user.id===topic.data.user.id">
            <el-icon><EditPen/></el-icon>
          </interact-button>

          <interact-button name="点个赞吧"  check-name="已点赞" color="pink" :check="topic.like"
                           @check="interact('like', '点赞')">
            <el-icon><CircleCheck/></el-icon>
          </interact-button>


          <interact-button name="收藏本帖"  check-name="已收藏"  color="orange" :check="topic.collect"
                           style="margin-left: 20px"
                            @check="interact('collect', '收藏')">
            <el-icon><Star/></el-icon>
          </interact-button>
        </div>
      </div>

    </div>
    <transition name="el-fade-in-linear" mode="out-in">
      <div v-if="topic.comments">
        <div class="topic-main" style="margin-top: 10px" v-for="item in topic.comments">

          <div class="topic-main-left">

            <el-avatar :src="store.avatarUserUrl(item.user.avatar) " :size="60"/>

            <div>
              <div style="font-size: 18px; font-weight: bold">
                {{item.user.username}}
                <span style="color: hotpink" v-if="item.user.gender == 1">
        <el-icon><Female/></el-icon>
      </span>
                <span style="color: dodgerblue" v-if="item.user.gender == 0">
        <el-icon><Male/></el-icon>
      </span>
              </div>
              <div class="desc">{{item.user.email}}</div>
            </div>


          </div>

          <div class="topic-main-right">
            <div style="font-size: 13px; color: grey">
              <div>评论时间：{{new Date(item.time).toLocaleString()}}</div>
            </div>
            <div v-if="item.quote" class="comment-quote">
              回复：{{ item.quote }}
            </div>
            <div class="topic-content "  v-html="convertToHtml(item.content)"></div>
            <div style="text-align: right">
              <el-link :icon="ChatSquare" @click="comment.show = true; comment.quote = item" type="info">

                回复评论</el-link>
              <el-link :icon="Delete" type="danger" v-if="item.user.id === store.user.id"
                       style="margin-left: 20px"  @click="deleteComment(item.id)">

                删除评论</el-link>
            </div>
          </div>


        </div>

        <div style="margin: 20px auto; width: fit-content">
          <el-pagination background layout="prev, pager, next"
                         v-model:current-page="topic.page"
                         @current-change="loadComments"
                         :total="topic.data.comments"  :page-size="10"
                          hide-on-single-page/>
        </div>
      </div>
    </transition>

    <topic-editor :show="edit" @close="edit = false" v-if="topic.data&&store.forum.types"
                  :default-type="topic.data.type"
                  :default-text="topic.data.content"
                  :default-title="topic.data.title"
                  submit-button="更新帖子内容"   :submit="updateTopic" />

    <topic-comment-editor :show="comment.show" @close="comment.show=false" :tid="tid" :quote="comment.quote"
                          @comment="onCommentAdd"/>

    <div class="add-comment" @click="comment.show=true; comment.quote=null">
      <el-icon><Plus/></el-icon>
    </div>

  </div>
</template>

<style lang="less" scoped>
.comment-quote {
  font-size: 13px;
  color: grey;
  background-color: rgba(94, 94, 94, 0.2);
  padding: 10px;
  margin-top: 10px;
  border-radius: 5px;
}

.add-comment {
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  font-size: 18px;
  color: var(--el-color-primary);
  text-align: center;
  line-height: 45px;
  background: var(--el-bg-color-overlay);
  box-shadow: var(--el-box-shadow-lighter);

  &:hover {
    background: var(--el-border-color-extra-light);
    cursor: pointer;
  }
}

.topic-page {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 10px 0;
}
.topic-main {
  display: flex;
  border-radius: 7px;
  margin: 0 auto;
  background-color: var(--el-bg-color);
  width: 800px;

  .topic-main-left {
    width: 200px;
    padding: 10px;
    border-right: solid 1px var(--el-border-color);
    text-align: center;

    .desc{
      color: grey;
      font-size: 14px;
    }
  }

  .topic-main-right {
    width: 600px;
    padding: 10px 20px;
    display: flex;
    flex-direction: column;

    .topic-content{
      font-size: 14px;
      opacity: 0.8;
      line-height: 22px;
      flex: 1;
    }
  }

}

</style>