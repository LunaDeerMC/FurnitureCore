package cn.lunadeer.furnitureCore.utils;

import org.bukkit.Location;

import java.io.File;

public class Common {

    public static boolean isPaper() {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static String LocationToHash(Location location) {
        String locationStr = location.getWorld().getUID() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
        return Integer.toString(locationStr.hashCode());
    }

    /**
     * Delete a folder recursively.
     *
     * @param folder the folder to delete
     * @return true if success, false if failed
     */
    public static boolean DeleteFolderRecursively(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    DeleteFolderRecursively(f);
                } else {
                    if (!f.delete()) {
                        return false;
                    }
                }
            }
        }
        return folder.delete();
    }

}
