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
            ConfigurationManager.load(Configuration.class, new File(FurnitureCore.getInstance().getDataFolder(), "config.yml"));
            ConfigurationManager.save(Configuration.class, new File(FurnitureCore.getInstance().getDataFolder(), "config.yml"));
        } catch (Exception e) {
            XLogger.err("Failed to load configuration file: %s", e.getMessage());
        }
        XLogger.setDebug(Configuration.debug);
        XLogger.info("FurnitureCore is loading...");

        new bStatsMetrics(this, 24192);
        new ModelManager(this);

        // 1. ModelManage#loadAndIndexModels

        // 2. GenerateResourcePack#generate

        // 3. GenerateResourcePack#startServer

        // http://patorjk.com/software/taag/#p=display&f=Big&t=FurnitureCore
        XLogger.info("  ______                _ _                   _____");
        XLogger.info(" |  ____|              (_) |                 / ____|");
        XLogger.info(" | |__ _   _ _ __ _ __  _| |_ _   _ _ __ ___| |     ___  _ __ ___");
        XLogger.info(" |  __| | | | '__| '_ \\| | __| | | | '__/ _ \\ |    / _ \\| '__/ _ \\");
        XLogger.info(" | |  | |_| | |  | | | | | |_| |_| | | |  __/ |___| (_) | | |  __/");
        XLogger.info(" |_|   \\__,_|_|  |_| |_|_|\\__|\\__,_|_|  \\___|\\_____\\___/|_|  \\___|");
        XLogger.info(" ");
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
        return "furniture_core";
    }

    private static FurnitureCore instance;
}
