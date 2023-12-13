package com.washerServer.main;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HttpWasherServerInitializer extends ChannelInitializer<SocketChannel> {

	private final SslContext sslCtx;

	public HttpWasherServerInitializer(SslContext sslCtx) {
		this.sslCtx = sslCtx;
	}

	@Override
	public void initChannel(SocketChannel ch) {
		ChannelPipeline p = ch.pipeline();
		if (sslCtx != null) {
			p.addLast(sslCtx.newHandler(ch.alloc()));
		}
		/*
		 * p.addLast(new HttpServerCodec()); p.addLast(new
		 * HttpServerExpectContinueHandler()); p.addLast(new
		 * HttpWasherServerHandler());
		 */

		p.addLast(new HttpServerCodec());
		p.addLast(new HttpObjectAggregator(65536));
		p.addLast(new ChunkedWriteHandler());
		p.addLast(new HttpWasherServerHandler());
	}
}
