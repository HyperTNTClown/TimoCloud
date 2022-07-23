package cloud.timo.TimoCloud.limbo.sockets;

import cloud.timo.TimoCloud.common.sockets.BasicSocketClientHandler;
import cloud.timo.TimoCloud.limbo.TimoCloudLimbo;
import io.netty.channel.ChannelHandlerContext;

public class LimboSocketClientHandler extends BasicSocketClientHandler {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        TimoCloudLimbo.getInstance().info("Successfully connected to Core socket!");
        setChannel(ctx.channel());
        TimoCloudLimbo.getInstance().onSocketConnect(ctx.channel());
        flush();
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        //causTimoCloudBukkit.getInstance().severe(e);
        ctx.close();
        TimoCloudLimbo.getInstance().onSocketDisconnect(false);
    }

}
