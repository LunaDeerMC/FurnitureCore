package cn.lunadeer.furnitureCore;

import cn.lunadeer.furnitureCore.commands.Apply;
import cn.lunadeer.furnitureCore.commands.Give;
import cn.lunadeer.furnitureCore.commands.Reload;
import cn.lunadeer.furnitureCore.events.*;
import cn.lunadeer.furnitureCore.managers.ModelManagerImpl;
import cn.lunadeer.furnitureCore.managers.ResourcePackManagerImpl;
import cn.lunadeer.furnitureCore.utils.Notification;
import cn.lunadeer.furnitureCore.utils.XLogger;
import cn.lunadeer.furnitureCore.utils.bStatsMetrics;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationManager;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationPart;
import cn.lunadeer.furnitureCoreApi.items.ScrewdriverItemStack;
import cn.lunadeer.furnitureCoreApi.managers.ResourcePackManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Objects;

public final class FurnitureCore extends JavaPlugin {

    public static class FurnitureCoreText extends ConfigurationPart {
        public String failToLoadConfig = "Failed to load configuration file: %s";
        public String failToLoadLanguage = "Failed to load language file: %s, using default language.";
        public String loading = "FurnitureCore is loading...";
        public String loaded = "FurnitureCore is loaded successfully!";
    }

    @Override
    public void onEnable() {
        instance = this;
        new Notification(this);
        new XLogger(this);
        // Load configuration file
        try {
            File configFile = new File(FurnitureCore.getInstance().getDataFolder(), "config.yml");
            ConfigurationManager.load(Configuration.class, configFile, "version");
        } catch (Exception e) {
            XLogger.err(Language.furnitureCoreText.failToLoadConfig, e.getMessage());
        }
        // Load language files
        try {
            final List<String> languages = List.of(
                    "languages/en_us.yml",
                    "languages/zh_cn.yml"
            );
            for (String language : languages) {
                this.saveResource(language, true);
            }
            File languageFile = new File(FurnitureCore.getInstance().getDataFolder(), "languages/" + Configuration.language + ".yml");
            ConfigurationManager.load(Language.class, languageFile, "version");
        } catch (Exception e) {
            XLogger.err(Language.furnitureCoreText.failToLoadLanguage, e.getMessage());
        }

        XLogger.setDebug(Configuration.debug);
        XLogger.info(Language.furnitureCoreText.loading);

        new bStatsMetrics(this, 24192);

        // http://patorjk.com/software/taag/#p=display&f=Big&t=FurnitureCore
        XLogger.info("  ______                _ _                   _____");
        XLogger.info(" |  ____|              (_) |                 / ____|");
        XLogger.info(" | |__ _   _ _ __ _ __  _| |_ _   _ _ __ ___| |     ___  _ __ ___");
        XLogger.info(" |  __| | | | '__| '_ \\| | __| | | | '__/ _ \\ |    / _ \\| '__/ _ \\");
        XLogger.info(" | |  | |_| | |  | | | | | |_| |_| | | |  __/ |___| (_) | | |  __/");
        XLogger.info(" |_|   \\__,_|_|  |_| |_|_|\\__|\\__,_|_|  \\___|\\_____\\___/|_|  \\___|");
        XLogger.info(" ");

        // commands
        Objects.requireNonNull(getCommand("furniture-give")).setExecutor(new Give());
        Objects.requireNonNull(getCommand("furniture-apply")).setExecutor(new Apply());
        Objects.requireNonNull(getCommand("furniture-reload")).setExecutor(new Reload());

        // Prepare recipe & event listeners
        getServer().addRecipe(ScrewdriverItemStack.getRecipe());
        getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
        getServer().getPluginManager().registerEvents(new PlaceFurniture(), this);
        getServer().getPluginManager().registerEvents(new BreakFurniture(), this);
        getServer().getPluginManager().registerEvents(new CraftFurniture(), this);
        getServer().getPluginManager().registerEvents(new RotateFurniture(), this);

        // Prepare managers do model stuff
        getCacheDir().mkdirs();
        new ModelManagerImpl(this);
        new ResourcePackManagerImpl(this);

        try {
            // 1. ModelManage#loadAndIndexModels
            ResourcePackManager.getInstance().loadModelsFromDisk();
            // 2. GenerateResourcePack
            ResourcePackManager.getInstance().generateResourcePack();
            // 3. StartHttpServerToProvideResourcePack
            ResourcePackManager.getInstance().startServer();

        } catch (Exception e) {
            XLogger.err("%s", e.getMessage());
        }

        XLogger.info(Language.furnitureCoreText.loaded);
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
        return Configuration.resourcePackSettings.namespace;
    }

    private static FurnitureCore instance;
}
