package com.demo.config.plugins;

import io.grpc.*;
import io.grpc.util.TransmitStatusRuntimeExceptionInterceptor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class GrpcServerInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        // 在这里可以添加拦截逻辑，比如打印请求信息、修改请求参数等
        System.out.println("Intercepting gRPC call: " + serverCall.getMethodDescriptor().getFullMethodName());

        ServerCall.Listener<ReqT> listener = serverCallHandler.startCall(serverCall, metadata);
        // 调用下一个拦截器或处理器
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {

            @Override
            public void onMessage(ReqT message) {
                super.onMessage(message);
            }

            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (Throwable e) {
                    System.out.println("onHalfClose error: " + e.getMessage());
                    log.error("onHalfClose error: ", e);
                }
            }

            @Override
            public void onCancel() {
                super.onCancel();
            }

            @Override
            public void onComplete() {
                super.onComplete();
            }

            @Override
            public void onReady() {
                super.onReady();
            }
        };

    }
}
