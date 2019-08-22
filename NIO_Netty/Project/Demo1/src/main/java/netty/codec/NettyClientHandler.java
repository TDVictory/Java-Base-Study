package netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

//客户端业务处理类
public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        BookMessage.Book book = BookMessage.Book.newBuilder().setId(1).setName("Java从入门到精通").build();
        ctx.writeAndFlush(book);
    }
}
