package cn.lunadeer.furnitureCore.events;

import cn.lunadeer.furnitureCore.items.FurnitureItemStack;
import cn.lunadeer.furnitureCore.items.ScrewdriverItemStack;
import cn.lunadeer.furnitureCore.utils.XLogger;
import cn.lunadeer.furnitureCoreApi.events.FurnitureBrokenEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

import static cn.lunadeer.furnitureCore.utils.Common.LocationToHash;

public class BreakFurniture implements Listener {

    @EventHandler
    public void onBreakFurniture(PlayerInteractEvent event) {
        if (!event.getAction().isLeftClick()) {
            return;
        }
        if (event.getItem() == null) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        if (block.getType() != Material.BARRIER) {
            return;
        }
        try {
            new ScrewdriverItemStack(event.getItem());
        } catch (IllegalArgumentException e) {
            XLogger.debug("Not a screwdriver: %s", e.getMessage());
            return;
        }
        Location location = block.getLocation();
        // try to break the block
        if(!new BlockBreakEvent(block, event.getPlayer()).callEvent()) {
            return;
        }
        // get item display uid
        NamespacedKey key = new NamespacedKey("furniture", LocationToHash(location));
        String itemDisplayUidStr = location.getWorld().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (itemDisplayUidStr == null) {
            XLogger.info("ItemDisplay not found by key: %s", key);
            return;
        }
        // get item display
        UUID itemDisplayUid = UUID.fromString(itemDisplayUidStr);
        ItemDisplay itemDisplay = (ItemDisplay) location.getWorld().getEntity(itemDisplayUid);
        if (itemDisplay == null) {
            XLogger.debug("ItemDisplay not found by uid: %s", itemDisplayUidStr);
            return;
        }
        // get furniture item stack
        FurnitureItemStack furnitureItemStack;
        try {
            furnitureItemStack = new FurnitureItemStack(itemDisplay.getItemStack());
        } catch (IllegalArgumentException e) {
            XLogger.debug("Not a furniture: %s", e.getMessage());
            return;
        }

        // call FurnitureBrokenEvent if not cancelled do the following
        if (!new FurnitureBrokenEvent(event.getPlayer(), itemDisplay, furnitureItemStack.getModel()).callEvent()) {
            XLogger.debug("FurnitureBrokenEvent cancelled");
            return;
        }
        block.setType(Material.AIR);
        itemDisplay.remove();
        location.getWorld().getPersistentDataContainer().remove(key);
        location.getWorld().dropItem(location, furnitureItemStack);
    }

}
