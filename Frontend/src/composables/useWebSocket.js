import { ref, watch } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useAuth } from './useAuth'

const WS_URL = import.meta.env.VITE_WS_BASE_URL
let stompClient = null
let subscriptions = []
const isConnected = ref(false)
const connectionError = ref(null)

export const useWebSocket = () => {
    const auth = useAuth()

    const connect = () => {
        return new Promise((resolve, reject) => {
            if (isConnected.value) {
                resolve()
                return
            }

            // khởi tạo kết nối STOMP qua SockJS
            stompClient = new Client({
                webSocketFactory: () => new SockJS(WS_URL),
                connectHeaders: {
                    Authorization: `Bearer ${auth.accessToken.value}`
                },
                debug: (str) => {
                    console.log('STOMP debug:', str)
                },
                reconnectDelay: 5000,
                heartbeatIncoming: 4000,
                heartbeatOutgoing: 4000,
                onConnect: (frame) => {
                    console.log('WebSocket connected:', frame)
                    isConnected.value = true
                    connectionError.value = null
                    resolve()
                },
                onStompError: (frame) => {
                    console.error('STOMP error:', frame)
                    connectionError.value = frame.headers['message'] || 'Connection error'
                    reject(new Error(connectionError.value))
                },
                onWebSocketError: (event) => {
                    console.error('WebSocket error:', event)
                    connectionError.value = 'WebSocket connection failed'
                    reject(new Error(connectionError.value))
                },
                onDisconnect: () => {
                    console.log('WebSocket disconnected')
                    isConnected.value = false
                    subscriptions = []
                }
            })

            stompClient.activate()
        })
    }

    const disconnect = async () => {
        if (stompClient) {
            // unsubscribe tất cả trước khi disconnect
            subscriptions.forEach(sub => {
                if (sub)
                    sub.unsubscribe()
            })
            subscriptions = []

            try {
                await stompClient.deactivate()
                console.log('WebSocket deactivated')
            } catch (err) {
                console.error('Error deactivating WebSocket:', err)
                isConnected.value = false
            }
        }
    }

    const subscribe = (destination, callback) => {
        if (!isConnected.value) {
            console.error('Cannot subscribe: WebSocket not connected')
            return null
        }

        try {
            const subscription = stompClient.subscribe(destination, (message) => {
                try {
                    const data = JSON.parse(message.body)
                    callback(data)
                } catch (error) {
                    console.error('Error parsing message:', error)
                    throw error
                }
            })

            subscriptions.push(subscription)
            return subscription
        } catch (error) {
            console.error('Error subscribing:', error)
            return null
        }
    }

    const send = (destination, body = {}) => {
        if (!isConnected.value) {
            console.error('Cannot send: WebSocket not connected')
            return
        }

        try {
            stompClient.publish({
                destination,
                body: JSON.stringify(body)
            })
        } catch (error) {
            console.error('Error sending message:', error)
            throw error
        }
    }

    // tự động kết nối hoặc ngắt kết nối dựa trên trạng thái xác thực
    watch(
        () => auth.isAuthenticated.value,
        async (authenticated) => {
            if (authenticated) {
                try {
                    await connect()
                } catch (err) {
                    console.error('Failed to connect WebSocket:', err)
                }
            } else
                await disconnect()
        },
        { immediate: false }
    )

    return {
        isConnected,
        connectionError,
        connect,
        disconnect,
        subscribe,
        send
    }
}
