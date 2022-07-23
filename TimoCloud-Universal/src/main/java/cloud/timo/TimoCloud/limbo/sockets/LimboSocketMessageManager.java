package cloud.timo.TimoCloud.limbo.sockets;

import cloud.timo.TimoCloud.common.protocol.Message;
import cloud.timo.TimoCloud.limbo.TimoCloudLimbo;

public class LimboSocketMessageManager {

    public void sendMessage(Message message) {
        TimoCloudLimbo.getInstance().getSocketClientHandler().sendMessage(message.toJson());
    }
}
