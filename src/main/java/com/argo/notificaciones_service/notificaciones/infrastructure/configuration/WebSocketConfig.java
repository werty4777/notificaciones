package com.argo.notificaciones_service.notificaciones.infrastructure.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/secured/user/queue/specific-user");

        registry.setApplicationDestinationPrefixes("/app");

        //registry.setUserDestinationPrefix("/secured/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat").setAllowedOrigins("*");
        registry.addEndpoint("/chat").setAllowedOrigins("*").withSockJS();
    }


}
