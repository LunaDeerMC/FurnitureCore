package cn.lunadeer.furnitureCore.events;

import cn.lunadeer.furnitureCore.blocks.FurnitureBlock;
import cn.lunadeer.furnitureCore.utils.XLogger;
import cn.lunadeer.furnitureCoreApi.items.FurnitureItemStack;
import cn.lunadeer.furnitureCoreApi.models.Rotation;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlaceFurniture implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlaceFurniture(HangingPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Block clickedBlock = event.getBlock();
        FurnitureItemStack furnitureItemStack;
        if (event.getItemStack() == null) {
            return;
        }
        try {
            furnitureItemStack = new FurnitureItemStack(event.getItemStack());
        } catch (IllegalArgumentException e) {
            XLogger.debug("Not a furniture: %s", e.getMessage());
            return;
        }
        event.setCancelled(true);
        Location location = clickedBlock.getRelative(event.getBlockFace()).getLocation();
        if (location.getBlock().getType().isSolid()) {
            XLogger.debug("Block is solid");
            return;
        }
        // call build event to check if the block can be placed
        if (!new BlockPlaceEvent(location.getBlock(), location.getBlock().getState(), clickedBlock, furnitureItemStack, event.getPlayer(), true, EquipmentSlot.HAND).callEvent()) {
            XLogger.debug("BlockPlaceEvent cancelled");
            return;
        }
        // do furniture place logic
        ItemDisplay itemDisplay = new FurnitureBlock(furnitureItemStack, location).
                tryPlace(
                        event.getPlayer(),
                        event.getBlockFace(),
                        Rotation.fromYaw(event.getPlayer().getYaw() + 180)  // +180 to make it face the player
                );
        if (itemDisplay != null) {
            event.getItemStack().setAmount(event.getItemStack().getAmount() - 1);
        }
    }

}
