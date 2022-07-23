package cloud.timo.TimoCloud.limbo.managers;

import cloud.timo.TimoCloud.api.TimoCloudAPI;
import cloud.timo.TimoCloud.limbo.TimoCloudLimbo;

import java.text.NumberFormat;
import java.util.*;

public class StateByEventManager {
    private String stateBefore;
    private int setByPlayer = 0;
    private String lastStateSet;

    private void setState(String state) {
        TimoCloudAPI.getLimboAPI().getThisServer().setState(state);
    }

    public void setStateByMotd(String motd) {
        Object state = TimoCloudLimbo.getInstance().getFileManager().getConfig().get("MotdToState." + motd.trim(), String.class);
        if (state != null && state instanceof String) {
            setState((String) state);
        }
    }

    public void onPlayerJoin() {
        TimoCloudLimbo.getInstance().getServer().getScheduler().runTaskAsync(TimoCloudLimbo.getInstance(), this::setStateByPlayerCount);
    }

    public void onPlayerQuit() {
        TimoCloudLimbo.getInstance().getServer().getScheduler().runTaskAsync(TimoCloudLimbo.getInstance(), this::setStateByPlayerCount);
    }

    public void setStateByPlayerCount() {
        int cur = TimoCloudLimbo.getInstance().getOnlinePlayersAmount();
        String currentState = TimoCloudAPI.getLimboAPI().getThisServer().getState();
        if (!currentState.equals(lastStateSet)) stateBefore = currentState;
        if (!TimoCloudLimbo.getInstance().getFileManager().getConfig().get("PlayersToState.enabledWhileStates", ArrayList.class).contains(stateBefore))
            return;
        double percentage = (double) cur / (double) TimoCloudLimbo.getInstance().getMaxPlayersAmount() * 100;
        String state = null;
        List<Double> steps = new ArrayList<>();
        Map<Double, String> states = new HashMap<>();
        String step = TimoCloudLimbo.getInstance().getFileManager().getConfig().get("PlayersToState.percentages.100,0", String.class);
        try {
            Double d = NumberFormat.getInstance(Locale.GERMAN).parse(step).doubleValue();
            steps.add(d);
            states.put(d, TimoCloudLimbo.getInstance().getFileManager().getConfig().get("PlayersToState.percentages." + step, String.class));
        } catch (Exception e) {
        }
        step = TimoCloudLimbo.getInstance().getFileManager().getConfig().get("PlayersToState.percentages.50,0", String.class);
        try {
            Double d = NumberFormat.getInstance(Locale.GERMAN).parse(step).doubleValue();
            steps.add(d);
            states.put(d, TimoCloudLimbo.getInstance().getFileManager().getConfig().get("PlayersToState.percentages." + step, String.class));
        } catch (Exception e) {
        }
        Collections.sort(steps);
        for (double _step : steps) {
            if (percentage >= _step) state = states.get(_step);
        }
        if (state == null) {
            setState(stateBefore);
        } else {
            setState(state);
            lastStateSet = state;
        }
    }
}
