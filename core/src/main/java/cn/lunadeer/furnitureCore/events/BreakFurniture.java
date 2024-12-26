package cn.lunadeer.furnitureCore.events;

import cn.lunadeer.furnitureCore.blocks.FurnitureBlock;
import cn.lunadeer.furnitureCore.items.ScrewdriverItemStack;
import cn.lunadeer.furnitureCore.utils.XLogger;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BreakFurniture implements Listener {

    @EventHandler
    public void onBreakBarrier(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            return;
        }
        if (event.getBlock().getType() != Material.BARRIER) {
            return;
        }
        try {
            boolean success = new FurnitureBlock(event.getBlock()).tryBreak(event.getPlayer());
            if (success) {
                event.setCancelled(true);
            }
        } catch (IllegalArgumentException e) {
            XLogger.debug(e.getMessage());
        }
    }

    @EventHandler
    public void onBreakFurniture(PlayerInteractEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
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
        if (block.getType().getHardness() != -1) {
            return;
        }
        try {
            new ScrewdriverItemStack(event.getItem());
        } catch (IllegalArgumentException e) {
            XLogger.debug("Not a screwdriver: %s", e.getMessage());
            return;
        }
        // call bukkit event to check if the block can be broken
        if (!new BlockBreakEvent(block, event.getPlayer()).callEvent()) {
            XLogger.debug("BlockBreakEvent cancelled");
            return;
        }
        // do furniture break logic
        try {
            boolean success = new FurnitureBlock(block).tryBreak(event.getPlayer());
            if (success) {
                event.setCancelled(true);
            }
        } catch (IllegalArgumentException e) {
            XLogger.debug(e.getMessage());
        }
    }

}
