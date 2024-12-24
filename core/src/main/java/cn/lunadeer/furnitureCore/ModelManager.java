package cn.lunadeer.furnitureCore;

import org.bukkit.NamespacedKey;

import java.util.HashMap;
import java.util.Map;

/**
 * Manager for models
 */
public class ModelManager {
    private final FurnitureCore plugin;
    private static ModelManager instance;

    private final Map<NamespacedKey, FurnitureModel> modelReady = new HashMap<>();


    public ModelManager(FurnitureCore plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public static ModelManager getInstance() {
        return instance;
    }

    /**
     * Register a model and
     */
    public void registerModel(FurnitureModel model) {
        modelReady.put(model.getItemModelKey(), model);
        model.registerRecipe();
    }

    /**
     * Get model by its key
     *
     * @param key model key
     * @return model if found, null if not found
     */
    public FurnitureModel getModel(NamespacedKey key) {
        return modelReady.get(key);
    }

    /**
     * Get model by callable name
     * @param modelName model name
     * @return model if found, null if not found
     */
    public FurnitureModel getModel(String modelName) {
        String name = modelName;
        if (name.contains(":")) {
            String[] p = name.split(":");
            if (!p[0].equals(FurnitureCore.getNamespace())) {
                return null;
            }
            name = p[1];
        }
        return modelReady.get(new NamespacedKey(FurnitureCore.getNamespace(), name));
    }

    /**
     * Unregister a model
     *
     * @param key model key
     */
    public void unregisterModel(NamespacedKey key) {
        modelReady.get(key).unregisterRecipe();
        modelReady.remove(key);
    }

    /**
     * Unregister all models
     */
    public void unregisterAllModels() {
        modelReady.forEach((key, model) -> model.unregisterRecipe());
        modelReady.clear();
    }

}
