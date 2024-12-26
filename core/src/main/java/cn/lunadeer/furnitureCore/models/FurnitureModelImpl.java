package cn.lunadeer.furnitureCore.models;

import cn.lunadeer.furnitureCore.FurnitureCore;
import cn.lunadeer.furnitureCore.items.FurnitureItemStack;
import cn.lunadeer.furnitureCore.utils.ImageUtils;
import cn.lunadeer.furnitureCore.utils.JsonUtils;
import cn.lunadeer.furnitureCore.utils.XLogger;
import cn.lunadeer.furnitureCore.utils.ZipUtils;
import cn.lunadeer.furnitureCoreApi.models.FurnitureModel;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

import static cn.lunadeer.furnitureCore.utils.Common.DeleteFolderRecursively;

public class FurnitureModelImpl implements FurnitureModel {

    public static FurnitureModelImpl loadModel(File modelFile) throws Exception {
        File unzipCache = new File(FurnitureCore.getCacheDir(), "model_" + modelFile.getName().replace(".zip", ""));
        try {
            FurnitureModelImpl furnitureModel = new FurnitureModelImpl();

            // 1. unzip the file to cache directory
            ZipUtils.decompressFromZip(modelFile, unzipCache);
            File[] jsonFiles = unzipCache.listFiles((dir, name) -> name.endsWith(".json"));

            // 2. valid the json file exists then parse it to modelJson
            if (jsonFiles == null || jsonFiles.length == 0) {
                throw new Exception("Model json file not found.");
            }
            if (jsonFiles.length > 1) {
                XLogger.warn("Multiple json files found in model zip file %s, only the first one will be used.".formatted(modelFile.getAbsolutePath()));
            }
            furnitureModel.modelName = jsonFiles[0].getName().replace(".json", "");
            JSONObject json = JsonUtils.loadFromFile(jsonFiles[0]);
            furnitureModel.ambientocclusion = json.containsKey("ambientocclusion") ? json.getBoolean("ambientocclusion") : true;
            furnitureModel.display = json.containsKey("display") ? json.getJSONObject("display") : null;
            furnitureModel.gui_light = json.containsKey("gui_light") ? json.getString("gui_light") : "side";
            furnitureModel.groups = json.getJSONArray("groups");
            furnitureModel.elements = json.getJSONArray("elements");
            if (furnitureModel.elements == null) {
                throw new Exception("Elements not found in json model file.");
            }

            // 3. check if custom_name exists in json file then set it to model
            if (json.containsKey("custom_name")) {
                furnitureModel.customName = json.getString("custom_name");
            } else {
                furnitureModel.customName = furnitureModel.modelName;
            }

            // 4. valid the texture referenced in json file exists then load it to textures
            for (String key : json.getJSONObject("textures").keySet()) {
                String textureName = json.getJSONObject("textures").getString(key);
                File textureFile = new File(unzipCache, textureName + ".png");
                if (!textureFile.exists()) {
                    throw new Exception("Texture file not found: %s".formatted(textureFile.toString()));
                }
                BufferedImage texture = ImageUtils.loadImage(textureFile);
                if (texture == null) {
                    throw new Exception("Failed to load texture: %s".formatted(textureFile.toString()));
                }
                furnitureModel.textures.put(key, texture);
            }
            // 5. load recipes (cache this for save stage to parse)
            furnitureModel.recipesJson = json.getJSONArray("recipes");

            return furnitureModel;
        } finally {
            // 6. clean the cache directory in finally block
            if (unzipCache.exists()) {
                DeleteFolderRecursively(unzipCache);
            }
        }
    }

    private NamespacedKey itemModelKey;
    private String customName;
    private String modelName;
    private boolean ambientocclusion = true;
    private JSONArray elements;
    private JSONArray groups;
    private JSONObject display;
    private String gui_light = "side";
    private final Map<String, BufferedImage> textures = new HashMap<>();
    private JSONArray recipesJson;
    private final Map<NamespacedKey, CraftingRecipe> recipes = new HashMap<>();
    private boolean savedAndEffective = false;

    @Override
    public NamespacedKey getItemModelKey() {
        if (!savedAndEffective) {
            throw new IllegalStateException("Model not effective yet.");
        }
        return itemModelKey;
    }

    @Override
    public void setCustomName(String customName) {
        this.customName = customName;
    }

    @Override
    public String getCustomName() {
        return customName;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    private String prefixPath = "furniture";
    private String namespace = "minecraft";

    /**
     * Set the model prefix path.
     * <p>
     * e.g. "beds" will make the model saved to "assets/namespace/models/beds". Texture saved to "assets/namespace/textures/beds".
     * Callable name will be "namespace:beds/model_or_texture_name".
     *
     * @param path the path of the texture
     */
    public void setPrefixPath(String path) {
        if (savedAndEffective) {
            throw new IllegalStateException("Model already effective, cannot change prefix path.");
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        this.prefixPath = path;
    }

    @Override
    public String getPrefixPath() {
        if (!savedAndEffective) {
            throw new IllegalStateException("Model not effective yet, cannot get prefix path.");
        }
        return prefixPath;
    }

    /**
     * Set the namespace of the model
     *
     * @param namespace the namespace of the model
     */
    public void setNamespace(String namespace) {
        if (savedAndEffective) {
            throw new IllegalStateException("Model already effective, cannot change namespace.");
        }
        if (namespace.endsWith(":")) {
            namespace = namespace.substring(0, namespace.length() - 1);
        }
        this.namespace = namespace;
    }

    @Override
    public String getCallableNameNoNamespace() {
        if (!savedAndEffective) {
            throw new IllegalStateException("Model not effective yet.");
        }
        return prefixPath == null ? modelName : prefixPath + "/" + modelName;
    }

    @Override
    public String getCallableNameWithNamespace() {
        if (!savedAndEffective) {
            throw new IllegalStateException("Model not effective yet.");
        }
        return namespace + ":" + getCallableNameNoNamespace();
    }

    /**
     * Save the model to the asset path
     *
     * @param assetPath the asset path of the resource pack
     * @throws Exception if failed to save the model
     */
    public void save(File assetPath) throws Exception {
        // prepare save path
        // assets/<namespace>/textures
        File textureSavePath = new File(assetPath, namespace + "/textures");
        // assets/<namespace>/models
        File modelSavePath = new File(assetPath, namespace + "/models");
        // assets/<namespace>/items
        File itemModelPath = new File(assetPath, namespace + "/items");
        if (prefixPath != null) {
            textureSavePath = new File(textureSavePath, prefixPath);
            modelSavePath = new File(modelSavePath, prefixPath);
            itemModelPath = new File(itemModelPath, prefixPath);
        }
        if (!textureSavePath.exists() && !textureSavePath.mkdirs()) {
            throw new Exception("Failed to create texture save path: %s".formatted(textureSavePath.toString()));
        }
        if (!modelSavePath.exists() && !modelSavePath.mkdirs()) {
            throw new Exception("Failed to create model save path: %s".formatted(modelSavePath.toString()));
        }
        if (!itemModelPath.exists() && !itemModelPath.mkdirs()) {
            throw new Exception("Failed to create item model save path: %s".formatted(itemModelPath.toString()));
        }

        // generate model json
        JSONObject json = new JSONObject();
        JSONObject textures = new JSONObject();
        for (String key : this.textures.keySet()) {
            // generate texture file
            String name = this.modelName + "_" + key;
            File textureFile = new File(textureSavePath, name + ".png");
            ImageUtils.saveImage(this.textures.get(key), textureFile, "png");
            if (prefixPath != null) {
                name = prefixPath + "/" + name;
            }
            if (namespace != null) {
                name = namespace + ":" + name;
            }
            textures.put(key, name);
        }
        json.put("ambientocclusion", ambientocclusion);
        json.put("textures", textures);
        json.put("elements", elements);
        if (groups != null) {
            json.put("groups", groups);
        }
        if (display != null) {
            json.put("display", display);
        }
        json.put("gui_light", gui_light);
        JsonUtils.saveToFile(json, new File(modelSavePath, this.modelName + ".json"));

        savedAndEffective = true;   // from now on, the model is effective

        // generate item model json
        JSONObject itemModelJson = new JSONObject();
        JSONObject model = new JSONObject();
        model.put("type", "minecraft:model");
        model.put("model", getCallableNameWithNamespace());
        itemModelJson.put("model", model);
        JsonUtils.saveToFile(itemModelJson, new File(itemModelPath, this.modelName + ".json"));

        // generate item model key
        itemModelKey = new NamespacedKey(namespace, getCallableNameNoNamespace());

        // parse recipes
        if (recipesJson != null) {
            for (int i = 0; i < recipesJson.size(); i++) {
                try {
                    NamespacedKey pdcKey = new NamespacedKey(FurnitureCore.getNamespace(), "furniture_" + this.getModelName() + "_recipe_" + i);
                    JSONObject recipe = recipesJson.getJSONObject(i);
                    String type = Objects.requireNonNullElse(recipe.getString("type"), "shapeless");
                    if (type.equals("shapeless")) {
                        recipes.put(pdcKey, getShapelessRecipe(i, recipe, pdcKey, this));
                    } else if (type.equals("shaped")) {
                        recipes.put(pdcKey, getShapedRecipe(i, recipe, pdcKey, this));
                    } else {
                        throw new Exception("Unknown recipe type: %s".formatted(type));
                    }
                } catch (Exception e) {
                    XLogger.err("Model %s recipe %d failed to load: %s".formatted(this.getCallableNameWithNamespace(), i, e.getMessage()));
                }
            }
        }
    }

    @Override
    public void registerRecipe() {
        for (CraftingRecipe recipe : recipes.values()) {
            FurnitureCore.getInstance().getServer().addRecipe(recipe);
        }
    }

    @Override
    public void unregisterRecipe() {
        for (CraftingRecipe recipe : recipes.values()) {
            FurnitureCore.getInstance().getServer().removeRecipe(recipe.getKey());
        }
    }

    @Override
    public Map<NamespacedKey, CraftingRecipe> getRecipes() {
        return recipes;
    }

    @Override
    public Boolean isEffect() {
        return savedAndEffective;
    }

    private static ShapedRecipe getShapedRecipe(int i, JSONObject recipe, NamespacedKey pdcKey, FurnitureModel furnitureModel) throws Exception {
        ShapedRecipe shapedRecipe = new ShapedRecipe(pdcKey, new FurnitureItemStack(furnitureModel));
        JSONArray shape = recipe.getJSONArray("shape");
        if (shape == null) {
            throw new Exception("Shape not found in shaped recipe %d".formatted(i));
        }
        if (shape.size() > 3) {
            throw new Exception("Shape should not have more than 3 rows in shaped recipe %d".formatted(i));
        }
        String[] shapeList = new String[shape.size()];
        List<Character> keys = new ArrayList<>();
        for (int j = 0; j < shape.size(); j++) {
            String row = shape.getString(j);
            if (row == null) {
                throw new Exception("Row %d not available in shaped recipe %d".formatted(j, i));
            }
            if (row.length() > 3) {
                throw new Exception("Row %d should not have more than 3 characters in shaped recipe %d".formatted(j, i));
            }
            shapeList[j] = row;
            for (char c : row.toCharArray()) {
                if (!keys.contains(c)) {
                    keys.add(c);
                }
            }
        }
        shapedRecipe.shape(shapeList);
        JSONObject ingredients = recipe.getJSONObject("ingredients");
        if (ingredients == null) {
            throw new Exception("Ingredients not found in shaped recipe %d".formatted(i));
        }
        for (String key : ingredients.keySet()) {
            if (key.length() != 1) {
                throw new Exception("Key %s should have 1 character in shaped recipe %d".formatted(key, i));
            }
            if (!keys.contains(key.charAt(0))) {
                throw new Exception("Key %s not found in shape in shaped recipe %d".formatted(key, i));
            }
            String ingredient = ingredients.getString(key);
            if (ingredient == null) {
                throw new Exception("Ingredient %s not available in shaped recipe %d".formatted(key, i));
            }
            if (ingredient.startsWith("minecraft:")) {
                ingredient = ingredient.split(":")[1];
            }
            shapedRecipe.setIngredient(key.charAt(0), Material.valueOf(ingredient.toUpperCase()));
        }
        return shapedRecipe;
    }

    private static ShapelessRecipe getShapelessRecipe(int i, JSONObject recipe, NamespacedKey pdcKey, FurnitureModel furnitureModel) throws Exception {
        ShapelessRecipe shapelessRecipe = new ShapelessRecipe(pdcKey, new FurnitureItemStack(furnitureModel));
        JSONArray ingredients = recipe.getJSONArray("ingredients");
        if (ingredients == null) {
            throw new Exception("Ingredients not found in recipe %d".formatted(i));
        }
        for (int j = 0; j < ingredients.size(); j++) {
            String ingredient = ingredients.getString(j);
            if (ingredient == null) {
                throw new Exception("Ingredient %d not available in recipe %d".formatted(j, i));
            }
            if (ingredient.startsWith("minecraft:")) {
                ingredient = ingredient.split(":")[1];
            }
            shapelessRecipe.addIngredient(Material.valueOf(ingredient.toUpperCase()));
        }
        return shapelessRecipe;
    }

}
