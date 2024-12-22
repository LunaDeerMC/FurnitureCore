package cn.lunadeer.furnitureCore;

import cn.lunadeer.furnitureCore.utils.configuration.Comment;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationFile;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationPart;

public class Configuration extends ConfigurationFile {

    @Comment("Do not modify this value.")
    public static int version = 1;

    @Comment("The namespace of the extra resources.")
    public static String namespace = "furniture_core";

    @Comment("The resource pack server for the client to download the resource pack.")
    public static ResourcePackServer resourcePackServer = new ResourcePackServer();

    public static class ResourcePackServer extends ConfigurationPart {
        @Comment("The host of the resource pack server.")
        public String host = "0.0.0.0";

        @Comment("The port of the resource pack server.")
        public int port = 8089;

        @Comment("The url will be used by the client to download the resource pack. Make sure it's accessible from the client.")
        public String url = "http://127.0.0.1:8089";

        @Comment("Weather the resource pack is force required to join the server.")
        public boolean required = false;
    }

    @Comment("Debug mode, if report bugs turn this on.")
    public static boolean debug = false;

}
