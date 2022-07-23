package cloud.timo.TimoCloud.limbo.api;

import cloud.timo.TimoCloud.api.TimoCloudUniversalAPI;
import cloud.timo.TimoCloud.api.implementations.TimoCloudUniversalAPIBasicImplementation;

public class TimoCloudUniversalAPILimboImplementation extends TimoCloudUniversalAPIBasicImplementation implements TimoCloudUniversalAPI {

    public TimoCloudUniversalAPILimboImplementation() {
        super(ServerObjectLimboImplementation.class, ProxyObjectLimboImplementation.class, ServerGroupObjectLimboImplementation.class, ProxyGroupObjectLimboImplementation.class, PlayerObjectLimboImplementation.class, BaseObjectLimboImplementation.class, CordObjectLimboImplementation.class);
    }

}
