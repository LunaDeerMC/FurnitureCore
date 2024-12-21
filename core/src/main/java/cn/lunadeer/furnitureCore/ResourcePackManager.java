package cn.lunadeer.furnitureCore;

import java.io.File;

public class ResourcePackManager {



    public ResourcePackManager() {

    }

    public void generate() {
        // 1. prepare resource_pack directory in cache directory (cache/resource_pack)

        // 2. copy pre-defined files to the resource_pack directory
        //      - pack.mcmeta (need to rename from pack.mcmeta.json)
        //      - pack.png
        //      - assets (dir)

        // 3. save all models (get from ModelManager)
    }


    private static void saveModel(Model model) throws Exception {
        model.setNamespace(FurnitureCore.getNamespace());
        model.save(getAssetDir());
    }

    private static File getResourcePackCacheDir() {
        return new File(FurnitureCore.getCacheDir(), "resource_pack");
    }

    private static File getAssetDir() {
        return new File(getResourcePackCacheDir(), "assets");
    }


}
