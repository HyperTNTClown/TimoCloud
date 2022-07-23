package cloud.timo.TimoCloud.limbo.sockets;

import cloud.timo.TimoCloud.common.sockets.PacketLengthPrepender;
import cloud.timo.TimoCloud.common.sockets.PacketLengthSplitter;
import cloud.timo.TimoCloud.limbo.TimoCloudLimbo;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

public class LimboPipeline extends ChannelInitializer<Channel> {
    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast("splitter", new PacketLengthSplitter());
        ch.pipeline().addLast(TimoCloudLimbo.getInstance().getSocketClientHandler());
        ch.pipeline().addLast("prepender", new PacketLengthPrepender());
    }
}
