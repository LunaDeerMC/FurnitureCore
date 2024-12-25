package cn.lunadeer.furnitureCore.managers;

import cn.lunadeer.furnitureCore.Configuration;
import cn.lunadeer.furnitureCore.FurnitureCore;
import cn.lunadeer.furnitureCore.models.FurnitureModelImpl;
import cn.lunadeer.furnitureCore.utils.XLogger;
import cn.lunadeer.furnitureCore.utils.ZipUtils;
import cn.lunadeer.furnitureCoreApi.managers.ModelManager;
import cn.lunadeer.furnitureCoreApi.managers.ResourcePackManager;
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
import java.util.Arrays;
import java.util.List;

/**
 * Load models, Generate resource pack, Serve resource pack, Apply resource pack to players
 */
public class ResourcePackManagerImpl extends ResourcePackManager {

    private final FurnitureCore plugin;
    private final File modelDir;

    private final List<FurnitureModelImpl> modelLoad = new ArrayList<>();

    private byte[] resourcePackHash;
    private ResourcePackStatus resourcePackStatus = ResourcePackStatus.GENERATING;
    private HttpServer server = null;

    public ResourcePackManagerImpl(FurnitureCore plugin) {
        instance = this;
        this.plugin = plugin;
        this.modelDir = new File(plugin.getDataFolder(), "models");
        if (!this.modelDir.exists()) {
            if (!this.modelDir.mkdirs()) {
                XLogger.err("Failed to create models directory.");
            }
        }
    }

    // add more files if  needed
    private final List<String> preDefinedResourcePackFiles = List.of(
            "pack.mcmeta.json",
            "pack.png",

            "assets/minecraft/atlases/blocks.json",

            "assets/furniture_core/items/tools/screwdriver.json",
            "assets/furniture_core/models/tools/screwdriver.json",
            "assets/furniture_core/textures/tools/screwdriver_handle.png",
            "assets/furniture_core/textures/tools/screwdriver_head.png"
    );

    @Override
    public void loadModelsFromDisk() throws Exception {
        // 1. list all zip files under models directory
        List<String> modelDirZipFilenames = new ArrayList<>();
        File[] zipFiles = modelDir.listFiles((dir, name) -> name.endsWith(".zip"));
        if (zipFiles == null) {
            throw new Exception("Failed to list files under dir %s".formatted(modelDir.getAbsolutePath()));
        }
        Arrays.stream(zipFiles).forEach(file -> modelDirZipFilenames.add(file.getName()));

        List<String> failed = new ArrayList<>();
        // 3. for each zip file
        for (String zipFilename : modelDirZipFilenames) {
            File zipFile = new File(modelDir, zipFilename);

            // - 2. try load model then assign & set index
            try {
                FurnitureModelImpl furnitureModel = FurnitureModelImpl.loadModel(zipFile);
                // - 3. add the model to a list for later use
                modelLoad.add(furnitureModel);
            } catch (Exception e) {
                XLogger.err("Failed to load model: %s", zipFile.getAbsoluteFile().toString());
                XLogger.err("Reason: %s", e.getMessage());
                failed.add(zipFilename);
            }
        }
        XLogger.info("Loaded %d models.", modelLoad.size());
        if (!failed.isEmpty()) {
            XLogger.err("Failed to load %d models: %s", failed.size(), String.join(", ", failed));
        }
    }

    @Override
    public void generateResourcePack() throws Exception {
        resourcePackStatus = ResourcePackStatus.GENERATING;
        // 0. clear all models
        ModelManager.getInstance().unregisterAllModels();
        // 1. copy pre-defined files to pack directory
        for (String filename : preDefinedResourcePackFiles) {
            plugin.saveResource("pack/" + filename, true);
        }

        // 2. move pack directory to cache/resource_pack
        File resourcePackDir = new File(plugin.getDataFolder(), "pack");
        if (getResourcePackCacheDir().exists()) {
            if (!DeleteFolderRecursively(getResourcePackCacheDir())) {
                resourcePackStatus = ResourcePackStatus.ERROR;
                throw new Exception("Failed to delete cache directory: %s".formatted(getResourcePackCacheDir().getAbsolutePath()));
            }
        }
        if (!resourcePackDir.renameTo(getResourcePackCacheDir())) {
            resourcePackStatus = ResourcePackStatus.ERROR;
            throw new Exception("Failed to move pack directory to cache directory.");
        }

        // 3. rename cache/resource_pack/pack.mcmeta.json to cache/resource_pack/pack.mcmeta
        File packMcmeta = new File(getResourcePackCacheDir(), "pack.mcmeta.json");
        if (!packMcmeta.renameTo(new File(getResourcePackCacheDir(), "pack.mcmeta"))) {
            resourcePackStatus = ResourcePackStatus.ERROR;
            throw new Exception("Failed to rename pack.mcmeta.json to pack.mcmeta");
        }

        // 4. save all models (get from ModelManager)
        for (FurnitureModelImpl furnitureModel : modelLoad) {
            try {
                furnitureModel.setNamespace(FurnitureCore.getNamespace());
                furnitureModel.save(getAssetDir());
                ModelManager.getInstance().registerModel(furnitureModel);
            } catch (Exception e) {
                XLogger.err("Failed to generate model file %s: %s", furnitureModel.getModelName(), e.getMessage());
            }
        }

        // 6. zip the cache/resource_pack directory to cache/furniture-core-resource-pack.zip
        if (getResourcePackZip().exists()) {
            if (!getResourcePackZip().delete()) {
                resourcePackStatus = ResourcePackStatus.ERROR;
                throw new Exception("Failed to delete existing resource pack zip file.");
            }
        }
        ZipUtils.compressFolderContentToZip(getResourcePackCacheDir(), getResourcePackZip());
        if (!getResourcePackZip().exists()) {
            resourcePackStatus = ResourcePackStatus.ERROR;
            throw new Exception("Failed to generate resource pack zip file.");
        }
        resourcePackHash = GetFileHash(getResourcePackZip());

        // Done
        XLogger.info("Resource pack generated successfully.");
        XLogger.info("Resource pack size: %s", GetResourcePackZipSize());
        XLogger.info("Resource pack hash: %s", BytesToHex(resourcePackHash));
        resourcePackStatus = ResourcePackStatus.GENERATED;

        if (!DeleteFolderRecursively(getResourcePackCacheDir())) {
            resourcePackStatus = ResourcePackStatus.ERROR;
            throw new Exception("Failed to delete cache/resource_pack directory.");
        }
    }

    @Override
    public void startServer() throws Exception {
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
        resourcePackStatus = ResourcePackStatus.READY;
    }

    @Override
    public void stopServer() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    @Override
    public void applyToAllPlayers() throws IllegalStateException {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            applyToPlayer(player);
        }
    }

    @Override
    public void applyToPlayer(Player player) throws IllegalStateException {
        if (resourcePackStatus != ResourcePackStatus.READY) {
            throw new IllegalStateException("Resource pack is not ready.");
        }
        XLogger.debug("Applying resource pack to %s", player.getName());
        player.setResourcePack(getResourcePackUrl(),
                resourcePackHash,
                Component.text("This is a resource pack update from Server FurnitureCore."),
                Configuration.resourcePackSettings.required);
    }

    @Override
    public ResourcePackStatus getStatus() {
        return resourcePackStatus;
    }

    @Override
    public boolean isReady() {
        return resourcePackStatus == ResourcePackStatus.READY;
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

    /**
     * Delete a folder recursively.
     *
     * @param folder the folder to delete
     * @return true if success, false if failed
     */
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

    /**
     * Get the size of the resource pack zip file.
     *
     * @return the size of the resource pack zip file with unit
     */
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

    /**
     * Convert byte array to hex string.
     *
     * @param bytes the byte array
     * @return the hex string
     */
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
