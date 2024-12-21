package cn.lunadeer.furnitureCore;

import cn.lunadeer.furnitureCore.utils.XLogger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModelManager {
    private final FurnitureCore plugin;
    private static ModelManager instance;
    private final File modelDir;
    private final File indexFilePath;
    private final YamlConfiguration indexFile;
    private final List<Model> models = new ArrayList<>();
    private int largestIndex = 0;


    public ModelManager(FurnitureCore plugin) {
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

    /**
     * Load and index models
     */
    public void loadAndIndexModels() throws Exception {
        // 1. list all zip files under models directory
        List<String> modelDirZipFilenames = new ArrayList<>();
        File[] zipFiles = modelDir.listFiles((dir, name) -> name.endsWith(".zip"));
        if (zipFiles == null) {
            throw new Exception("Failed to list files under dir %s".formatted(modelDir.getAbsolutePath()));
        }
        Arrays.stream(zipFiles).forEach(file -> modelDirZipFilenames.add(file.getName()));

        List<String> skipFilenames = new ArrayList<>();

        // 2. for each indexed model
        for (String key : indexFile.getKeys(false)) {
            int index;
            try {
                index = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                XLogger.err("Invalid index: %s", key);
                indexFile.set(key, null);
                continue;
            }
            // - 1. check if the model zip still exists
            String zipFileName = indexFile.getString(key);
            if (zipFileName == null) {
                indexFile.set(key, null);
                continue;
            }
            if (!modelDirZipFilenames.contains(zipFileName)) {
                XLogger.err("Model %s not found removing from index.", zipFileName);
                indexFile.set(key, null);
                continue;
            }
            skipFilenames.add(zipFileName);

            // - 2. try load model then set index
            File zipFile = new File(modelDir, zipFileName);
            try {
                Model model = Model.loadModel(zipFile);
                model.setIndex(index);
                if (model.getIndex() > largestIndex) {
                    largestIndex = model.getIndex();
                }

                // - 3. add the model to a list for later use
                models.add(model);
            } catch (Exception e) {
                // - 4. if anything wrong, remove the model from index
                XLogger.err("Failed to load model: %s", zipFile.getAbsoluteFile().toString());
                XLogger.err("Reason: %s", e.getMessage());
                indexFile.set(key, null);
            }
        }

        // 3. for each zip file
        for (String zipFilename : modelDirZipFilenames) {
            // - 1. skip if the model is already processed
            if (skipFilenames.contains(zipFilename)) {
                continue;
            }
            File zipFile = new File(modelDir, zipFilename);

            // - 2. try load model then assign & set index
            try {
                Model model = Model.loadModel(zipFile);
                largestIndex++;
                model.setIndex(largestIndex);
                indexFile.set(String.valueOf(largestIndex), zipFilename);
                // - 3. add the model to a list for later use
                models.add(model);
            } catch (Exception e) {
                XLogger.err("Failed to load model: %s", zipFile.getAbsoluteFile().toString());
                XLogger.err("Reason: %s", e.getMessage());
            }
        }

        XLogger.info("Loaded & indexed %d models.", models.size());
        for (Model model : models) {
            XLogger.info("Model %d: %s", model.getIndex(), model.getModelName());
        }

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

    public List<Model> getModels() {
        return models;
    }

    /**
     * Remove a model from index if something wrong externally
     *
     * @param index model index to remove
     */
    public void removeIndexedModel(int index) {
        models.removeIf(model -> model.getIndex() == index);
        indexFile.set(String.valueOf(index), null);
        try {
            indexFile.save(indexFilePath);
        } catch (Exception e) {
            XLogger.err("Failed to save index file: %s", e.getMessage());
        }
    }

}
