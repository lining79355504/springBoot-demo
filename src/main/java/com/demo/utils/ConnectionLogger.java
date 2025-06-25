package com.demo.utils;

import okhttp3.*;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

public class ConnectionLogger extends EventListener {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ConnectionLogger.class);

    public static final ConnectionLogger INSTANCE = new ConnectionLogger();

    @Override
    public void callStart(Call call) {
        logger.info("Call started: {}", call.request().url());
    }

    @Override
    public void dnsStart(Call call, String domainName) {
        logger.info("DNS lookup started for: {}", domainName);
    }

    @Override
    public void dnsEnd(Call call, String domainName, List<InetAddress> inetAddressList) {
        logger.info("DNS lookup finished for: {}, resolved IPs: {}", domainName, inetAddressList);
    }

    @Override
    public void connectStart(Call call, InetSocketAddress inetSocketAddress, Proxy proxy) {
        logger.info("Connecting to {} via proxy {}:{}", inetSocketAddress.getHostName(), proxy.type(), inetSocketAddress.getPort());
    }

    @Override
    public void connectionAcquired(Call call, Connection connection) {
        logger.info("Connection acquired: {}", connection);
    }

    @Override
    public void connectEnd(Call call, InetSocketAddress inetSocketAddress, Proxy proxy, @Nullable Protocol protocol) {
        logger.info("Connected to {} via proxy {}:{}", inetSocketAddress.getHostName(), proxy.type(), inetSocketAddress.getPort());
    }

    @Override
    public void connectFailed(Call call, InetSocketAddress inetSocketAddress, Proxy proxy, @Nullable Protocol protocol, IOException ioe) {
        logger.info("Connection failed: {} call.url {}", ioe.getMessage(), call.request().url());
    }

    @Override
    public void connectionReleased(Call call, Connection connection) {
        logger.info("Connection released: {}", connection);
    }

    @Override
    public void secureConnectStart(Call call) {
        logger.info("Starting secure (TLS) connection for call: {}", call.request().url());
    }

    @Override
    public void secureConnectEnd(Call call, Handshake handshake) {
        if (handshake != null) {
            logger.info("Secure connection established for call: {}", call.request().url());
            logger.info("  TLS Version: {}", handshake.tlsVersion());
            logger.info("  Cipher Suite: {}", handshake.cipherSuite());
            logger.info("  Peer Principal: {}", handshake.peerPrincipal());
        }
    }


    @Override
    public void callEnd(Call call) {
        logger.info("Call ended: {}", call.request().url());
    }

    @Override
    public void callFailed(Call call, IOException ioe) {
        logger.info("Call failed: {} call.url {}", ioe.getMessage(), call.request().url());
    }
}