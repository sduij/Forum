import { createRouter, createWebHistory } from 'vue-router';
import {unauthorized} from "@/net/index.js";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'welcome',
            component: () => import('@/views/WelcomeView.vue'),
            children: [
                {
                    path: '',
                    name: 'welcome-login',
                    component: () => import('@/views/welcome/LoginPage.vue'),
                },
                {
                    path: '/register',
                    name: 'welcome-register',
                    component: () => import('@/views/welcome/RegisterPage.vue'),
                },
                {
                    path: '/reset',
                    name: 'welcome-reset',
                    component: () => import('@/views/welcome/ResetPage.vue'),
                }
            ]
        },
        {
            path: '/index',
            name: 'index',
            component: () => import('@/views/indexView.vue'),
            children: [
                {
                    path: '',
                    name: 'topics',
                    component: () => import('@/views/forum/Forum.vue'),
                    children: [
                        {
                            path: '',
                            name: 'topic-list',
                            component: () => import('@/views/forum/TopicList.vue')
                        },
                        {
                            path: 'topic-detail/:tid',
                            name: 'topic-detail',
                            component: () => import('@/views/forum/TopicDetail.vue')
                        }
                    ]
                },
                {
                    path: 'user-setting',
                    name: 'user-setting',
                    component: () => import('@/views/settings/UserSetting.vue')
                },
                {
                    path: 'privacy-setting',
                    name: 'privacy-setting',
                    component: () => import('@/views/settings/PrivacySetting.vue')
                }

            ]
        }
    ]

});

/*路由守卫v*/
router.beforeEach((to, from, next) => {
    const isUnauthorized = unauthorized()
    /*登录后无法访问welcome开头的页面，未登录无法访问index页面*/
    if (to.name.startsWith('welcome-') && !isUnauthorized) {
        next('/index')
    } else if (to.fullPath.startsWith('/index') && isUnauthorized) {
        next('/')
    } else {
        next()
    }
})

export default router;