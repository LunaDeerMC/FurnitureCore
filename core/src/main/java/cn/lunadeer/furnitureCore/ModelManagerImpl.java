package cn.lunadeer.furnitureCore;

import cn.lunadeer.furnitureCoreApi.managers.ModelManager;
import cn.lunadeer.furnitureCoreApi.models.FurnitureModel;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager for models
 */
public class ModelManagerImpl extends ModelManager {
    private final FurnitureCore plugin;

    private final Map<NamespacedKey, FurnitureModel> modelReady = new HashMap<>();


    public ModelManagerImpl(FurnitureCore plugin) {
        instance = this;
        this.plugin = plugin;
    }

    @Override
    public FurnitureModel getModel(NamespacedKey key) {
        return modelReady.get(key);
    }

    @Override
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
    
    @Override
    public List<FurnitureModel> getModels() {
        return new ArrayList<>(modelReady.values());
    }

    @Override
    public void registerModel(FurnitureModel model) {
        modelReady.put(model.getItemModelKey(), model);
        model.registerRecipe();
    }

    @Override
    public void unregisterModel(NamespacedKey key) {
        modelReady.get(key).unregisterRecipe();
        modelReady.remove(key);
    }

    @Override
    public void unregisterAllModels() {
        modelReady.forEach((key, model) -> model.unregisterRecipe());
        modelReady.clear();
    }

}
