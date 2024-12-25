package cn.lunadeer.furnitureCore.events;

import cn.lunadeer.furnitureCore.FurnitureCore;
import cn.lunadeer.furnitureCore.items.FurnitureItemStack;
import cn.lunadeer.furnitureCore.utils.XLogger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;

import static cn.lunadeer.furnitureCore.utils.Common.LocationToHash;

public class PlaceFurniture implements Listener {

    @EventHandler
    public void onPlaceFurniture(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }
        if (event.getItem() == null) {
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        FurnitureItemStack furnitureItemStack;
        try {
            furnitureItemStack = new FurnitureItemStack(event.getItem());
        } catch (IllegalArgumentException e) {
            XLogger.debug("Not a furniture: %s", e.getMessage());
            return;
        }
        Location location = clickedBlock.getRelative(event.getBlockFace()).getLocation();
        if (location.getBlock().getType().isSolid()) {
            XLogger.debug("Block is solid");
            return;
        }
        // try to place the block
        if (!new BlockPlaceEvent(location.getBlock(), location.getBlock().getState(), clickedBlock, furnitureItemStack, event.getPlayer(), true, EquipmentSlot.HAND).callEvent()) {
            XLogger.debug("BlockPlaceEvent cancelled");
            return;
        }

        // todo call FurniturePlacedEvent if not cancelled do the following
        location.getBlock().setType(Material.BARRIER);
        ItemDisplay itemDisplay = (ItemDisplay) location.getWorld().spawnEntity(location, EntityType.ITEM_DISPLAY);
        // todo: configure item display to proper size and rotation
        NamespacedKey key = new NamespacedKey("furniture", LocationToHash(location));
        location.getWorld().getPersistentDataContainer().set(key, PersistentDataType.STRING, itemDisplay.getUniqueId().toString());
    }

}
