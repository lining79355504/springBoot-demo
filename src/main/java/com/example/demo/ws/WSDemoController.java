package com.example.demo.ws;

import com.demo.config.WebSocketConfig;
import org.apache.tomcat.websocket.WsSession;
import org.apache.tomcat.websocket.server.WsRemoteEndpointImplServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流：
 *
 * 同一个buvid 最多一个会话  分布式限流
 * 同一个mid最多1个会话（mid存在的话）
 * 同一个ip 最多10个会话
 * Lancer 存储会话内容 （图像 表情 待调研）
 * 实现：
 *
 * 1、新建会话服务（防止对其他业务影响）
 *
 * 2、依赖
 *
 * spring-boot-starter-websocket
 * 3、redis 存储session信息  key mid+设备ID  value（token）
 * 4、退出聊天窗 停止会话
 *
 *
 * 压测：
 *  最大连接数压测 、spring 链接总数设置等
 *
 * TODO
 * 客户回复消息回复涉及到分布式问题 通过消息广播实现 redis 的广播
 *
 * 分布式sessionId  （IP + bvid ）  或者生成的sessionId
 *
 * 重启或者发布 当前session 会中断
 *
 * client 失败重连 （前端实现）
 *
 *             <dependency>
 *                 <groupId>org.springframework.boot</groupId>
 *                 <artifactId>spring-boot-starter-websocket</artifactId>
 *                 <version>${spring.boot.version}</version>
 *             </dependency>
 *
 * ServerEndpoint 非单例bean @AutoWired @Resource 注解注入 均会失败
 * 需引入 SpringBeanContext  解决依赖注入的问题
 */



// 使用 @ServerEndpoint 注解表示此类是一个 WebSocket 端点
// 通过 value 注解，指定 websocket 的路径

@ServerEndpoint(value = "/channel/echo", configurator = WebSocketConfig.class)
public class WSDemoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WSDemoController.class);

    private Session session;
    //key  session id
    // 分布式sessionID
    private static final Map<String, Session> openSessions = new ConcurrentHashMap<>();

    // 收到消息
    @OnMessage
    public void onMessage(String message) throws IOException {

        Map<String, List<String>> query = session.getRequestParameterMap();

        String remoteIp = (String)session.getUserProperties().get("x-forwarded-for");

        LOGGER.info("[websocket] 收到消息：id={}, from {}, message={}", this.session.getId(), remoteIp, message);

        if (message.equalsIgnoreCase("bye")) {
            // 由服务器主动关闭连接。状态码为 NORMAL_CLOSURE（正常关闭）。
            this.session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Bye"));;
            return;
        }


        this.session.getAsyncRemote().sendText("["+ Instant.now().toEpochMilli() +"] Hello " + message);
    }

    // 连接打开
    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig){
        // 保存 session 到对象
        //session 超时时间 半小时
        session.setMaxIdleTimeout(1000 * 60 * 20);
        this.session = session;
        openSessions.put(session.getId(), session);
        LOGGER.info("[websocket] 新的连接：id={}", this.session.getId());
    }

    // 连接关闭
    @OnClose
    public void onClose(CloseReason closeReason){
        LOGGER.info("[websocket] 连接断开：id={}，reason={}", this.session.getId(),closeReason);
    }

    // 连接异常  分布式回调如何实现 ? 消息交互
    @OnError
    public void onError(Throwable throwable) throws IOException {

        LOGGER.info("[websocket] 连接异常：id={}，throwable={}", this.session.getId(), throwable.getMessage());

        // 关闭连接。状态码为 UNEXPECTED_CONDITION（意料之外的异常）
        this.session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, throwable.getMessage()));
    }

    public void boardCast(String message) {
        for (Session openSession : session.getOpenSessions()) {
            try {
                openSession.getAsyncRemote().sendText("[" + Instant.now().toEpochMilli() + "] boardCast " + session.getId() + " " + message);
            } catch (Exception e) {
                LOGGER.error("error ", e);
            }
        }
    }

    public void boardCastById(String id, String message) {

        Session session = openSessions.get(id);
        if (null == session) {
            return;
        }
        try {
            session.getAsyncRemote().sendText("[" + Instant.now().toEpochMilli() + "] boardCast " + session.getId() + " " + message);
        } catch (Exception e) {
            LOGGER.error("error ", e);
        }
    }

    /**
     *
     * @ServerEndpoint(value = "/channel/echo/{id}")
     *
     * ...
     *
     * @OnOpen
     * public void onOpen(Session session, @PathParam("id") Long id, EndpointConfig endpointConfig){
     *     ....
     * }
     */
}