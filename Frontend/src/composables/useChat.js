import { ref, watch } from 'vue'
import { chatApi } from '@/api/chat'
import { useWebSocket } from './useWebSocket'
import { useAuth } from './useAuth'

const chats = ref([])
const unreadCount = ref(0)
const isLoading = ref(false)
const error = ref(null)
const currentlyViewingChatId = ref(null) // theo dõi chat nào đang mở

export const useChat = () => {
    const ws = useWebSocket()
    const auth = useAuth()

    const fetchUnreadCount = async () => {
        try {
            const response = await chatApi.getUnreadCount()
            if (response.data.status === 200)
                unreadCount.value = response.data.data
        } catch (err) {
            console.error('Error fetching unread chat count:', err)
            throw err
        }
    }

    const fetchLatestChats = async (limit = 15) => {
        isLoading.value = true
        error.value = null
        try {
            const response = await chatApi.getAllChats(0, limit)
            if (response.data.status === 200)
                chats.value = response.data.data.content || []
        } catch (err) {
            error.value = 'Failed to load chats'
            console.error('Error fetching chats:', err)
            throw err
        } finally {
            isLoading.value = false
        }
    }

    const fetchChats = async (page = 0, size = 50) => {
        isLoading.value = true
        error.value = null
        try {
            const response = await chatApi.getAllChats(page, size)
            if (response.data.status === 200)
                return response.data.data
            return { content: [], totalPages: 0, totalElements: 0 }
        } catch (err) {
            error.value = 'Failed to load chats'
            console.error('Error fetching chats:', err)
            return { content: [], totalPages: 0, totalElements: 0 }
        } finally {
            isLoading.value = false
        }
    }

    const markAsRead = async (chatId) => {
        try {
            const response = await chatApi.markAsRead(chatId)
            if (response.data.status === 200) {
                const chat = chats.value.find(c => c.id === chatId)
                if (chat && chat.hasUnread) {
                    chat.hasUnread = false
                    unreadCount.value = Math.max(0, unreadCount.value - 1)
                }
            }
        } catch (err) {
            console.error('Error marking chat as read:', err)
            throw err
        }
    }

    const addOrUpdateChat = (chatPayload) => {
        const isMessageFromOther = chatPayload.message && chatPayload.message.senderId !== auth.userId.value
        const shouldMarkUnread = isMessageFromOther && currentlyViewingChatId.value !== chatPayload.info.id

        const existingIndex = chats.value.findIndex(c => c.id === chatPayload.info.id)
        
        if (existingIndex !== -1) {
            const existingChat = chats.value[existingIndex]
            const wasUnread = existingChat.hasUnread

            chats.value.splice(existingIndex, 1)
            
            // quyết định trạng thái hasUnread
            // ưu tiên: shouldMarkUnread (dựa trên logic tin nhắn) > trạng thái đang xem > giá trị trả về từ backend
            let newHasUnread
            if (currentlyViewingChatId.value === chatPayload.info.id)
                newHasUnread = false
            else if (shouldMarkUnread)
                newHasUnread = true
            else
                newHasUnread = chatPayload.info.hasUnread !== undefined ? chatPayload.info.hasUnread : existingChat.hasUnread

            const updatedChat = {
                ...existingChat,
                ...chatPayload.info,
                lastMessagePreview: chatPayload.message?.content || chatPayload.info.lastMessagePreview || existingChat.lastMessagePreview,
                lastMessagedAt: chatPayload.message?.createdAt || chatPayload.info.lastMessagedAt || existingChat.lastMessagedAt,
                lastSenderId: chatPayload.message?.senderId || chatPayload.info.lastSenderId || existingChat.lastSenderId,
                hasUnread: newHasUnread
            }

            chats.value.unshift(updatedChat)

            if (!wasUnread && updatedChat.hasUnread)
                ++unreadCount.value
            else if (wasUnread && !updatedChat.hasUnread)
                unreadCount.value = Math.max(0, unreadCount.value - 1)
        } else {
            let newHasUnread
            if (currentlyViewingChatId.value === chatPayload.info.id)
                newHasUnread = false
            else if (shouldMarkUnread)
                newHasUnread = true
            else
                newHasUnread = chatPayload.info.hasUnread || false

            const newChat = {
                ...chatPayload.info,
                lastMessagePreview: chatPayload.message?.content || chatPayload.info.lastMessagePreview,
                lastMessagedAt: chatPayload.message?.createdAt || chatPayload.info.lastMessagedAt,
                lastSenderId: chatPayload.message?.senderId || chatPayload.info.lastSenderId,
                hasUnread: newHasUnread
            }
            
            chats.value.unshift(newChat)
            if (newChat.hasUnread)
                ++unreadCount.value
        }
    }

    const handleChatRead = (payload) => {
        const chat = chats.value.find(c => c.id === payload.chatId)
        if (chat && chat.hasUnread) {
            chat.hasUnread = false
            unreadCount.value = Math.max(0, unreadCount.value - 1)
        }
    }

    let messageSubscription = null
    let chatReadSubscription = null

    const subscribeToMessages = async () => {
        if (messageSubscription)
            return

        if (!ws.isConnected.value) {
            try {
                await ws.connect()
                // đợi lâu hơn để đảm bảo kết nối ổn định và notifications subscribe trước
                await new Promise(resolve => setTimeout(resolve, 200))
                doSubscribe()
            } catch (err) {
                console.error('[useChat] Failed to connect WebSocket:', err)
            }
        } else {
            await new Promise(resolve => setTimeout(resolve, 100))
            doSubscribe()
        }
    }

    const doSubscribe = () => {
        messageSubscription = ws.subscribe('/user/queue/messages', (chatPayload) =>
            addOrUpdateChat(chatPayload))

        chatReadSubscription = ws.subscribe('/user/queue/chats/read', (readPayload) =>
            handleChatRead(readPayload))
    }

    const sendMessage = (recipientId, content) => {
        if (!ws.isConnected.value) {
            console.error('WebSocket not connected')
            throw new Error('WebSocket not connected')
        }

        return new Promise((resolve) => {
            ws.send('/app/messages/send', {
                recipientId,
                content
            })
            resolve({ success: true })
        })
    }

    const createOrGetChat = async (recipientId) => {
        const existingChat = chats.value.find(chat => 
            chat.recipientId === recipientId || chat.otherUserId === recipientId
        )
        
        if (existingChat)
            return existingChat.id
        return null
    }

    const initialize = async () => {
        if (!auth.isAuthenticated.value)
            return

        try {
            await Promise.all([
                fetchUnreadCount(),
                fetchLatestChats(15)
            ])

            if (!ws.isConnected.value)
                await ws.connect()
            await subscribeToMessages()
        } catch (err) {
            console.error('Error initializing chats:', err)
            throw err
        }
    }

    watch(
        () => auth.isAuthenticated.value,
        async (authenticated) => {
            if (authenticated) {
                try {
                    await initialize()
                } catch (err) {
                    console.error('Failed to initialize chats:', err)
                }
            } else {
                chats.value = []
                unreadCount.value = 0
                messageSubscription = null
                chatReadSubscription = null
            }
        },
        { immediate: false }
    )

    watch(
        () => ws.isConnected.value,
        async (connected) => {
            if (connected && auth.isAuthenticated.value && !messageSubscription)
                await subscribeToMessages()
            else if (!connected) {
                messageSubscription = null
                chatReadSubscription = null
            }
        }
    )

    return {
        chats,
        unreadCount,
        isLoading,
        error,
        currentlyViewingChatId,
        fetchUnreadCount,
        fetchLatestChats,
        fetchChats,
        markAsRead,
        addOrUpdateChat,
        sendMessage,
        createOrGetChat,
        subscribeToMessages,
        initialize
    }
}
