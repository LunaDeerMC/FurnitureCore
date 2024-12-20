package cn.lunadeer.furnitureCore.configuration;

public class ConfigurationManager {

    public static void load(Class<ConfigurationFile> clazz) {

    }

    public static void reload(Class<ConfigurationFile> clazz) {

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
