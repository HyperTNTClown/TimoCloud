package cloud.timo.TimoCloud.limbo.api;

import cloud.timo.TimoCloud.api.TimoCloudMessageAPI;
import cloud.timo.TimoCloud.api.implementations.TimoCloudMessageAPIBasicImplementation;
import cloud.timo.TimoCloud.api.messages.objects.MessageClientAddress;
import cloud.timo.TimoCloud.api.messages.objects.MessageClientAddressType;
import cloud.timo.TimoCloud.bukkit.TimoCloudBukkit;

public class TimoCloudMessageAPILimboImplementation extends TimoCloudMessageAPIBasicImplementation implements TimoCloudMessageAPI {
    @Override
    public MessageClientAddress getOwnAddress() {
        return new MessageClientAddress(TimoCloudBukkit.getInstance().getServerId(), MessageClientAddressType.SERVER);
    }
}
