package cloud.timo.TimoCloud.limbo.sockets;

import cloud.timo.TimoCloud.api.TimoCloudAPI;
import cloud.timo.TimoCloud.api.events.EventType;
import cloud.timo.TimoCloud.api.implementations.TimoCloudMessageAPIBasicImplementation;
import cloud.timo.TimoCloud.api.implementations.TimoCloudUniversalAPIBasicImplementation;
import cloud.timo.TimoCloud.api.implementations.managers.EventManager;
import cloud.timo.TimoCloud.api.messages.objects.AddressedPluginMessage;
import cloud.timo.TimoCloud.api.utils.EventUtil;
import cloud.timo.TimoCloud.common.protocol.Message;
import cloud.timo.TimoCloud.common.protocol.MessageType;
import cloud.timo.TimoCloud.common.sockets.BasicStringHandler;
import cloud.timo.TimoCloud.common.utils.EnumUtil;
import cloud.timo.TimoCloud.common.utils.PluginMessageSerializer;
import cloud.timo.TimoCloud.limbo.TimoCloudLimbo;
import cloud.timo.TimoCloud.limbo.api.TimoCloudUniversalAPILimboImplementation;
import com.loohp.limbo.Limbo;
import io.netty.channel.Channel;

import java.util.Map;

public class LimboStringHandler extends BasicStringHandler {


    @Override
    public void handleMessage(Message message, String originalMessage, Channel channel) {
        if (message == null) {
            TimoCloudLimbo.getInstance().severe("Error while parsing json (json is null): " + originalMessage);
            return;
        }
        MessageType type = message.getType();
        Object data = message.getData();
        switch (type) {
            case SERVER_HANDSHAKE_SUCCESS:
                TimoCloudLimbo.getInstance().onHandshakeSuccess();
                break;
            case API_DATA:
                ((TimoCloudUniversalAPILimboImplementation) TimoCloudAPI.getUniversalAPI()).setData((Map<String, Object>) data);
                break;
            case EVENT_FIRED:
                try {
                    EventType eventType = EnumUtil.valueOf(EventType.class, (String) message.get("eT"));
                    ((EventManager) TimoCloudAPI.getEventAPI()).callEvent(((TimoCloudUniversalAPIBasicImplementation) TimoCloudAPI.getUniversalAPI()).getObjectMapper().readValue((String) data, EventUtil.getClassByEventType(eventType)));
                } catch (Exception e) {
                    System.err.println("Error while parsing event from json: ");
                    TimoCloudLimbo.getInstance().severe(e);
                }
                break;
            case SERVER_EXECUTE_COMMAND:
                Limbo.getInstance().getScheduler().runTask(TimoCloudLimbo.getInstance(), () -> TimoCloudLimbo.getInstance().getServer().dispatchCommand(TimoCloudLimbo.getInstance().getServer().getConsole(), (String) data));
                break;
            case ON_PLUGIN_MESSAGE: {
                AddressedPluginMessage addressedPluginMessage = PluginMessageSerializer.deserialize((Map) data);
                ((TimoCloudMessageAPIBasicImplementation) TimoCloudAPI.getMessageAPI()).onMessage(addressedPluginMessage);
                break;
            }
            case SERVER_STOP: {
                TimoCloudLimbo.getInstance().stop();
                break;
            }
            default:
                TimoCloudLimbo.getInstance().severe("Error: Could not categorize json message: " + message);
        }
    }
}
