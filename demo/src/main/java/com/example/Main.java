package com.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SniHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import java.net.InetSocketAddress;

import java.net.URISyntaxException;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

public class Main {
    public static void main(String[] args) throws InterruptedException, URISyntaxException, SSLException {
        System.out.println("Java version: " + System.getProperty("java.version"));

        // Try SSL handshake

        EventLoopGroup group = new NioEventLoopGroup();

        try {

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            SslContext sslContext = SslContextBuilder.forClient()
                                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                    .build();

                            // Non working SSL
                            SSLEngine engine = sslContext.newEngine(ch.alloc());

                            engine.setUseClientMode(true);

                            ch.pipeline().addLast(new SslHandler(engine));
                        }
                    });

            // Attempt to connect to the server
            bootstrap.connect(new InetSocketAddress("api.kolet.com", 443)).sync().channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }

    }
}