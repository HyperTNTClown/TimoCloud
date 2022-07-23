package cloud.timo.TimoCloud.limbo.api;

import cloud.timo.TimoCloud.api.TimoCloudAPI;
import cloud.timo.TimoCloud.api.TimoCloudLimboAPI;
import cloud.timo.TimoCloud.api.objects.ServerObject;
import cloud.timo.TimoCloud.limbo.TimoCloudLimbo;

public class TimoCloudLimboAPIImplementation implements TimoCloudLimboAPI {
    @Override
    public ServerObject getThisServer() {
        return TimoCloudAPI.getUniversalAPI().getServer(TimoCloudLimbo.getInstance().getServerName());
    }
}
