package cloud.timo.TimoCloud.limbo;

import cloud.timo.TimoCloud.api.TimoCloudAPI;
import cloud.timo.TimoCloud.api.implementations.TimoCloudUniversalAPIBasicImplementation;
import cloud.timo.TimoCloud.api.implementations.internal.TimoCloudInternalImplementationAPIBasicImplementation;
import cloud.timo.TimoCloud.api.implementations.managers.APIResponseManager;
import cloud.timo.TimoCloud.api.implementations.managers.EventManager;
import cloud.timo.TimoCloud.api.utils.APIInstanceUtil;
import cloud.timo.TimoCloud.bukkit.TimoCloudBukkit;
import cloud.timo.TimoCloud.limbo.managers.StateByEventManager;
import cloud.timo.TimoCloud.common.encryption.RSAKeyPairRetriever;
import cloud.timo.TimoCloud.common.global.logging.TimoCloudLogger;
import cloud.timo.TimoCloud.common.log.utils.LogInjectionUtil;
import cloud.timo.TimoCloud.common.protocol.Message;
import cloud.timo.TimoCloud.common.protocol.MessageType;
import cloud.timo.TimoCloud.common.sockets.AESDecrypter;
import cloud.timo.TimoCloud.common.sockets.AESEncrypter;
import cloud.timo.TimoCloud.common.sockets.RSAHandshakeHandler;
import cloud.timo.TimoCloud.common.utils.network.InetAddressUtil;
import cloud.timo.TimoCloud.limbo.api.TimoCloudInternalMessageAPILimboImplementation;
import cloud.timo.TimoCloud.limbo.api.TimoCloudLimboAPIImplementation;
import cloud.timo.TimoCloud.limbo.api.TimoCloudMessageAPILimboImplementation;
import cloud.timo.TimoCloud.limbo.api.TimoCloudUniversalAPILimboImplementation;
import cloud.timo.TimoCloud.limbo.listeners.PlayerJoin;
import cloud.timo.TimoCloud.limbo.listeners.PlayerQuit;
import cloud.timo.TimoCloud.limbo.managers.LimboFileManager;
import cloud.timo.TimoCloud.limbo.sockets.LimboSocketClient;
import cloud.timo.TimoCloud.limbo.sockets.LimboSocketClientHandler;
import cloud.timo.TimoCloud.limbo.sockets.LimboSocketMessageManager;
import cloud.timo.TimoCloud.limbo.sockets.LimboStringHandler;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.loohp.limbo.Limbo;
import com.loohp.limbo.events.status.StatusPingEvent;
import com.loohp.limbo.plugins.LimboPlugin;
import io.netty.channel.Channel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import com.loohp.limbo.player.Player;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.security.KeyPair;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TimoCloudLimbo extends LimboPlugin implements TimoCloudLogger {

    private static TimoCloudLimbo instance;
    private LimboFileManager fileManager;
    private LimboSocketClientHandler socketClientHandler;
    private LimboSocketMessageManager socketMessageManager;
    private StateByEventManager stateByEventManager;
    private LimboStringHandler stringHandler;
    private String prefix = "[TimoCloud] ";
    private boolean enabled = false;
    private boolean disabling = false;
    private boolean serverRegistered = false;

    public static TimoCloudLimbo getInstance() {
        return instance;
    }

    @Override
    public void info(String message) {
        Limbo.getInstance().getConsole().sendMessage(getPrefix() + message);
    }

    @Override
    public void warning(String message) {
        Limbo.getInstance().getConsole().sendMessage(getPrefix() + "§c" + message);
    }

    @Override
    public void severe(String message) {
        Limbo.getInstance().getConsole().sendMessage(getPrefix() + "§c" + message);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void onEnable() {
        this.disabling = false;
        if (this.enabled) {
            registerListeners();
            registerTasks();
        } else {
            try {
                info("&eEnabling &bTimoCloudLimbo&r &eversion &7[&6" + getInfo().getVersion() + "&7]&e...");

                makeInstances();
                registerListeners();
                registerTasks();
                LogInjectionUtil.saveSystemOutAndErr();
                Executors.newScheduledThreadPool(1).scheduleAtFixedRate(this::doEverySecond, 1L, 1L, TimeUnit.SECONDS);
                Executors.newSingleThreadExecutor().submit(this::connectToCore);
                long timeToTimeout = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30);
                while (!((TimoCloudUniversalAPIBasicImplementation) TimoCloudAPI.getUniversalAPI()).gotAnyData()) {
                    //Timeout?
                    if (timeToTimeout < System.currentTimeMillis()) {
                        LogInjectionUtil.restoreSystemOutAndErr();
                        severe("&Connection to the core could not be established");
                        System.exit(0);
                        return;
                    }
                    try {
                        Thread.sleep(50); // Wait until we get the API data
                    } catch (Exception ignored) {
                    }
                }
                LogInjectionUtil.restoreSystemOutAndErr();
                this.enabled = true;
                info("&aTimoCloudLimbo has been enabled!");
            } catch (Exception e) {
                severe("Error while enabling TimoCloudLimbo: ");
                TimoCloudLimbo.getInstance().severe(e);
            }
        }
    }

    private void connectToCore() {
        try {
            info("Connecting to TimoCloudCore socket on " + getTimoCloudCoreIP() + ":" + getTimoCloudCoreSocketPort() + "...");
            new LimboSocketClient().init(getTimoCloudCoreIP(), getTimoCloudCoreSocketPort());
        } catch (Exception e) {
            TimoCloudLimbo.getInstance().severe(e);
        }
    }

    private int getTimoCloudCoreSocketPort() {
        return Integer.parseInt(System.getProperty("timocloud-corehost").split(":")[1]);
    }

    private String getTimoCloudCoreIP() {
        return System.getProperty("timocloud-corehost").split(":")[0];
    }

    private void registerListeners() {
        Limbo.getInstance().getEventsManager().registerEvents(this, new PlayerJoin());
        Limbo.getInstance().getEventsManager().registerEvents(this, new PlayerQuit());
    }

    private void makeInstances() throws Exception {
        instance = this;

        TimoCloudLogger.setLogger(this);

        fileManager = new LimboFileManager();
        socketClientHandler = new LimboSocketClientHandler();
        socketMessageManager = new LimboSocketMessageManager();
        stringHandler = new LimboStringHandler();
        stateByEventManager = new StateByEventManager();

        APIInstanceUtil.setInternalMessageInstance(new TimoCloudInternalMessageAPILimboImplementation());
        APIInstanceUtil.setEventInstance(new EventManager());
        APIInstanceUtil.setUniversalInstance(new TimoCloudUniversalAPILimboImplementation());
        APIInstanceUtil.setLimboInstance(new TimoCloudLimboAPIImplementation());
        APIInstanceUtil.setMessageInstance(new TimoCloudMessageAPILimboImplementation());
        APIInstanceUtil.setInternalImplementationAPIInstance(new TimoCloudInternalImplementationAPIBasicImplementation());
        TimoCloudAPI.getMessageAPI().registerMessageListener(new APIResponseManager(), "TIMOCLOUD_API_RESPONSE");    }

    @Override
    public void onDisable() {
        this.disabling = true;
        info("&chas been disabled!");
    }

    private void doEverySecond() {
        if (this.disabling) return;
        if (!this.serverRegistered) return;
        sendEverything();
    }

    private void sendEverything() {
        sendMotds();
        TimoCloudLimbo.getInstance().getServer().getScheduler().runTaskAsync(TimoCloudLimbo.getInstance(), () -> TimoCloudLimbo.getInstance().sendPlayers());
    }

    public void sendPlayers() {
        getSocketMessageManager().sendMessage(Message.create().setType(MessageType.SERVER_SET_PLAYERS).setData(getOnlinePlayersAmount() + "/" + getMaxPlayersAmount()));
    }

    public LimboSocketMessageManager getSocketMessageManager() {
        return socketMessageManager;
    }

    private void sendMotds() {
        try {
            Constructor<StatusPingEvent> eventConstructor;
            try {
                eventConstructor = StatusPingEvent.class.getConstructor(InetAddress.class, String.class, int.class, int.class);
                StatusPingEvent event = eventConstructor.newInstance(InetAddressUtil.getLocalHost(), Limbo.getInstance().getServerProperties().getMotd(), Limbo.getInstance().getPlayers().size(), Limbo.getInstance().getServerProperties().getMaxPlayers());
                Limbo.getInstance().getEventsManager().callEvent(event);
                getSocketMessageManager().sendMessage(Message.create().setType(MessageType.SERVER_SET_MOTD).setData(event.getMotd()));
                getStateByEventManager().setStateByMotd(event.getMotd().toString().trim());
            } catch (NoSuchMethodException e) {
                eventConstructor = StatusPingEvent.class.getConstructor(InetAddress.class, String.class, boolean.class, int.class, int.class);
                StatusPingEvent event = eventConstructor.newInstance(InetAddressUtil.getLocalHost(), Limbo.getInstance().getServerProperties().getMotd(), false, Limbo.getInstance().getPlayers().size(), Limbo.getInstance().getServerProperties().getMaxPlayers());
                Limbo.getInstance().getEventsManager().callEvent(event);
                Limbo.getInstance().getEventsManager().callEvent(event);
                getSocketMessageManager().sendMessage(Message.create().setType(MessageType.SERVER_SET_MOTD).setData(event.getMotd()));
                getStateByEventManager().setStateByMotd(event.getMotd().toString().trim());
            }
        } catch (Exception e) {
            severe("Error while sending MOTD: ");
            TimoCloudLimbo.getInstance().severe(e);
            getSocketMessageManager().sendMessage(Message.create().setType(MessageType.SERVER_SET_MOTD).setData(Limbo.getInstance().getServerProperties().getMotd()));
        }
    }

    public StateByEventManager getStateByEventManager() {
        return stateByEventManager;
    }


    public int getOnlinePlayersAmount() {
        try {
            Constructor<StatusPingEvent> eventConstructor;
            try {
                eventConstructor = StatusPingEvent.class.getConstructor(InetAddress.class, String.class, int.class, int.class);
                StatusPingEvent event = eventConstructor.newInstance(InetAddressUtil.getLocalHost(), Limbo.getInstance().getServerProperties().getMotd(), Limbo.getInstance().getPlayers().size(), Limbo.getInstance().getServerProperties().getMaxPlayers());
                Limbo.getInstance().getEventsManager().callEvent(event);
                return event.getPlayersOnline();
            } catch (NoSuchMethodException e) {
                eventConstructor = StatusPingEvent.class.getConstructor(InetAddress.class, String.class, boolean.class, int.class, int.class);
                StatusPingEvent event = eventConstructor.newInstance(InetAddressUtil.getLocalHost(), Limbo.getInstance().getServerProperties().getMotd(), false, Limbo.getInstance().getPlayers().size(), Limbo.getInstance().getServerProperties().getMaxPlayers());
                Limbo.getInstance().getEventsManager().callEvent(event);
                return event.getPlayersOnline();
            }
        } catch (Exception e) {
            severe("Error while calling ServerListPingEvent: ");
            TimoCloudLimbo.getInstance().severe(e);
            return Limbo.getInstance().getPlayers().size();
        }
    }

    public int getMaxPlayersAmount() {
        try {
            Constructor<StatusPingEvent> eventConstructor;
            try {
                eventConstructor = StatusPingEvent.class.getConstructor(InetAddress.class, String.class, int.class, int.class);
                StatusPingEvent event = eventConstructor.newInstance(InetAddressUtil.getLocalHost(), Limbo.getInstance().getServerProperties().getMotd(), Limbo.getInstance().getPlayers().size(), Limbo.getInstance().getServerProperties().getMaxPlayers());
                Limbo.getInstance().getEventsManager().callEvent(event);
                return event.getMaxPlayers();
            } catch (NoSuchMethodException e) {
                eventConstructor = StatusPingEvent.class.getConstructor(InetAddress.class, String.class, boolean.class, int.class, int.class);
                StatusPingEvent event = eventConstructor.newInstance(InetAddressUtil.getLocalHost(), Limbo.getInstance().getServerProperties().getMotd(), false, Limbo.getInstance().getPlayers().size(), Limbo.getInstance().getServerProperties().getMaxPlayers());
                Limbo.getInstance().getEventsManager().callEvent(event);
                return event.getMaxPlayers();
            }
        } catch (Exception e) {
            severe("Error while calling ServerListPingEvent: ");
            TimoCloudLimbo.getInstance().severe(e);
            return Limbo.getInstance().getServerProperties().getMaxPlayers();
        }
    }

    public LimboFileManager getFileManager() {
        return fileManager;
    }

    public LimboStringHandler getStringHandler() {
        return stringHandler;
    }

    public void onSocketConnect(Channel channel) {
        try {
            KeyPair keyPair = new RSAKeyPairRetriever(new File(getFileManager().getBaseDirectory(), "/keys/")).getKeyPair();
            new RSAHandshakeHandler(channel, keyPair, (aesKey -> {
                channel.pipeline().addBefore("prepender", "decrypter", new AESDecrypter(aesKey));
                channel.pipeline().addBefore("prepender", "decoder", new StringDecoder(CharsetUtil.UTF_8));
                channel.pipeline().addBefore("prepender", "handler", getStringHandler());
                channel.pipeline().addLast("encrypter", new AESEncrypter(aesKey));
                channel.pipeline().addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));

                getSocketMessageManager().sendMessage(Message.create().setType(MessageType.SERVER_HANDSHAKE).setTarget(getServerId()));
            })).startHandshake();
        } catch (Exception e) {
            severe("Error during public key authentification, please report this!");
            e.printStackTrace();
        }
    }

    private void registerTasks() {
        getServer().getScheduler().runTaskLater(this, this::registerAtCore, 100L);
    }

    private void registerAtCore() {
        info("Registering at core...");
        getSocketMessageManager().sendMessage(Message.create().setType(MessageType.SERVER_REGISTER).setTarget(getServerId()));
        getSocketMessageManager().sendMessage(Message.create().setType(MessageType.SERVER_SET_MAP).setData(getMapName()));
    }

    public void onSocketDisconnect(boolean connectionFailed) {
        LogInjectionUtil.restoreSystemOutAndErr();
        info("Disconnected from TimoCloudCore. Stopping server.");
        if (connectionFailed) {
            System.exit(0);
        } else {
            if (true) stop();
        }
    }

    public void stop() {
        Limbo.getInstance().getScheduler().runTask(this, () -> Limbo.getInstance().stopServer());
    }

    public void onHandshakeSuccess() {
        LogInjectionUtil.injectSystemOutAndErr(logEntry ->
                getSocketMessageManager().sendMessage(Message.create()
                        .setType(MessageType.SERVER_LOG_ENTRY)
                        .setData(logEntry)));
        requestApiData();
        doEverySecond();
    }

    private void requestApiData() {
        getSocketMessageManager().sendMessage(Message.create().setType(MessageType.GET_API_DATA));
    }

    public LimboSocketClientHandler getSocketClientHandler() {
        return socketClientHandler;
    }

    public String getServerName() {
        return System.getProperty("timocloud-servername");
    }

    public String getServerId() {
        return System.getProperty("timocloud-serverid");
    }

    public String getMapName() {
        return "Spawn.schem";
    }
}
