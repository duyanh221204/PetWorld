import { createRouter, createWebHistory } from 'vue-router'
import { useAuth } from '@/composables/useAuth'

const routes = [
    {
        path: '/',
        redirect: '/welcome'
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('@/views/NotFoundView.vue')
    },
    {
        path: '/welcome',
        name: 'Welcome',
        component: () => import('@/views/WelcomeView.vue')
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/RegisterView.vue')
    },
    {
        path: '/verify-otp',
        name: 'VerifyOTP',
        component: () => import('@/views/VerifyOTPView.vue')
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/LoginView.vue')
    },
    {
        path: '/newsfeed',
        redirect: '/newsfeed/home'
    },
    {
        path: '/newsfeed/home',
        name: 'NewsFeedHome',
        component: () => import('@/views/NewsFeedView.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/newsfeed/group',
        name: 'NewsFeedGroup',
        component: () => import('@/views/NewsFeedView.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/newsfeed/friends',
        name: 'NewsFeedFriends',
        component: () => import('@/views/NewsFeedView.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/profile/:userId',
        name: 'Profile',
        component: () => import('@/views/ProfileView.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/post/create',
        name: 'CreatePost',
        component: () => import('@/views/CreateEditPostView.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/posts/:id',
        name: 'PostDetail',
        component: () => import('@/views/PostDetailView.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/friend-requests',
        name: 'FriendRequests',
        component: () => import('@/views/FriendRequestsView.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/notifications',
        name: 'Notifications',
        component: () => import('@/views/NotificationsView.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/groups',
        name: 'Groups',
        component: () => import('@/views/GroupsView.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/groups/:groupId',
        name: 'GroupDetail',
        component: () => import('@/views/GroupDetailView.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/groups/:groupId/join-form-questions/:formId',
        name: 'GroupJoinFormQuestions',
        component: () => import('@/views/GroupJoinFormQuestionsView.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/groups/:groupId/join-requests',
        name: 'GroupJoinRequests',
        component: () => import('@/views/GroupJoinRequestsView.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/groups/:groupId/join',
        name: 'GroupJoinForm',
        component: () => import('@/views/GroupJoinFormView.vue'),
        meta: { requiresAuth: true }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach(async (to, from, next) => {
    const { isAuthenticated, refreshAccessToken } = useAuth()

    if (!to.meta.requiresAuth || isAuthenticated.value)
        return next()

    try {
        await refreshAccessToken()
        return next()
    } catch (e) {
        return next({ name: 'Login' })
    }
})

export default router
