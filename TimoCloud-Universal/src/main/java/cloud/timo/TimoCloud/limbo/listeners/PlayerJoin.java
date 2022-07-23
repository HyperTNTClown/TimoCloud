package cloud.timo.TimoCloud.limbo.listeners;

import cloud.timo.TimoCloud.limbo.TimoCloudLimbo;
import com.loohp.limbo.Limbo;
import com.loohp.limbo.events.EventPriority;
import com.loohp.limbo.events.EventHandler;
import com.loohp.limbo.events.Listener;
import com.loohp.limbo.events.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        TimoCloudLimbo.getInstance().getServer().getScheduler().runTaskAsync(TimoCloudLimbo.getInstance(), () -> TimoCloudLimbo.getInstance().sendPlayers());
        TimoCloudLimbo.getInstance().getStateByEventManager().onPlayerJoin();
    }

}
