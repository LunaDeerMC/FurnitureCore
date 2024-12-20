package cn.lunadeer.furnitureCore;

import cn.lunadeer.furnitureCore.utils.XLogger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ModelManager {
    private final JavaPlugin plugin;
    private static ModelManager instance;
    private final File modelDir;
    private final File indexFilePath;
    private YamlConfiguration indexFile;
    private List<Model> models = new ArrayList<>();


    public ModelManager(JavaPlugin plugin) {
        this.plugin = plugin;
        instance = this;
        modelDir = new File(plugin.getDataFolder(), "models");
        if (!modelDir.exists()) {
            if (!modelDir.mkdirs()) {
                XLogger.err("Failed to create models directory.");
            }
        }
        indexFilePath = new File(plugin.getDataFolder(), "index.yml");
        if (!indexFilePath.exists()) {
            indexFile = new YamlConfiguration();
        } else {
            indexFile = YamlConfiguration.loadConfiguration(indexFilePath);
        }

    }

    public void checkAndIndexModels() {
        // 1. list all zip files under models directory

        // 2. for each indexed model

        // - 1. check if the model zip still exists

        // - 2. try load model then set index

        // - 3. add the model to a list for later use

        // 3. if anything wrong, remove the model from index

        // 4. for each zip file

        // - 1. skip if the model is already indexed

        // - 2. skip the model removed in step 3

        // - 3. try load model then assign & set index

        // - 4. add the model to a list for later use


        try {
            indexFile.save(indexFilePath);
        } catch (Exception e) {
            XLogger.err("Failed to save index file: %s", e.getMessage());
        }
    }




    public static ModelManager getInstance() {
        return instance;
    }

    public File getModelDir() {
        return modelDir;
    }

}
