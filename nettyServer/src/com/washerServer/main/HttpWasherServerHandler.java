package com.washerServer.main;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import com.washerServer.DBCon.DBConnection;
import com.washerServer.clientPC.ClientPC;
import com.washerServer.phone.ClientAndroid;
import com.washerServer.raspi.RasPi;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;

public class HttpWasherServerHandler extends SimpleChannelInboundHandler<FullHttpRequest>{
	private byte[] CONTENT = { 'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd' };

	private static final AsciiString CONTENT_TYPE = new AsciiString("Content-Type");
	private static final AsciiString CONTENT_LENGTH = new AsciiString("Content-Length");
	
	RasPi customUtil = new RasPi();
	ClientPC clientpc = new ClientPC();
	ClientAndroid anclient = new ClientAndroid();
	public static DBConnection dbcon =new DBConnection();
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
		// TODO Auto-generated method stub
		System.out.println(msg.uri().toString());
		String[] temp = msg.uri().toString().split("/");
		ByteBuf buf = msg.content();
		String requestString = buf.toString(CharsetUtil.UTF_8);
		String servername="null";
		if(temp.length>1){
			switch (temp[1]) {
			case "raspi":
				CONTENT = customUtil.recieveJSONFromRaspi(requestString,temp[2]);
				servername="RASPI";
				break;
			case "pc":
				if(msg.method().toString().equals("POST")){
					CONTENT = clientpc.recieveJSONFromPC(requestString,temp[2]);
				}else{
					CONTENT = clientpc.recieveGETFromPC(requestString, temp[2], temp[3]);
				}
				servername="PC";
				break;
			case "phone":
				CONTENT = anclient.recieveJSONFromAn(requestString, temp[2]);
				servername="PHONE";
				break;
			default:
				break;
			}
		}
		System.out.println("Used server name : "+servername);
		customFullHttpResponseForJSON(ctx);

	}

	
	private void customFullHttpResponseForJSON(ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(CONTENT));
		response.headers().set(CONTENT_TYPE, "application/json");
		response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
    	ctx.write(response);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

}
