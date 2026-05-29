package com.mro.ar.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebRTC signaling over WebSocket.
 * Message format: {"type":"offer|answer|candidate|annotation|join|leave","sessionId":"xxx","payload":{...}}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArSignalingHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;

    /** sessionId -> set of WebSocketSessions in that room */
    private final ConcurrentHashMap<String, CopyOnWriteArraySet<WebSocketSession>> rooms =
            new ConcurrentHashMap<>();

    /** wsSessionId -> AR sessionId */
    private final ConcurrentHashMap<String, String> sessionIndex = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.debug("WS connected: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<?, ?> msg = objectMapper.readValue(message.getPayload(), Map.class);
        String type = (String) msg.get("type");
        String arSessionId = (String) msg.get("sessionId");

        if (arSessionId == null || type == null) {
            log.warn("Malformed signaling message from {}", session.getId());
            return;
        }

        switch (type) {
            case "join" -> joinRoom(session, arSessionId);
            case "leave" -> leaveRoom(session, arSessionId);
            default -> relay(session, arSessionId, message.getPayload());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String arSessionId = sessionIndex.remove(session.getId());
        if (arSessionId != null) {
            CopyOnWriteArraySet<WebSocketSession> room = rooms.get(arSessionId);
            if (room != null) {
                room.remove(session);
                if (room.isEmpty()) {
                    rooms.remove(arSessionId);
                }
            }
        }
        log.debug("WS disconnected: {}", session.getId());
    }

    private void joinRoom(WebSocketSession session, String arSessionId) {
        rooms.computeIfAbsent(arSessionId, k -> new CopyOnWriteArraySet<>()).add(session);
        sessionIndex.put(session.getId(), arSessionId);
        log.debug("Session {} joined room {}", session.getId(), arSessionId);
    }

    private void leaveRoom(WebSocketSession session, String arSessionId) {
        CopyOnWriteArraySet<WebSocketSession> room = rooms.get(arSessionId);
        if (room != null) {
            room.remove(session);
        }
        sessionIndex.remove(session.getId());
    }

    /** Relay message to all other participants in the same room */
    private void relay(WebSocketSession sender, String arSessionId, String payload) {
        CopyOnWriteArraySet<WebSocketSession> room = rooms.get(arSessionId);
        if (room == null) return;
        TextMessage out = new TextMessage(payload);
        for (WebSocketSession peer : room) {
            if (!peer.getId().equals(sender.getId()) && peer.isOpen()) {
                try {
                    peer.sendMessage(out);
                } catch (IOException e) {
                    log.warn("Failed to relay to {}: {}", peer.getId(), e.getMessage());
                }
            }
        }
    }

    public void broadcastToRoom(String arSessionId, String json) {
        CopyOnWriteArraySet<WebSocketSession> room = rooms.get(arSessionId);
        if (room == null) return;
        TextMessage msg = new TextMessage(json);
        for (WebSocketSession s : room) {
            if (s.isOpen()) {
                try {
                    s.sendMessage(msg);
                } catch (IOException e) {
                    log.warn("Broadcast failed: {}", e.getMessage());
                }
            }
        }
    }
}
