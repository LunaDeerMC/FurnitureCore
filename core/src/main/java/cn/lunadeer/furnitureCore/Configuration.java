package cn.lunadeer.furnitureCore;

import cn.lunadeer.furnitureCore.configuration.Comment;
import cn.lunadeer.furnitureCore.configuration.ConfigurationFile;

import java.io.File;

public class Configuration extends ConfigurationFile {

    @Comment("The port the resource pack server will run on.")
    public static int resourcePackPort;

    @Comment("Debug mode, if report bugs turn this on.")
    public static boolean debug;

    @Override
    public File getFilePath() {
        return new File(FurnitureCore.getInstance().getDataFolder(), "config.yml");
    }
}
