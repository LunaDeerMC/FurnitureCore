package cn.lunadeer.furnitureCore.managers;

import cn.lunadeer.furnitureCore.FurnitureCore;
import cn.lunadeer.furnitureCore.Language;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationPart;
import cn.lunadeer.furnitureCoreApi.managers.ModelManager;
import cn.lunadeer.furnitureCoreApi.models.FurnitureModel;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager for models
 */
public class ModelManagerImpl extends ModelManager {

    public static class ModelManagerText extends ConfigurationPart {
        public static String modelNotEffect = "Model %s is not effect, cannot be registered.";
    }

    private final FurnitureCore plugin;
    private final Map<NamespacedKey, FurnitureModel> modelReady = new HashMap<>();

    public ModelManagerImpl(FurnitureCore plugin) {
        instance = this;
        this.plugin = plugin;
    }

    @Override
    public FurnitureModel getModel(@NotNull NamespacedKey key) {
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
    public void registerModel(@NotNull FurnitureModel model) throws IllegalArgumentException {
        if (!model.isEffect()) {
            throw new IllegalArgumentException(Language.modelManagerText.modelNotEffect.formatted(model.getModelName()));
        }
        modelReady.put(model.getItemModelKey(), model);
        model.registerInternalRecipe();
    }

    @Override
    public void unregisterModel(@NotNull NamespacedKey key) {
        modelReady.get(key).unregisterInternalRecipe();
        modelReady.remove(key);
    }

    @Override
    public void unregisterAllModels() {
        modelReady.forEach((key, model) -> model.unregisterInternalRecipe());
        modelReady.clear();
    }

}
