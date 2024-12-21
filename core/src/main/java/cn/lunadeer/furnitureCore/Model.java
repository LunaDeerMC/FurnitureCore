package cn.lunadeer.furnitureCore;

import cn.lunadeer.furnitureCore.utils.ImageUtils;
import cn.lunadeer.furnitureCore.utils.JsonUtils;
import cn.lunadeer.furnitureCore.utils.ZipUtils;
import com.alibaba.fastjson.JSONObject;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Model {

    public static Model loadModel(File modelFile) throws Exception {
        File unzipCache = new File(FurnitureCore.getCacheDir(), "model_" + modelFile.getName().substring(0, modelFile.getName().lastIndexOf(".")));
        try {
            Model model = new Model();

            // 1. unzip the file to cache directory
            ZipUtils.decompressFromZip(modelFile, unzipCache);
            File[] jsonFiles = unzipCache.listFiles((dir, name) -> name.endsWith(".json"));

            // 2. valid the json file exists then parse it to modelJson
            if (jsonFiles == null || jsonFiles.length == 0) {
                throw new Exception("Model json file not found.");
            }
            model.modelName = jsonFiles[0].getName().replace(".json", "");
            JSONObject json = JsonUtils.loadFromFile(jsonFiles[0]);
            model.elements = json.getJSONObject("elements");

            // 3. check if custom_name exists in json file then set it to model
            if (json.containsKey("custom_name")) {
                model.customName = json.getString("custom_name");
            } else {
                model.customName = modelFile.getName().substring(0, modelFile.getName().lastIndexOf("."));
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
                model.textures.put(key, texture);
            }
            return model;
        } finally {
            // 5. clean the cache directory in finally block
            if (unzipCache.exists()) {
                boolean re = unzipCache.delete();
            }
        }
    }

    private Integer index;
    private String customName;
    private String modelName;
    private JSONObject elements;
    private final Map<String, BufferedImage> textures = new HashMap<>();
    private boolean savedAndEffective = false;

    public void setIndex(Integer index) {
        if (savedAndEffective) {
            throw new IllegalStateException("Model already effective, cannot change index.");
        }
        this.index = index;
    }

    public Integer getIndex() {
        if (index == null) {
            throw new IllegalStateException("Index not set.");
        }
        return index;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getCustomName() {
        return customName;
    }

    public String getModelName() {
        return modelName;
    }

    private String texturePath = null;
    private String modelPath = null;
    private String namespace = "minecraft";

    public void setTexturePath(String texturePath) {
        if (savedAndEffective) {
            throw new IllegalStateException("Model already effective, cannot change texture path.");
        }
        if (texturePath.startsWith("/")) {
            texturePath = texturePath.substring(1);
        }
        if (texturePath.endsWith("/")) {
            texturePath = texturePath.substring(0, texturePath.length() - 1);
        }
        this.texturePath = texturePath;
    }

    public void setModelPath(String modelPath) {
        if (savedAndEffective) {
            throw new IllegalStateException("Model already effective, cannot change model path.");
        }
        if (modelPath.startsWith("/")) {
            modelPath = modelPath.substring(1);
        }
        if (modelPath.endsWith("/")) {
            modelPath = modelPath.substring(0, modelPath.length() - 1);
        }
        this.modelPath = modelPath;
    }

    public void setNamespace(String namespace) {
        if (savedAndEffective) {
            throw new IllegalStateException("Model already effective, cannot change namespace.");
        }
        if (namespace.endsWith(":")) {
            namespace = namespace.substring(0, namespace.length() - 1);
        }
        this.namespace = namespace;
    }

    public String getModelCallableName() {
        if (!savedAndEffective) {
            throw new IllegalStateException("Model not effective yet.");
        }
        return namespace + ":" + (modelPath == null ? modelName : modelPath + "/" + modelName);
    }

    public void save(File assetPath) throws Exception {
        File textureSavePath = new File(assetPath, namespace + "/textures");
        if (texturePath != null) {
            textureSavePath = new File(textureSavePath, texturePath);
        }
        if (!textureSavePath.exists()) {
            boolean re = textureSavePath.mkdirs();
        }
        File modelSavePath = new File(assetPath, namespace + "/models");
        if (modelPath != null) {
            modelSavePath = new File(modelSavePath, modelPath);
        }
        if (!modelSavePath.exists()) {
            boolean re = modelSavePath.mkdirs();
        }
        JSONObject json = new JSONObject();
        JSONObject textures = new JSONObject();
        for (String key : this.textures.keySet()) {
            String name = this.modelName + "_" + key;
            if (texturePath != null) {
                name = texturePath + "/" + name;
            }
            File textureFile = new File(textureSavePath, name + ".png");
            ImageUtils.saveImage(this.textures.get(key), textureFile, "png");
            if (namespace != null) {
                name = namespace + ":" + name;
            }
            textures.put(key, name);
        }
        json.put("textures", textures);
        json.put("elements", elements);
        JsonUtils.saveToFile(json, new File(modelSavePath, modelName + ".json"));
        savedAndEffective = true;
    }


}
