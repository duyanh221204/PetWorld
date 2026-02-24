package com.duyanhnguyen.petworld.backend.configuration;

import com.duyanhnguyen.petworld.backend.service.impl.token.JwtService;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebSocketSecurityInterceptor implements ChannelInterceptor {

    JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor stompHeaderAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (stompHeaderAccessor == null)
            return message;

        if (StompCommand.CONNECT.equals(stompHeaderAccessor.getCommand())) {
            String authHeader = stompHeaderAccessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.error("WebSocket connection denied: Missing or invalid Authorization header");
                throw new MessageDeliveryException("WebSocket authentication failed");
            }

            String accessToken = authHeader.substring(7);
            try {
                SignedJWT signedJWT = jwtService.verifyToken(accessToken);
                String userId = signedJWT.getJWTClaimsSet().getSubject();
                stompHeaderAccessor.setUser(() -> userId);
            } catch (Exception e) {
                log.error("WebSocket connection denied: Invalid token");
                throw new MessageDeliveryException("WebSocket authentication failed");
            }
        }
        return message;
    }

}
