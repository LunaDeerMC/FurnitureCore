package cn.lunadeer.furnitureCore.events;

import cn.lunadeer.furnitureCore.utils.XLogger;
import cn.lunadeer.furnitureCoreApi.events.FurnitureCraftedEvent;
import cn.lunadeer.furnitureCoreApi.items.FurnitureItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class CraftFurniture implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCraftFurniture(CraftItemEvent event) {
        if(event.isCancelled()) {
            return;
        }
        ItemStack item = event.getCurrentItem();
        if (item == null) {
            return;
        }
        FurnitureItemStack furnitureItemStack;
        try {
            furnitureItemStack = new FurnitureItemStack(item);
        } catch (IllegalArgumentException e) {
            XLogger.debug("Not a furniture: %s", e.getMessage());
            return;
        }
        if (!new FurnitureCraftedEvent(
                event.getRecipe(),
                event.getView(),
                event.getSlotType(),
                event.getSlot(),
                event.getClick(),
                event.getAction(),
                furnitureItemStack
        ).callEvent()) {
            XLogger.debug("FurnitureCraftedEvent cancelled");
            event.setCancelled(true);
        }
    }
}
