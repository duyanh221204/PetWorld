import { ref, watch } from 'vue'
import { notificationApi } from '@/api/notification'
import { useWebSocket } from './useWebSocket'
import { useAuth } from './useAuth'

const notifications = ref([])
const unreadCount = ref(0)
const isLoading = ref(false)
const error = ref(null)

export const useNotifications = () => {
    const ws = useWebSocket()
    const auth = useAuth()

    const fetchUnreadCount = async () => {
        try {
            const response = await notificationApi.getUnreadCount()
            if (response.data.status === 200)
                unreadCount.value = response.data.data
        } catch (err) {
            console.error('Error fetching unread count:', err)
            throw err
        }
    }

    const fetchLatestNotifications = async (limit = 15) => {
        isLoading.value = true
        error.value = null
        try {
            const response = await notificationApi.getNotifications(0, limit)
            if (response.data.status === 200)
                notifications.value = response.data.data.content || []
        } catch (err) {
            error.value = 'Failed to load notifications'
            console.error('Error fetching notifications:', err)
            throw err
        } finally {
            isLoading.value = false
        }
    }

    const fetchNotifications = async (page = 0, size = 50) => {
        isLoading.value = true
        error.value = null
        try {
            const response = await notificationApi.getNotifications(page, size)
            if (response.data.status === 200)
                return response.data.data
            return { content: [], totalPages: 0, totalElements: 0 }
        } catch (err) {
            error.value = 'Failed to load notifications'
            console.error('Error fetching notifications:', err)
            return { content: [], totalPages: 0, totalElements: 0 }
        } finally {
            isLoading.value = false
        }
    }

    const markAllAsRead = async () => {
        try {
            const response = await notificationApi.markAllAsRead()
            if (response.data.status === 200) {
                notifications.value = notifications.value.map(n => ({ ...n, isRead: true }))
                unreadCount.value = 0
            }
        } catch (err) {
            console.error('Error marking all as read:', err)
            throw err
        }
    }

    const markAsRead = async (notificationId) => {
        try {
            const response = await notificationApi.markAsRead(notificationId)
            if (response.data.status === 200) {
                const notification = notifications.value.find(n => n.id === notificationId)
                if (notification && !notification.isRead) {
                    notification.isRead = true
                    unreadCount.value = Math.max(0, unreadCount.value - 1)
                }
            }
        } catch (err) {
            console.error('Error marking notification as read:', err)
            throw err
        }
    }

    const addNotification = (notification) => {
        notifications.value = [notification, ...notifications.value]
        if (!notification.isRead)
            ++unreadCount.value
    }

    let notificationSubscription = null

    const subscribeToNotifications = async () => {
        // Nếu đã subscribe rồi, không subscribe lại
        if (notificationSubscription) {
            console.log('Already subscribed to notifications')
            return
        }

        if (!ws.isConnected.value) {
            console.warn('WebSocket not connected, attempting to connect...')
            try {
                await ws.connect()
                doSubscribe()
            } catch (err) {
                console.error('Failed to connect for notifications:', err)
            }
        } else
            doSubscribe()
    }

    const doSubscribe = () => {
        notificationSubscription = ws.subscribe('/user/queue/notifications', (notification) => {
            console.log('Received notification:', notification)
            addNotification(notification)
        })
    }

    // khởi tạo sau khi đăng nhập
    const initialize = async () => {
        if (!auth.isAuthenticated.value)
            return

        try {
            await Promise.all([
                fetchUnreadCount(),
                fetchLatestNotifications(15)
            ])

            if (!ws.isConnected.value)
                await ws.connect()
            await subscribeToNotifications()
        } catch (err) {
            console.error('Error initializing notifications:', err)
            throw err
        }
    }

    // theo dõi trạng thái đăng nhập để khởi tạo hoặc xóa thông báo
    watch(
        () => auth.isAuthenticated.value,
        async (authenticated) => {
            if (authenticated) {
                try {
                    await initialize()
                    console.log('Notifications initialized')
                } catch (err) {
                    console.error('Failed to initialize notifications:', err)
                }
            } else {
                notifications.value = []
                unreadCount.value = 0
                notificationSubscription = null // reset subscription khi logout
            }
        },
        { immediate: false }
    )

    // theo dõi WebSocket reconnect để subscribe lại
    watch(
        () => ws.isConnected.value,
        async (connected) => {
            if (connected && auth.isAuthenticated.value && !notificationSubscription) {
                console.log('WebSocket reconnected, resubscribing...')
                await subscribeToNotifications()
            } else if (!connected)
                notificationSubscription = null // reset khi disconnect
        }
    )

    return {
        notifications,
        unreadCount,
        isLoading,
        error,
        fetchUnreadCount,
        fetchLatestNotifications,
        fetchNotifications,
        markAllAsRead,
        markAsRead,
        addNotification,
        subscribeToNotifications,
        initialize
    }
}
