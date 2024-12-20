package cn.lunadeer.furnitureCore;

import cn.lunadeer.furnitureCore.utils.configuration.Comment;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationFile;

import java.io.File;

public class Configuration extends ConfigurationFile {

    @Comment("The port the resource pack server will run on.")
    public static int resourcePackPort = 8089;

    @Comment("Debug mode, if report bugs turn this on.")
    public static boolean debug = false;

}
