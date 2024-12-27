package cn.lunadeer.furnitureCore.events;

import cn.lunadeer.furnitureCore.blocks.FurnitureBlock;
import cn.lunadeer.furnitureCore.utils.XLogger;
import cn.lunadeer.furnitureCoreApi.items.ScrewdriverItemStack;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class RotateFurniture implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRotateFurniture(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }
        if (event.getItem() == null) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        if (block.getType().getHardness() != -1) {
            return;
        }
        try {
            new ScrewdriverItemStack(event.getItem());
        } catch (IllegalArgumentException e) {
            XLogger.debug("Not a screwdriver: %s", e.getMessage());
            return;
        }
        event.setCancelled(true);
        // do furniture rotate logic
        try {
            new FurnitureBlock(block).tryRotate(event.getPlayer());
        } catch (IllegalArgumentException e) {
            XLogger.debug(e.getMessage());
        }
    }

}
