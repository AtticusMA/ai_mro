package com.mro.dtwin.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class DtwinRedisSubscriber {

    private final RedisMessageListenerContainer redisListenerContainer;
    private final DtwinWebSocketHandler webSocketHandler;

    @PostConstruct
    public void subscribe() {
        MessageListenerAdapter adapter = new MessageListenerAdapter(
                (MessageListener) (message, pattern) -> relay(message));
        redisListenerContainer.addMessageListener(adapter, new PatternTopic("dtwin:*"));
    }

    private void relay(Message message) {
        String payload = new String(message.getBody(), StandardCharsets.UTF_8);
        log.debug("Redis → WS relay: {}", payload);
        webSocketHandler.broadcast(payload);
    }
}
