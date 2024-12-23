package cn.lunadeer.furnitureCore;

import cn.lunadeer.furnitureCore.utils.configuration.Comment;
import cn.lunadeer.furnitureCore.utils.configuration.Comments;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationFile;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationPart;

public class Configuration extends ConfigurationFile {

    @Comment("Do not modify this value.")
    public static int version = 3;

    @Comment("The resource pack server for the client to download the resource pack.")
    public static ResourcePackServer resourcePackServer = new ResourcePackServer();

    public static class ResourcePackServer extends ConfigurationPart {
        @Comments({
                "Your public ip or domain players use to connect to the server.",
                "If you are using a domain the record should be an A record pointing to your server's ip."
        })
        public String host = "replace.to.your.domain.or.ip";

        @Comment("The port of the resource pack server.")
        public int port = 8089;
    }

    @Comment("The settings of the resource pack.")
    public static ResourcePackSettings resourcePackSettings = new ResourcePackSettings();

    public static class ResourcePackSettings extends ConfigurationPart {
        @Comment("The namespace of the resources.")
        public String namespace = "furniture_core";

        @Comments({
                "The name of the resource pack.",
                "If change this, don't forget to change the name in url below."
        })
        public String packName = "furniture-core-resource-pack";

        @Comment("Weather the resource pack is force required to join the server.")
        public boolean required = false;
    }

    @Comment("Debug mode, if report bugs turn this on.")
    public static boolean debug = false;

}
