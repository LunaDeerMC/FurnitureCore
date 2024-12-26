package cn.lunadeer.furnitureCore.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.*;

/**
 * Utility class for JSON file operations.
 * <p>
 * This depends on the alibaba fastjson library.
 */
public class JsonUtils {
    /**
     * Load a JSON object from a file.
     *
     * @param dataFolder The file to load the JSON object from.
     * @return The JSON object.
     * @throws Exception If an error occurs while reading the file.
     */
    public static JSONObject loadFromFile(File dataFolder) throws Exception {
        try (FileReader reader = new FileReader(dataFolder);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(System.lineSeparator());
            }
            String fileContent = stringBuilder.toString();
            return (JSONObject) JSONObject.parse(fileContent);
        }
    }

    /**
     * Save a JSON object to a file.
     *
     * @param json       The JSON object to save.
     * @param dataFolder The file to save the JSON object to.
     * @throws Exception If an error occurs while writing the file.
     */
    public static void saveToFile(JSONObject json, File dataFolder) throws Exception {
        if (!dataFolder.getParentFile().exists() && !dataFolder.getParentFile().mkdirs()) {
            throw new IOException("Failed to create parent directory: %s".formatted(dataFolder.getParentFile()));
        }
        try (FileWriter fileWriter = new FileWriter(dataFolder);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(JSON.toJSONString(json, true));
        }
    }
}
