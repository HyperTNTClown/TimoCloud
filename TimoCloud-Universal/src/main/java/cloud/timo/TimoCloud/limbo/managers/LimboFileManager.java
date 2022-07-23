package cloud.timo.TimoCloud.limbo.managers;

import cloud.timo.TimoCloud.limbo.TimoCloudLimbo;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.loohp.limbo.file.FileConfiguration;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.util.Arrays;

public class LimboFileManager {

    private File baseDirectory;
    private File signsPath;

    private File configFile;
    private File signTemplatesFile;
    private File signInstancesFile;

    private FileConfiguration config;
    private FileConfiguration signTemplates;

    public LimboFileManager() {
        load();
    }

    public void load() {
        try {
            baseDirectory = new File(TimoCloudLimbo.getInstance().getServer().getPluginFolder(), "/TimoCloud/");
            baseDirectory.mkdirs();
            TimoCloudLimbo.getInstance().info("Created directory: " + baseDirectory.getAbsolutePath());

            loadConfig();

        } catch (Exception e) {
            TimoCloudLimbo.getInstance().severe(e);
        }
    }

    private void loadConfig() {
        try {
            configFile = new File(baseDirectory, "config.yml");
            config = new FileConfiguration(configFile);

            TimoCloudLimbo.getInstance().info("Initialized config.yml at: " + configFile.getAbsolutePath());

            addConfigDefaults();
        } catch (Exception e) {
            TimoCloudLimbo.getInstance().severe("Error while loading config.yml: ");
            TimoCloudLimbo.getInstance().severe(e);
        }
    }


    private void addConfigDefaults() {
        config.set("prefix", "&6[&bTimo&fCloud&6]");
        config.set("defaultMapName", "Village");
        config.set("MotdToState.§aOnline", "ONLINE");
        config.set("PlayersToState.enabledWhileStates", Arrays.asList("WAITING", "LOBBY"));
        config.set("PlayersToState.percentages.100,0", "FULL");
        config.set("PlayersToState.percentages.50,0", "HALF_FULL");
        TimoCloudLimbo.getInstance().setPrefix(config.get("prefix", String.class));
        try {
            config.saveConfig(configFile);
        } catch (Exception e) {
            TimoCloudLimbo.getInstance().severe(e);
        }
    }

    public File getBaseDirectory() {
        return baseDirectory;
    }

    public File getConfigFile() {
        return configFile;
    }

    public FileConfiguration getConfig() {
        return config;
    }

}
