package cn.lunadeer.furnitureCore.models;

import cn.lunadeer.furnitureCore.Configuration;
import cn.lunadeer.furnitureCore.FurnitureCore;
import cn.lunadeer.furnitureCore.Language;
import cn.lunadeer.furnitureCore.functionality.*;
import cn.lunadeer.furnitureCore.utils.ImageUtils;
import cn.lunadeer.furnitureCore.utils.JsonUtils;
import cn.lunadeer.furnitureCore.utils.XLogger;
import cn.lunadeer.furnitureCore.utils.ZipUtils;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationPart;
import cn.lunadeer.furnitureCoreApi.functionality.Functionality;
import cn.lunadeer.furnitureCoreApi.items.FurnitureItemStack;
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

    public static class  FurnitureModelText extends ConfigurationPart {
        // loadModel()
        public String modelNameInvalid = "Model name contains invalid characters, must be [a-z0-9._-]: %s";
        public String prefixPathInvalid = "Prefix path contains invalid characters, must be [a-z0-9._-]: %s";
        public String modelJsonNotFound = "Model file model.json not found.";
        public String elementNotFound = "Elements not found in json model file.";
        public String textureNotFound = "Texture file not found: %s";
        public String failToLoadTexture = "Failed to load texture file: %s";

        // save()
        public String failToCreateTexturePath = "Failed to create texture save path: %s";
        public String failToCreateModelPath = "Failed to create model save path: %s";
        public String failToCreateItemModelPath = "Failed to create item model save path: %s";
        public String unknownRecipeType = "Unknown recipe type: %s";
        public String failToParseRecipe = "Model %s recipe %d failed to parse: %s";

        public String modelNotEffect = "Model not effective yet.";
    }

    public static FurnitureModelImpl loadModel(File modelFile) throws Exception {
        return loadModel(modelFile, null);
    }

    public static FurnitureModelImpl loadModel(File modelFile, String prefixPath) throws Exception {
        // 0. validate name & path
        String modelName = modelFile.getName().replace(".zip", "");
        if (!isValidName(modelName)) {
            throw new Exception(Language.furnitureModelText.modelNameInvalid.formatted(modelName));
        }
        String prefix = prefixPath == null ? "furniture" : prefixPath;
        if (!isValidName(prefix.replace("/", ""))) {
            throw new Exception(Language.furnitureModelText.prefixPathInvalid.formatted(prefix));
        }
        File unzipCache = new File(FurnitureCore.getCacheDir(), "model_" + modelName);
        try {
            FurnitureModelImpl furnitureModel = new FurnitureModelImpl();
            furnitureModel.modelName = modelName;
            furnitureModel.setPrefixPath(prefix);

            // 1. unzip the file to cache directory
            ZipUtils.decompressFromZip(modelFile, unzipCache);

            // 2. valid the json file exists then parse it to modelJson
            File jsonFile = new File(unzipCache, "model.json");
            if (!jsonFile.exists()) {
                throw new Exception(Language.furnitureModelText.modelJsonNotFound);
            }
            JSONObject json = JsonUtils.loadFromFile(jsonFile);
            furnitureModel.ambientocclusion = json.containsKey("ambientocclusion") ? json.getBoolean("ambientocclusion") : true;
            furnitureModel.display = json.containsKey("display") ? json.getJSONObject("display") : null;
            furnitureModel.gui_light = json.containsKey("gui_light") ? json.getString("gui_light") : "side";
            furnitureModel.groups = json.getJSONArray("groups");
            furnitureModel.elements = json.getJSONArray("elements");
            if (furnitureModel.elements == null) {
                throw new Exception(Language.furnitureModelText.elementNotFound);
            }

            // 3. load properties.json exists, then parse it to model properties
            File propertiesFile = new File(unzipCache, "properties.json");
            if (propertiesFile.exists()) {
                JSONObject propertiesJson = JsonUtils.loadFromFile(propertiesFile);
                furnitureModel.displayName = propertiesJson.containsKey("display_name") ? propertiesJson.getString("display_name") : furnitureModel.modelName;
                furnitureModel.canRotate = propertiesJson.containsKey("can_rotate") ? propertiesJson.getBoolean("can_rotate") : true;
                furnitureModel.canHanging = propertiesJson.containsKey("can_hanging") ? propertiesJson.getBoolean("can_hanging") : false;
                if (propertiesJson.containsKey("function")) {
                    JSONObject functionJson = propertiesJson.getJSONObject("function");
                    String type = functionJson.containsKey("type") ? functionJson.getString("type") : "none";
                    switch (type) {
                        case "chair":
                            furnitureModel.modelFunction = new ChairFunction(functionJson);
                            break;
                        case "storage":
                            furnitureModel.modelFunction = new StorageFunction(functionJson);
                            break;
                        case "illumination":
                            furnitureModel.modelFunction = new IlluminationFunction(functionJson);
                            break;
                        case "work_block":
                            furnitureModel.modelFunction = new WorkBlockFunction(functionJson);
                            break;
                        default:
                            furnitureModel.modelFunction = new NoneFunction();
                            break;
                    }
                }

                if (furnitureModel.displayName.isEmpty()) { // if display name is empty, set it to model name
                    furnitureModel.displayName = furnitureModel.modelName;
                }
            }

            // 4. valid the texture referenced in json file exists then load it to textures
            for (String key : json.getJSONObject("textures").keySet()) {
                String textureName = json.getJSONObject("textures").getString(key);
                if (textureName.contains("minecraft:")) {
                    // skip minecraft textures (e.g. minecraft:block/stone)
                    continue;
                }
                File textureFile = new File(unzipCache, textureName + ".png");
                if (!textureFile.exists()) {
                    throw new Exception(Language.furnitureModelText.textureNotFound.formatted(textureFile.toString()));
                }
                BufferedImage texture = ImageUtils.loadImage(textureFile);
                if (texture == null) {
                    throw new Exception(Language.furnitureModelText.failToLoadTexture.formatted(textureFile.toString()));
                }
                furnitureModel.textures.put(key, texture);
            }

            // 5. load recipes (cache this for save stage to parse)
            File recipeFile = new File(unzipCache, "recipes.json");
            if (recipeFile.exists()) {
                JSONObject recipeJson = JsonUtils.loadFromFile(recipeFile);
                furnitureModel.recipesJson = recipeJson.getJSONArray("recipes");
            }

            return furnitureModel;
        } finally {
            // 6. clean the cache directory in finally block
            if (unzipCache.exists()) {
                DeleteFolderRecursively(unzipCache);
            }
        }
    }

    private NamespacedKey itemModelKey;
    private String displayName = null;
    private String modelName;
    private boolean canRotate = true;
    private boolean canHanging = false;
    private boolean ambientocclusion = true;
    private JSONArray elements;
    private JSONArray groups;
    private JSONObject display;
    private String gui_light = "side";
    private final Map<String, BufferedImage> textures = new HashMap<>();
    private JSONArray recipesJson = new JSONArray();
    private final Map<NamespacedKey, CraftingRecipe> internalRecipes = new HashMap<>();
    private boolean savedAndEffective = false;
    private Functionality modelFunction;

    @Override
    public NamespacedKey getItemModelKey() {
        if (!savedAndEffective) {
            throw new IllegalStateException("Model not effective yet.");
        }
        return itemModelKey;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getDisplayName() {
        return displayName == null ? modelName : displayName;
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
    private void setPrefixPath(String path) {
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

    @Override
    public Boolean canRotate() {
        return this.canRotate;
    }

    @Override
    public Boolean canHanging() {
        return this.canHanging;
    }

    @Override
    public Functionality getFunction() {
        return modelFunction;
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
            throw new IllegalStateException(Language.furnitureModelText.modelNotEffect);
        }
        return prefixPath == null ? modelName : prefixPath + "/" + modelName;
    }

    @Override
    public String getCallableNameWithNamespace() {
        if (!savedAndEffective) {
            throw new IllegalStateException(Language.furnitureModelText.modelNotEffect);
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
            throw new Exception(Language.furnitureModelText.failToCreateTexturePath.formatted(textureSavePath.toString()));
        }
        if (!modelSavePath.exists() && !modelSavePath.mkdirs()) {
            throw new Exception(Language.furnitureModelText.failToCreateModelPath.formatted(modelSavePath.toString()));
        }
        if (!itemModelPath.exists() && !itemModelPath.mkdirs()) {
            throw new Exception(Language.furnitureModelText.failToCreateItemModelPath.formatted(itemModelPath.toString()));
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
        for (int i = 0; i < recipesJson.size(); i++) {
            try {
                NamespacedKey pdcKey = new NamespacedKey(FurnitureCore.getNamespace(), "furniture_" + this.getModelName() + "_recipe_" + i);
                JSONObject recipe = recipesJson.getJSONObject(i);
                String type = Objects.requireNonNullElse(recipe.getString("type"), "shapeless");
                if (type.equals("shapeless")) {
                    internalRecipes.put(pdcKey, getShapelessRecipe(i, recipe, pdcKey, this));
                } else if (type.equals("shaped")) {
                    internalRecipes.put(pdcKey, getShapedRecipe(i, recipe, pdcKey, this));
                } else {
                    throw new Exception(Language.furnitureModelText.unknownRecipeType.formatted(type));
                }
            } catch (Exception e) {
                XLogger.err(Language.furnitureModelText.failToParseRecipe.formatted(this.getCallableNameWithNamespace(), i, e.getMessage()));
            }
        }

    }

    @Override
    public void registerInternalRecipe() {
        if (!Configuration.modelInternalRecipes) return;
        for (CraftingRecipe recipe : internalRecipes.values()) {
            FurnitureCore.getInstance().getServer().addRecipe(recipe);
        }
    }

    @Override
    public void unregisterInternalRecipe() {
        if (!Configuration.modelInternalRecipes) return;
        for (CraftingRecipe recipe : internalRecipes.values()) {
            FurnitureCore.getInstance().getServer().removeRecipe(recipe.getKey());
        }
    }

    @Override
    public Map<NamespacedKey, CraftingRecipe> getInternalRecipes() {
        return internalRecipes;
    }

    @Override
    public Boolean isEffect() {
        return savedAndEffective;
    }

    public static class ParseRecipeText extends ConfigurationPart {
        // getShapedRecipe()
        public String shapeNotFound = "Shape not found in shaped recipe %d";
        public String shapeInvalid = "Shape should not have more than 3 rows in shaped recipe %d";
        public String rowNotAvailable = "Row %d not available in shaped recipe %d";
        public String rowInvalid = "Row %d should not have more than 3 characters in shaped recipe %d";
        public String ingredientsNotFound = "Ingredients not found in recipe %d";
        public String keyInvalid = "Key %s should have only 1 character in shaped recipe %d";
        public String keyNotFound = "Key %s not found in shape in shaped recipe %d";
        public String ingredientNotAvailable = "Ingredient %s not available in recipe %d";
    }

    private static ShapedRecipe getShapedRecipe(int i, JSONObject recipe, NamespacedKey pdcKey, FurnitureModel furnitureModel) throws Exception {
        ShapedRecipe shapedRecipe = new ShapedRecipe(pdcKey, new FurnitureItemStack(furnitureModel));
        JSONArray shape = recipe.getJSONArray("shape");
        if (shape == null) {
            throw new Exception(Language.parseRecipeText.shapeNotFound.formatted(i));
        }
        if (shape.size() > 3) {
            throw new Exception(Language.parseRecipeText.shapeInvalid.formatted(i));
        }
        String[] shapeList = new String[shape.size()];
        List<Character> keys = new ArrayList<>();
        for (int j = 0; j < shape.size(); j++) {
            String row = shape.getString(j);
            if (row == null) {
                throw new Exception(Language.parseRecipeText.rowNotAvailable.formatted(j, i));
            }
            if (row.length() > 3) {
                throw new Exception(Language.parseRecipeText.rowInvalid.formatted(j, i));
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
            throw new Exception(Language.parseRecipeText.ingredientsNotFound.formatted(i));
        }
        for (String key : ingredients.keySet()) {
            if (key.length() != 1) {
                throw new Exception(Language.parseRecipeText.keyInvalid.formatted(key, i));
            }
            if (!keys.contains(key.charAt(0))) {
                throw new Exception(Language.parseRecipeText.keyNotFound.formatted(key, i));
            }
            String ingredient = ingredients.getString(key);
            if (ingredient == null) {
                throw new Exception(Language.parseRecipeText.ingredientNotAvailable.formatted(key, i));
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
            throw new Exception(Language.parseRecipeText.ingredientsNotFound.formatted(i));
        }
        for (int j = 0; j < ingredients.size(); j++) {
            String ingredient = ingredients.getString(j);
            if (ingredient == null) {
                throw new Exception(Language.parseRecipeText.ingredientNotAvailable.formatted(j, i));
            }
            if (ingredient.startsWith("minecraft:")) {
                ingredient = ingredient.split(":")[1];
            }
            shapelessRecipe.addIngredient(Material.valueOf(ingredient.toUpperCase()));
        }
        return shapelessRecipe;
    }

    private static boolean isValidNameChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '.' || c == '_' || c == '-';
    }

    private static boolean isValidName(String namespace) {
        int len = namespace.length();
        if (len == 0) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (!isValidNameChar(namespace.charAt(i))) {
                return false;
            }
        }

        return true;
    }

}
