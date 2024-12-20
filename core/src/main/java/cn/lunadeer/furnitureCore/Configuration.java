package cn.lunadeer.furnitureCore;

import cn.lunadeer.furnitureCore.utils.configuration.Comment;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationFile;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationPart;

import java.io.File;

public class Configuration extends ConfigurationFile {

    @Comment("The resource pack server for the client to download the resource pack.")
    public static ResourcePackServer resourcePackServer = new ResourcePackServer();

    public static class ResourcePackServer extends ConfigurationPart {
        @Comment("The host of the resource pack server.")
        public static String host = "0.0.0.0";

        @Comment("The port of the resource pack server.")
        public static int port = 8089;
    }

    @Comment("Debug mode, if report bugs turn this on.")
    public static boolean debug = false;

}
