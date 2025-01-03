package cn.lunadeer.furnitureCore.managers;

import cn.lunadeer.furnitureCore.Configuration;
import cn.lunadeer.furnitureCore.FurnitureCore;
import cn.lunadeer.furnitureCore.Language;
import cn.lunadeer.furnitureCore.models.FurnitureModelImpl;
import cn.lunadeer.furnitureCore.utils.JsonUtils;
import cn.lunadeer.furnitureCore.utils.Notification;
import cn.lunadeer.furnitureCore.utils.XLogger;
import cn.lunadeer.furnitureCore.utils.ZipUtils;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationPart;
import cn.lunadeer.furnitureCoreApi.managers.ModelManager;
import cn.lunadeer.furnitureCoreApi.managers.ResourcePackManager;
import cn.lunadeer.furnitureCoreApi.models.FurnitureModel;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.net.httpserver.HttpServer;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static cn.lunadeer.furnitureCore.utils.Common.DeleteFolderRecursively;

/**
 * Load models, Generate resource pack, Serve resource pack, Apply resource pack to players
 */
public class ResourcePackManagerImpl extends ResourcePackManager {

    public static class ResourcePackManagerText extends ConfigurationPart {
        public String failToCreateModelsDir = "Failed to create models directory.";
        // loadModelsFromDisk()
        public String failToLoadModel = "Failed to load model: %s";
        public String reason = "Reason: %s";
        public String loadModelsCount = "Loaded %d models.";
        public String listFailedLoadModels = "Failed to load %d models: %s";
        // generateResourcePack()
        public String failToDeleteCacheDir = "Failed to delete cache directory: %s";
        public String failToMovePackDir = "Failed to move pack directory to cache directory.";
        public String failToRenamePackMcmeta = "Failed to rename pack.mcmeta.json to pack.mcmeta.";
        public String failToGenerateModelFile = "Failed to generate model file %s: %s";
        public String registeredRecipesCount = "Registered %d recipes.";
        public String resourcePackModelsCount = "Resource pack will generate with %s models.";
        public String failToDeleteExistingPack = "Failed to delete existing resource pack zip file.";
        public String failToGeneratePackZip = "Failed to generate resource pack zip file.";
        public String packGenerateSuccess = "Resource pack generated successfully.";
        public String resourcePackSize = "Resource pack size: %s";
        public String resourcePackHash = "Resource pack hash: %s";
        public String failToDeletePackCache = "Failed to delete cache/resource_pack directory.";
        // startServer()
        public String packServerStartSuccess = "Resource pack is hosted at %s";
        public String packServerStartFail = "Failed to start http server: %s";
        // applyToPlayer()
        public String packNotReady = "Resource pack is not ready.";
        public String messageSentToClient = "This is a resource pack update from Server FurnitureCore.";
        // GetFileHash()
        public String fileNotFound = "File not found: %s";
        public String notFile = "Not a file: %s";
    }

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
                XLogger.err(Language.resourcePackManagerText.failToCreateModelsDir);
            }
        }
    }

    // pre-defined resource pack files in jar resources, these files will be copied to pack directory
    private final List<String> preDefinedResourcePackFiles = List.of(
            "pack/pack.mcmeta.json",
            "pack/pack.png",

            "pack/assets/furniture_core/items/tools/screwdriver.json",
            "pack/assets/furniture_core/models/tools/screwdriver.json",
            "pack/assets/furniture_core/textures/tools/screwdriver_handle.png",
            "pack/assets/furniture_core/textures/tools/screwdriver_head.png"

            // add more files if  needed ...
    );

    @Override
    public void loadModelsFromDisk(CommandSender sender) {
        modelLoad.clear();
        // 1. list all zip files under models directory
        Map<File, String> modelDirZipFiles = listModelFileAndPrefix(modelDir, modelDir);

        // 2. for each zip file
        List<String> failed = new ArrayList<>();
        for (File zipFile : modelDirZipFiles.keySet()) {
            // - try load model then assign & set index
            try {
                FurnitureModelImpl furnitureModel = FurnitureModelImpl.loadModel(zipFile, modelDirZipFiles.get(zipFile));
                // - add the model to a list for later use
                modelLoad.add(furnitureModel);
            } catch (Exception e) {
                Notification.error(sender, Language.resourcePackManagerText.failToLoadModel, zipFile.getAbsoluteFile().toString());
                Notification.error(sender, Language.resourcePackManagerText.reason, e.getMessage());
                failed.add(zipFile.getName());
            }
        }
        Notification.info(sender, Language.resourcePackManagerText.loadModelsCount, modelLoad.size());
        if (!failed.isEmpty()) {
            Notification.error(sender, Language.resourcePackManagerText.listFailedLoadModels, failed.size(), String.join(", ", failed));
        }
    }

    @Override
    public void generateResourcePack(CommandSender sender) throws Exception {
        resourcePackStatus = ResourcePackStatus.GENERATING;
        // 0. clear all models
        ModelManager.getInstance().unregisterAllModels();
        // 1. copy pre-defined files to pack directory
        for (String filename : preDefinedResourcePackFiles) {
            plugin.saveResource(filename, true);
        }

        // 2. move pack directory to cache/resource_pack
        File resourcePackDir = new File(plugin.getDataFolder(), "pack");
        if (getResourcePackCacheDir().exists()) {
            if (!DeleteFolderRecursively(getResourcePackCacheDir())) {
                resourcePackStatus = ResourcePackStatus.ERROR;
                throw new Exception(Language.resourcePackManagerText.failToDeleteCacheDir.formatted(getResourcePackCacheDir().getAbsolutePath()));
            }
        }
        if (!resourcePackDir.renameTo(getResourcePackCacheDir())) {
            resourcePackStatus = ResourcePackStatus.ERROR;
            throw new Exception(Language.resourcePackManagerText.failToMovePackDir);
        }

        // 3. rename cache/resource_pack/pack.mcmeta.json to cache/resource_pack/pack.mcmeta
        File packMcmeta = new File(getResourcePackCacheDir(), "pack.mcmeta.json");
        if (!packMcmeta.renameTo(new File(getResourcePackCacheDir(), "pack.mcmeta"))) {
            resourcePackStatus = ResourcePackStatus.ERROR;
            throw new Exception(Language.resourcePackManagerText.failToRenamePackMcmeta);
        }

        // 4. save all models (get from ModelManager)
        int recipeCount = 0;
        for (FurnitureModelImpl furnitureModel : modelLoad) {
            try {
                furnitureModel.setNamespace(FurnitureCore.getNamespace());
                furnitureModel.save(getAssetDir());     // <<<<<<<<<<<<<<   from now the model is effectively
                ModelManager.getInstance().registerModel(furnitureModel);
                recipeCount += furnitureModel.getInternalRecipes().size();
            } catch (Exception e) {
                Notification.error(sender, Language.resourcePackManagerText.failToGenerateModelFile, furnitureModel.getModelName(), e.getMessage());
            }
        }
        Notification.info(sender, Language.resourcePackManagerText.registeredRecipesCount, recipeCount);
        Notification.info(sender, Language.resourcePackManagerText.resourcePackModelsCount, ModelManager.getInstance().getModels().size());

        // 5. generate atlas files
        File atlasDir = new File(getAssetDir(), "minecraft/atlases");
        JSONObject blocksAtlas = new JSONObject(); // blocks.json
        for (FurnitureModel model : ModelManager.getInstance().getModels()) {
            if (atlasSources.containsKey(model.getPrefixPath())) {
                continue;
            }
            atlasSources.put(model.getPrefixPath(), new AtlasSource(model.getPrefixPath()));
        }
        JSONArray sources = new JSONArray();
        for (AtlasSource source : atlasSources.values()) {
            JSONObject sourceJson = new JSONObject();
            sourceJson.put("type", source.type);
            sourceJson.put("source", source.source);
            sourceJson.put("prefix", source.prefix);
            sources.add(sourceJson);
        }
        blocksAtlas.put("sources", sources);
        JsonUtils.saveToFile(blocksAtlas, new File(atlasDir, "blocks.json"));

        // 6. zip the cache/resource_pack directory to cache/furniture-core-resource-pack.zip
        if (getResourcePackZip().exists()) {
            if (!getResourcePackZip().delete()) {
                resourcePackStatus = ResourcePackStatus.ERROR;
                throw new Exception(Language.resourcePackManagerText.failToDeleteExistingPack);
            }
        }
        ZipUtils.compressFolderContentToZip(getResourcePackCacheDir(), getResourcePackZip());
        if (!getResourcePackZip().exists()) {
            resourcePackStatus = ResourcePackStatus.ERROR;
            throw new Exception(Language.resourcePackManagerText.failToGeneratePackZip);
        }
        resourcePackHash = GetFileHash(getResourcePackZip());

        // Done output resource pack info
        Notification.info(sender, Language.resourcePackManagerText.packGenerateSuccess);
        Notification.info(sender, Language.resourcePackManagerText.resourcePackSize, GetResourcePackZipSize());
        Notification.info(sender, Language.resourcePackManagerText.resourcePackHash, BytesToHex(resourcePackHash));
        resourcePackStatus = ResourcePackStatus.GENERATED;

        if (!DeleteFolderRecursively(getResourcePackCacheDir())) {
            resourcePackStatus = ResourcePackStatus.ERROR;
            throw new Exception(Language.resourcePackManagerText.failToDeletePackCache);
        }
        applyToAllPlayers(Configuration.resourcePackSettings.required);
    }

    @Override
    public void startServer() throws Exception {
        InetSocketAddress address = new InetSocketAddress("0.0.0.0", Configuration.resourcePackServer.port);
        // run a small http server to serve the resource pack
        if (server != null) {
            server.stop(0);
        }
        try {
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
            resourcePackStatus = ResourcePackStatus.READY;
            XLogger.info(Language.resourcePackManagerText.packServerStartSuccess, getResourcePackUrl());
        } catch (IOException e) {
            resourcePackStatus = ResourcePackStatus.ERROR;
            throw new Exception(Language.resourcePackManagerText.packServerStartFail.formatted(e.getMessage()));
        }
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
        applyToAllPlayers(false);
    }

    @Override
    public void applyToAllPlayers(boolean force) throws IllegalStateException {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            applyToPlayer(player, force);
        }
    }

    @Override
    public void applyToPlayer(Player player) throws IllegalStateException {
        applyToPlayer(player, false);
    }

    @Override
    public void applyToPlayer(Player player, boolean force) throws IllegalStateException {
        if (resourcePackStatus != ResourcePackStatus.READY) {
            throw new IllegalStateException(Language.resourcePackManagerText.packNotReady);
        }
        XLogger.debug("Applying resource pack to %s", player.getName());
        player.setResourcePack(getResourcePackUrl(),
                resourcePackHash,
                Component.text(Language.resourcePackManagerText.messageSentToClient),
                force);
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
            throw new IOException(Language.resourcePackManagerText.fileNotFound.formatted(file.getAbsolutePath()));
        }
        if (!file.isFile()) {
            throw new IOException(Language.resourcePackManagerText.notFile.formatted(file.getAbsolutePath()));
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

    private final Map<String, AtlasSource> atlasSources = new HashMap<>(Map.of(
            "tools", new AtlasSource("tools"),
            "furniture", new AtlasSource("furniture")
    ));

    private static class AtlasSource {
        public AtlasSource(String name) {
            this.source = name;
            this.prefix = name + "/";
        }

        public String type = "directory";
        public String source;
        public String prefix;
    }


    private static Map<File, String> listModelFileAndPrefix(File modelDir, File dir) {
        Map<File, String> result = new HashMap<>();
        File[] files = dir.listFiles();
        XLogger.debug("listModelFileAndPrefix: Path %s with files: %s", dir.getAbsolutePath(), Arrays.toString(files));
        if (files == null) {
            return new HashMap<>();
        }
        String prefix;
        if (modelDir.getAbsolutePath().equals(dir.getAbsolutePath())) {
            prefix = null;
        } else {
            prefix = dir.getAbsolutePath().substring(modelDir.getAbsolutePath().length() + 1);
        }
        for (File file : files) {
            if (file.isDirectory()) {
                result.putAll(listModelFileAndPrefix(modelDir, file));
            } else if (file.getName().endsWith(".zip")) {
                result.put(file, prefix);
            }
        }
        return result;
    }

}
