package cn.lunadeer.furnitureCore;

import cn.lunadeer.furnitureCore.utils.JsonUtils;
import cn.lunadeer.furnitureCore.utils.XLogger;
import cn.lunadeer.furnitureCore.utils.ZipUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.net.httpserver.HttpServer;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class ResourcePackManager {
    public enum Status {
        GENERATING,
        GENERATED,
        READY,
        ERROR
    }

    private static ResourcePackManager instance;
    private final FurnitureCore plugin;
    private byte[] resourcePackHash;
    private Status status = Status.GENERATING;
    private HttpServer server = null;

    public ResourcePackManager(FurnitureCore plugin) {
        instance = this;
        this.plugin = plugin;
    }

    // add more files if  needed
    private final List<String> preDefinedResourcePackFiles = List.of(
            "pack.mcmeta.json",
            "pack.png",
            "assets/minecraft/models/item/item_frame.json",
            "assets/minecraft/models/item/stick.json",

            "assets/minecraft/atlases/blocks.json",

            "assets/furniture_core/models/item/screwdriver.json",
            "assets/furniture_core/textures/item/screwdriver_handle.png",
            "assets/furniture_core/textures/item/screwdriver_head.png"
    );

    /**
     * Generate the resource pack.
     *
     * @throws Exception if failed to generate the resource pack
     */
    public void generate() throws Exception {
        status = Status.GENERATING;
        // 1. copy pre-defined files to pack directory
        for (String filename : preDefinedResourcePackFiles) {
            plugin.saveResource("pack/" + filename, true);
        }

        // 2. move pack directory to cache/resource_pack
        File resourcePackDir = new File(plugin.getDataFolder(), "pack");
        if (getResourcePackCacheDir().exists()) {
            if (!DeleteFolderRecursively(getResourcePackCacheDir())) {
                status = Status.ERROR;
                throw new Exception("Failed to delete cache directory: %s".formatted(getResourcePackCacheDir().getAbsolutePath()));
            }
        }
        if (!resourcePackDir.renameTo(getResourcePackCacheDir())) {
            status = Status.ERROR;
            throw new Exception("Failed to move pack directory to cache directory.");
        }

        // 3. rename cache/resource_pack/pack.mcmeta.json to cache/resource_pack/pack.mcmeta
        File packMcmeta = new File(getResourcePackCacheDir(), "pack.mcmeta.json");
        if (!packMcmeta.renameTo(new File(getResourcePackCacheDir(), "pack.mcmeta"))) {
            status = Status.ERROR;
            throw new Exception("Failed to rename pack.mcmeta.json to pack.mcmeta");
        }

        // 4. save all models (get from ModelManager)
        List<Integer> failedModels = new ArrayList<>();
        File itemFrameJsonFile = new File(getAssetDir(), "minecraft/models/item/item_frame.json");
        if (!itemFrameJsonFile.exists()) {
            status = Status.ERROR;
            throw new Exception("item_frame.json not found.");
        }
        JSONObject itemFrameJsonObj = JsonUtils.loadFromFile(itemFrameJsonFile);
        JSONArray overrides = new JSONArray();
        for (FurnitureModel furnitureModel : ModelManager.getInstance().getModels()) {
            try {
                furnitureModel.setNamespace(FurnitureCore.getNamespace());
                furnitureModel.save(getAssetDir());
            } catch (Exception e) {
                XLogger.err("Failed to generate model file %s: %s", furnitureModel.getModelName(), e.getMessage());
                failedModels.add(furnitureModel.getIndex());
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
            predicate.put("custom_model_data", furnitureModel.getIndex());
            override.put("predicate", predicate);
            override.put("model", furnitureModel.getCallableName());
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
                status = Status.ERROR;
                throw new Exception("Failed to delete existing resource pack zip file.");
            }
        }
        ZipUtils.compressFolderContentToZip(getResourcePackCacheDir(), getResourcePackZip());
        if (!getResourcePackZip().exists()) {
            status = Status.ERROR;
            throw new Exception("Failed to generate resource pack zip file.");
        }
        resourcePackHash = GetFileHash(getResourcePackZip());

        // Done
        XLogger.info("Resource pack generated successfully.");
        XLogger.info("Resource pack size: %s", GetResourcePackZipSize());
        XLogger.info("Resource pack hash: %s", BytesToHex(resourcePackHash));
        status = Status.GENERATED;

        if (!DeleteFolderRecursively(getResourcePackCacheDir())) {
            status = Status.ERROR;
            throw new Exception("Failed to delete cache/resource_pack directory.");
        }
    }

    /**
     * Start a small http server to serve the resource pack.
     */
    public void startServer() throws Exception{
        InetSocketAddress address = new InetSocketAddress("0.0.0.0", Configuration.resourcePackServer.port);
        // run a small http server to serve the resource pack
        if (server != null) {
            server.stop(0);
        }
        server = HttpServer.create(address, 0);
        server.createContext("/" + getResourcePackZip().getName(), exchange -> {
            exchange.sendResponseHeaders(200, getResourcePackZip().length());
            try (FileInputStream fis = new FileInputStream(getResourcePackZip())) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    exchange.getResponseBody().write(buffer, 0, bytesRead);
                }
            }
            exchange.close();
        });
        server.start();
        XLogger.info("Resource pack is hosted at %s", getResourcePackUrl());
        status = Status.READY;
    }

    public void stopServer(){
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    /**
     * Apply the resource pack to all players.
     *
     * @throws IllegalStateException if the resource pack is not ready
     */
    public void applyToAllPlayers() throws IllegalStateException {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            applyToPlayer(player);
        }
    }

    /**
     * Apply the resource pack to the player.
     *
     * @param player the player to apply the resource pack to
     * @throws IllegalStateException if the resource pack is not ready
     */
    public void applyToPlayer(Player player) throws IllegalStateException {
        if (status != Status.READY) {
            throw new IllegalStateException("Resource pack is not ready.");
        }
        XLogger.debug("Applying resource pack to %s", player.getName());
        player.setResourcePack(getResourcePackUrl(),
                resourcePackHash,
                Component.text("This is a resource pack update from Server FurnitureCore."),
                Configuration.resourcePackSettings.required);
    }

    public Status getStatus() {
        return status;
    }

    public boolean isReady() {
        return status == Status.READY;
    }

    private static File getResourcePackCacheDir() {
        return new File(FurnitureCore.getCacheDir(), "resource_pack");
    }

    private static File getAssetDir() {
        return new File(getResourcePackCacheDir(), "assets");
    }

    private static File getResourcePackZip() {
        return new File(FurnitureCore.getCacheDir(), Configuration.resourcePackSettings.packName + ".zip");
    }

    private static String getResourcePackUrl() {
        return "http://%s:%d/%s".formatted(Configuration.resourcePackServer.host, Configuration.resourcePackServer.port, getResourcePackZip().getName());
    }

    public static ResourcePackManager getInstance() {
        return instance;
    }

    private static boolean DeleteFolderRecursively(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    DeleteFolderRecursively(f);
                } else {
                    if (!f.delete()) {
                        return false;
                    }
                }
            }
        }
        return folder.delete();
    }

    private static String GetResourcePackZipSize() {
        float sizeByte = (float) getResourcePackZip().length();
        if (sizeByte < 1024) {
            return sizeByte + " B";
        } else if (sizeByte < 1024 * 1024) {
            return "%.2f KB".formatted(sizeByte / 1024);
        } else {
            return "%.2f MB".formatted(sizeByte / 1024 / 1024);
        }
    }

    /**
     * Get the sha1 hash of the file.
     *
     * @param file the file to get the hash
     * @return the hash of the file
     * @throws IOException              if failed to read the file
     * @throws NoSuchAlgorithmException if failed to get the hash algorithm
     */
    public static byte[] GetFileHash(File file) throws IOException, NoSuchAlgorithmException {
        if (!file.exists()) {
            throw new IOException("File not found: %s".formatted(file.getAbsolutePath()));
        }
        if (!file.isFile()) {
            throw new IOException("Not a file: %s".formatted(file.getAbsolutePath()));
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] byteArray = new byte[1024];
            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }
        return digest.digest();
    }

    private static String BytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

}
