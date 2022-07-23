package cloud.timo.TimoCloud.limbo.sockets;

import cloud.timo.TimoCloud.common.utils.network.NettyUtil;
import cloud.timo.TimoCloud.limbo.TimoCloudLimbo;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;

public class LimboSocketClient {

    public void init(String host, int port) throws Exception {
        EventLoopGroup group = NettyUtil.getEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NettyUtil.getSocketChannelClass())
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new LimboPipeline());
        ChannelFuture f = null;
        try {
            f = b.connect(host, port).sync();
        } catch (Exception e) {
            TimoCloudLimbo.getInstance().onSocketDisconnect(true);
            group.shutdownGracefully();
            return;
        }
        f.channel().closeFuture().addListener(future -> {
            TimoCloudLimbo.getInstance().onSocketDisconnect(false);
            group.shutdownGracefully();
        });
    }
}
