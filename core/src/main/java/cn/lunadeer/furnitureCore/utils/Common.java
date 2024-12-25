package cn.lunadeer.furnitureCore.utils;

import org.bukkit.Location;

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

}
