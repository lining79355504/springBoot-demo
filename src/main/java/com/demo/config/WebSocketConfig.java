package com.demo.config;

import com.example.demo.ws.WSDemoController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.List;
import java.util.Map;

/**
 * 开启WebSocket支持
 * 在实际的ServerEndpoint  通过一下注解生效配置
 * @ServerEndpoint(value = "/channel/echo", configurator = WebSocketConfig.class)
 */
@Configuration
public class WebSocketConfig extends ServerEndpointConfig.Configurator {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {

        ServerEndpointExporter exporter = new ServerEndpointExporter();

        // 手动注册 WebSocket 端点
        exporter.setAnnotatedEndpointClasses(WSDemoController.class);

        return exporter;
    }

    /**
     * 获取http header 设置到session中的userProperties
     * @param sec
     * @param request
     * @param response
     */
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        Map<String, Object> userProperties = sec.getUserProperties();
        Map<String, List<String>> headers = request.getHeaders();
        List<String> remoteIp = headers.get("x-forwarded-for");
        if (!CollectionUtils.isEmpty(remoteIp)) {
            userProperties.put("x-forwarded-for", remoteIp.get(0));
        }
    }
}