package cn.lunadeer.furnitureCore;

import cn.lunadeer.furnitureCore.utils.JsonUtils;
import cn.lunadeer.furnitureCore.utils.XLogger;
import cn.lunadeer.furnitureCore.utils.ZipUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ResourcePackManager {

    private static ResourcePackManager instance;
    private final FurnitureCore plugin;

    public ResourcePackManager(FurnitureCore plugin) {
        instance = this;
        this.plugin = plugin;
    }

    // add more files if  needed
    private final List<String> preDefinedResourcePackFiles = List.of(
            "pack.mcmeta.json",
            "pack.png",
            "assets/minecraft/models/item/item_frame.json"
    );

    public void generate() throws Exception{
        // 1. copy pre-defined files to pack directory
        for (String filename : preDefinedResourcePackFiles) {
            plugin.saveResource("pack/" + filename, true);
        }

        // 2. move pack directory to cache/resource_pack
        File resourcePackDir = new File(plugin.getDataFolder(), "pack");
        if (getResourcePackCacheDir().exists()) {
            if (!getResourcePackCacheDir().delete()) {
                throw new Exception("Failed to delete cache directory: %s".formatted(getResourcePackCacheDir().getAbsolutePath()));
            }
        }
        if (!resourcePackDir.renameTo(getResourcePackCacheDir())) {
            throw new Exception("Failed to move pack directory to cache directory.");
        }

        // 3. rename cache/resource_pack/pack.mcmeta.json to cache/resource_pack/pack.mcmeta
        File packMcmeta = new File(getResourcePackCacheDir(), "pack.mcmeta.json");
        if (!packMcmeta.renameTo(new File(getResourcePackCacheDir(), "pack.mcmeta"))) {
            throw new Exception("Failed to rename pack.mcmeta.json to pack.mcmeta");
        }

        // 4. save all models (get from ModelManager)
        List<Integer> failedModels = new ArrayList<>();
        File itemFrameJsonFile = new File(getAssetDir(), "minecraft/models/item/item_frame.json");
        if (!itemFrameJsonFile.exists()) {
            throw new Exception("item_frame.json not found.");
        }
        JSONObject itemFrameJsonObj = JsonUtils.loadFromFile(itemFrameJsonFile);
        JSONArray overrides = new JSONArray();
        for (Model model : ModelManager.getInstance().getModels()) {
            try {
                model.setNamespace(FurnitureCore.getNamespace());
                model.save(getAssetDir());
            } catch (Exception e) {
                XLogger.err("Failed to generate model file %s: %s", model.getModelName(), e.getMessage());
                failedModels.add(model.getIndex());
                continue;
            }
            // 5. modify item_frame.json
            //  "overrides": [
            //    {
            //      "predicate": {
            //        "custom_model_data": 1
            //      },
            //      "model": "tutorial/red_emerald_block"
            //    }
            //  ]
            JSONObject override = new JSONObject();
            JSONObject predicate = new JSONObject();
            predicate.put("custom_model_data", model.getIndex());
            override.put("predicate", predicate);
            override.put("model", model.getModelCallableName());
            overrides.add(override);
        }
        for (Integer index : failedModels) {
            ModelManager.getInstance().removeIndexedModel(index);
        }
        itemFrameJsonObj.put("overrides", overrides);
        JsonUtils.saveToFile(itemFrameJsonObj, itemFrameJsonFile);

        // 6. zip the cache/resource_pack directory to cache/furniture-core-resource-pack.zip
        if (getResourcePackZip().exists()) {
            if (!getResourcePackZip().delete()) {
                throw new Exception("Failed to delete existing resource pack zip file.");
            }
        }
        ZipUtils.compressToZip(getResourcePackCacheDir(), getResourcePackZip());
        if (!getResourcePackZip().exists()) {
            throw new Exception("Failed to generate resource pack zip file.");
        }
        if(!getResourcePackCacheDir().delete()) {
            throw new Exception("Failed to delete cache/resource_pack directory.");
        }

        XLogger.info("Resource pack generated successfully. Size: %d MB", getResourcePackZip().length() / 1024 / 1024);
    }

    public void startServer(String host, int port) {
        // todo: run a small http server to serve the resource pack
        XLogger.info("Starting resource pack server at %s:%d", host, port);
    }

    private static File getResourcePackCacheDir() {
        return new File(FurnitureCore.getCacheDir(), "resource_pack");
    }

    private static File getAssetDir() {
        return new File(getResourcePackCacheDir(), "assets");
    }

    private static File getResourcePackZip() {
        return new File(FurnitureCore.getCacheDir(), "furniture-core-resource-pack.zip");
    }

    public static ResourcePackManager getInstance() {
        return instance;
    }


}
