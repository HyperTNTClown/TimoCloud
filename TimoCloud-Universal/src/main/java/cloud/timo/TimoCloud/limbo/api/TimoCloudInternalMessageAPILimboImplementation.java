package cloud.timo.TimoCloud.limbo.api;

import cloud.timo.TimoCloud.api.internal.TimoCloudInternalMessageAPI;
import cloud.timo.TimoCloud.limbo.TimoCloudLimbo;

public class TimoCloudInternalMessageAPILimboImplementation implements TimoCloudInternalMessageAPI {
    @Override
    public void sendMessageToCore(String message) {
        TimoCloudLimbo.getInstance().getSocketClientHandler().sendMessage(message);
    }
}
