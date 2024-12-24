package cn.lunadeer.furnitureCore.events;

import cn.lunadeer.furnitureCoreApi.managers.ResourcePackManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!ResourcePackManager.getInstance().isReady()) {
            return;
        }
        ResourcePackManager.getInstance().applyToPlayer(event.getPlayer());
    }
}
