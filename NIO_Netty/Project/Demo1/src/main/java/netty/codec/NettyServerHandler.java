package netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

//服务器端业务处理类
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    //读取数据事件
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        BookMessage.Book book = (BookMessage.Book)msg;
        System.out.println("客户端发来的消息：" + book.getName());
    }
}
