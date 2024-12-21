package cn.lunadeer.furnitureCore.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.*;

public class JsonUtils {
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

    public static void saveToFile(JSONObject json, File dataFolder) throws Exception {
        try (FileWriter fileWriter = new FileWriter(dataFolder);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(JSON.toJSONString(json, true));
        }
    }
}
