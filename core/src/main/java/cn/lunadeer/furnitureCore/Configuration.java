package cn.lunadeer.furnitureCore;

import cn.lunadeer.furnitureCore.utils.configuration.Comments;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationFile;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationPart;

public class Configuration extends ConfigurationFile {

    @Comments("Do not modify this value.")
    public static int version = 6;  // <<<<<< When you change the configuration, you should increment this value.

    @Comments("The resource pack server for the client to download the resource pack.")
    public static ResourcePackServer resourcePackServer = new ResourcePackServer();

    @Comments("Language of the plugin, see others in the plugins/FurnitureCore/languages folder.")
    public static String language = "en_us";

    public static class ResourcePackServer extends ConfigurationPart {
        @Comments({
                "Your public ip or domain players use to connect to the server.",
                "If you are using a domain the record should be an A record pointing to your server's ip."
        })
        public String host = "replace.to.your.domain.or.ip";

        @Comments("The port of the resource pack server.")
        public int port = 8089;
    }

    @Comments("The settings of the resource pack.")
    public static ResourcePackSettings resourcePackSettings = new ResourcePackSettings();

    public static class ResourcePackSettings extends ConfigurationPart {
        @Comments("The namespace of the resources.")
        public String namespace = "furniture_core";

        @Comments("The name of the resource pack.")
        public String packName = "furniture-core-resource-pack";

        @Comments("Weather the resource pack is force required to join the server.")
        public boolean required = false;
    }

    @Comments({
            "Use the internal recipes of the models.",
            "If you want to customize the recipes with other plugins (e.g. Craftorithm), turn this off."
    })
    public static boolean modelInternalRecipes = true;

    @Comments("Debug mode, if report bugs turn this on.")
    public static boolean debug = false;

}
