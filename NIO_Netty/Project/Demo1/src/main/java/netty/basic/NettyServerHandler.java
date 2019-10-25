package netty.basic;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.nio.ByteBuffer;

//服务器端业务处理类
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    //读取数据事件
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("调试用，ctx为：" + ctx);
        ByteBuf buf = (ByteBuf)msg;
        System.out.println("客户端发来的消息：" + buf.toString(CharsetUtil.UTF_8));
    }

    //数据读取完毕事件
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.copiedBuffer("没钱",CharsetUtil.UTF_8));
    }

    //异常发生事件
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("服务器发生异常！");
        ctx.close();
    }
}
