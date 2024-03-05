package com.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import java.net.InetSocketAddress;

import io.netty.util.CharsetUtil;

import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

public class Main {
    public static void main(String[] args) throws InterruptedException, URISyntaxException, SSLException {
        System.out.println("Java version: " + System.getProperty("java.version"));

        URI uri = new URI("https://api.kolet.com");
        String host = uri.getHost();
        int port = uri.getPort() == -1 ? 80 : uri.getPort();

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(new HttpClientCodec());
                    ch.pipeline().addLast(new HttpObjectAggregator(1048576)); // 1 MB
                    ch.pipeline().addLast(new SimpleChannelInboundHandler<HttpObject>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
                            if (msg instanceof HttpResponse) {
                                HttpResponse response = (HttpResponse) msg;
                                System.out.println("Response Status: " + response.status());
                            }
                            if (msg instanceof HttpContent) {
                                HttpContent content = (HttpContent) msg;
                                System.out.println(
                                        "Response Content: " + content.content().toString(CharsetUtil.UTF_8));
                            }
                        }
                    });
                }
            });

            ChannelFuture future = bootstrap.connect(host, port).sync();

            HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                    uri.getRawPath());
            request.headers().set(HttpHeaders.Names.HOST, host);
            request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            future.channel().writeAndFlush(request);

            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }

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
                                    .trustManager(InsecureTrustManagerFactory.INSTANCE) // Use an insecure trust manager
                                                                                        // for testing
                                    .build();
                            SSLEngine engine = sslContext.newEngine(ch.alloc());
                            engine.setUseClientMode(true);

                            // Misconfigure SSL/TLS settings here to intentionally cause handshake failure
                            // For example:
                            // engine.setEnabledProtocols(new String[]{"SSLv3"});
                            // engine.setEnabledCipherSuites(new
                            // String[]{"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384"});

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