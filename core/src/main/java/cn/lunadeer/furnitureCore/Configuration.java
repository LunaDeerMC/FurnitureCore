package cn.lunadeer.furnitureCore;

import cn.lunadeer.furnitureCore.utils.configuration.Comment;
import cn.lunadeer.furnitureCore.utils.configuration.Comments;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationFile;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationPart;

public class Configuration extends ConfigurationFile {

    @Comment("Do not modify this value.")
    public static int version = 2;

    @Comment("The resource pack server for the client to download the resource pack.")
    public static ResourcePackServer resourcePackServer = new ResourcePackServer();

    public static class ResourcePackServer extends ConfigurationPart {
        @Comment("The host of the resource pack server.")
        public String host = "0.0.0.0";

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

        @Comments({
                "The url will be used by the client to download the resource pack. Make sure it's accessible from the client.",
                "<public-ip>: usually the same ip (or domain) players use to connect to the server.",
                "<port>: the port you set above.",
                "If you are using a domain, make sure it's correctly resolved to your public ip."
        })
        public String url = "http://<public-ip>:<port>/furniture-core-resource-pack.zip";

        @Comment("Weather the resource pack is force required to join the server.")
        public boolean required = false;
    }

    @Comment("Debug mode, if report bugs turn this on.")
    public static boolean debug = false;

}
