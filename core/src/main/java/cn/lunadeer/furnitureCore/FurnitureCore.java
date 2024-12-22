package cn.lunadeer.furnitureCore;

import cn.lunadeer.furnitureCore.utils.Notification;
import cn.lunadeer.furnitureCore.utils.XLogger;
import cn.lunadeer.furnitureCore.utils.bStatsMetrics;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class FurnitureCore extends JavaPlugin {

    @Override
    public void onEnable() {
        instance = this;
        new Notification(this);
        new XLogger(this);
        try {
            ConfigurationManager.load(Configuration.class, new File(FurnitureCore.getInstance().getDataFolder(), "config.yml"), "version");
        } catch (Exception e) {
            XLogger.err("Failed to load configuration file: %s", e.getMessage());
        }
        XLogger.setDebug(Configuration.debug);
        XLogger.info("FurnitureCore is loading...");

        new bStatsMetrics(this, 24192);

        // http://patorjk.com/software/taag/#p=display&f=Big&t=FurnitureCore
        XLogger.info("  ______                _ _                   _____");
        XLogger.info(" |  ____|              (_) |                 / ____|");
        XLogger.info(" | |__ _   _ _ __ _ __  _| |_ _   _ _ __ ___| |     ___  _ __ ___");
        XLogger.info(" |  __| | | | '__| '_ \\| | __| | | | '__/ _ \\ |    / _ \\| '__/ _ \\");
        XLogger.info(" | |  | |_| | |  | | | | | |_| |_| | | |  __/ |___| (_) | | |  __/");
        XLogger.info(" |_|   \\__,_|_|  |_| |_|_|\\__|\\__,_|_|  \\___|\\_____\\___/|_|  \\___|");
        XLogger.info(" ");

        // Prepare managers do model stuff
        getCacheDir().mkdirs();
        new ModelManager(this);
        new ResourcePackManager(this);

        try {
            // 1. ModelManage#loadAndIndexModels
            ModelManager.getInstance().loadAndIndexModels();
            // 2. GenerateResourcePack#generate
            ResourcePackManager.getInstance().generate();
            // 3. GenerateResourcePack#startServer
            ResourcePackManager.getInstance().startServer(Configuration.resourcePackServer.host, Configuration.resourcePackServer.port);

        } catch (Exception e) {
            XLogger.err("%s", e.getMessage());
        }

        XLogger.info("FurnitureCore is loaded!");
        XLogger.debug("Debug mode is enabled.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static FurnitureCore getInstance() {
        return instance;
    }

    public static File getCacheDir() {
        return new File(FurnitureCore.getInstance().getDataFolder(), "cache");
    }

    public static String getNamespace() {
        return Configuration.namespace;
    }

    private static FurnitureCore instance;
}
