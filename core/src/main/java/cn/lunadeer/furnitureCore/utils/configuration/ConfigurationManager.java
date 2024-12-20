package cn.lunadeer.furnitureCore.utils.configuration;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

public class ConfigurationManager {

    public static void load(Class<? extends ConfigurationFile> clazz, File file) throws Exception {
        if (!file.exists()) {
            save(clazz, file);
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        readContent(yaml, clazz, null);
    }

    public static void save(Class<? extends ConfigurationFile> clazz, File file) throws Exception {
        createIfNotExist(file);
        YamlConfiguration yaml = new YamlConfiguration();
        writeContent(yaml, clazz, null);
        yaml.save(file);
    }

    private static void writeContent(YamlConfiguration yaml, Class<?> clazz, String prefix) throws Exception {
        for (Field field : clazz.getFields()) {
            field.setAccessible(true);
            String key = camelToKebab(field.getName());
            if (prefix != null && !prefix.isEmpty()) {
                key = prefix + "." + key;
            }
            if (field.getType().isAssignableFrom(ConfigurationPart.class)) {
                writeContent(yaml, field.getType(), key);
            } else {
                yaml.set(key, field.get(null));
            }
            if (field.isAnnotationPresent(Comment.class)) {
                yaml.setComments(key, List.of(field.getAnnotation(Comment.class).value()));
            }
        }
    }

    private static void createIfNotExist(File file) throws Exception{
        if (file.exists()) return;
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) throw new Exception("Failed to create %s directory.".formatted(file.getParentFile().getAbsolutePath()));
        if (!file.createNewFile()) throw new Exception("Failed to create %s file.".formatted(file.getAbsolutePath()));
    }

    private static void readContent(YamlConfiguration yaml, Class<?> clazz, String prefix) throws Exception {
        for (Field field : clazz.getFields()) {
            field.setAccessible(true);
            String key = camelToKebab(field.getName());
            if (prefix != null && !prefix.isEmpty()) {
                key = prefix + "." + key;
            }
            if (!yaml.contains(key)) {
                continue;
            }
            if (field.getType().isAssignableFrom(ConfigurationPart.class)) {
                readContent(yaml, field.getType(), key);
            } else {
                field.set(null, yaml.get(key));
            }
        }
    }

    /**
     * Converts a camelCase string to kebab-case.
     *
     * @param camel The camelCase string.
     * @return The kebab-case string.
     */
    private static String camelToKebab(String camel) {
        return camel.replaceAll("([a-z])([A-Z]+)", "$1-$2").toLowerCase();
    }

}
