package cloud.timo.TimoCloud.limbo.listeners;

import cloud.timo.TimoCloud.limbo.TimoCloudLimbo;
import com.loohp.limbo.Limbo;
import com.loohp.limbo.events.EventPriority;
import com.loohp.limbo.events.EventHandler;
import com.loohp.limbo.events.Listener;
import com.loohp.limbo.events.player.PlayerQuitEvent;

public class PlayerQuit implements Listener {
    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        TimoCloudLimbo.getInstance().getServer().getScheduler().runTaskAsync(TimoCloudLimbo.getInstance(), () -> TimoCloudLimbo.getInstance().sendPlayers());
        Limbo.getInstance().getScheduler().runTaskLater(TimoCloudLimbo.getInstance(), () -> {
            TimoCloudLimbo.getInstance().getStateByEventManager().onPlayerQuit();
        }, 1L);
    }
}
