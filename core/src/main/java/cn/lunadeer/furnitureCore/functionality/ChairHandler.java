package cn.lunadeer.furnitureCore.functionality;

import cn.lunadeer.furnitureCore.Configuration;
import cn.lunadeer.furnitureCore.blocks.FurnitureBlock;
import cn.lunadeer.furnitureCore.utils.XLogger;
import cn.lunadeer.furnitureCoreApi.events.FurnitureBrokenEvent;
import cn.lunadeer.furnitureCoreApi.functionality.Functionality;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public class ChairHandler implements Listener {

    private static final float HEIGHT_OFFSET = -0.99f;
    private static final NamespacedKey isChair = new NamespacedKey("this_is", "chair");
    private static float GET_HEIGHT(Float height) {
        return HEIGHT_OFFSET + height;
    }

    @EventHandler
    public void onPlayerSit(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        if (block.getType() != Material.BARRIER) {
            return;
        }

        // Check if furniture block is a chair.
        FurnitureBlock furnitureBlock;
        try {
            furnitureBlock = new FurnitureBlock(block);
        } catch (IllegalArgumentException e) {
            XLogger.debug(e.getMessage());
            return;
        }
        Functionality functionality = furnitureBlock.getFurnitureItemStack().getModel().getFunction();
        if (!(functionality instanceof ChairFunction chairFunction)) {
            return;
        }

        // Check if block beneath chair is solid.
        if (block.getRelative(BlockFace.DOWN).isEmpty())
            return;

        // Check if player is sitting.
        if (!player.isSneaking() && player.getVehicle() != null) {
            player.getVehicle().remove();
            return;
        }

        // Check for distance between player and chair.
        if (player.getLocation().distance(block.getLocation().add(0.5, 0, 0.5)) > 2)
            return;

        // Sit-down process.
        if (player.getVehicle() != null)
            player.getVehicle().remove();

        ArmorStand drop = dropSeat(block.getLocation(), furnitureBlock.getItemDisplay().getLocation().getDirection(), chairFunction.getHeight());

        // Changing the drop material is only necessary for the item merge feature of CB++
        // The client won't update the material, though.
        if (!drop.addPassenger(player)) {
            XLogger.debug("Failed to make player " + player.getName() + " sit on a chair.");
        } else {
            XLogger.debug("Player " + player.getName() + " is sitting on a chair.");
        }

        // Cancel BlockPlaceEvent Result, if player is rightclicking with a block in his hand.
        event.setUseInteractedBlock(Event.Result.DENY);
    }

    @EventHandler
    public void onPassengerLeave(EntityDismountEvent event) {
        Entity vehicle = event.getDismounted();
        Entity passenger = event.getEntity();
        if (!(vehicle instanceof ArmorStand)) {
            return;
        }
        if (!(passenger instanceof Player)) {
            return;
        }
        vehicle.remove();
        passenger.teleportAsync(passenger.getLocation().add(0, HEIGHT_OFFSET * -1 + 1, 0));
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Entity vehicle = event.getPlayer().getVehicle();
        if (vehicle == null) {
            return;
        }
        if (vehicle instanceof ArmorStand && vehicle.getPersistentDataContainer().has(isChair, PersistentDataType.BYTE)) {
            event.setTo(event.getTo().add(0, HEIGHT_OFFSET * -1 + 1, 0));
        }
    }

    @EventHandler
    public void onSitBroken(FurnitureBrokenEvent event) {
        if (event.getModel().getFunction() instanceof ChairFunction chairFunction) {
            clearSeat(event.getLocation(), chairFunction.getHeight());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Entity vehicle = event.getPlayer().getVehicle();
        // Let players stand up when leaving the server.
        if (vehicle instanceof ArmorStand && vehicle.getPersistentDataContainer().has(isChair, PersistentDataType.BYTE)) {
            vehicle.remove();
            event.getPlayer().teleportAsync(event.getPlayer().getLocation().add(0, HEIGHT_OFFSET * -1 + 1, 0));
        }
    }

    private static ArmorStand dropSeat(Location blockLocation, Vector direction, Float height) {
        clearSeat(blockLocation.clone(), height);
        Location location = blockLocation.add(0.5, GET_HEIGHT(height), 0.5);
        location.setDirection(direction);
        ArmorStand armorStand = (ArmorStand) blockLocation.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.getPersistentDataContainer().set(isChair, PersistentDataType.BYTE, (byte) 1);
        if (!Configuration.debug) {
            armorStand.setVisible(false);
        }
        armorStand.setGravity(false);
        armorStand.setInvulnerable(true);
        armorStand.setSmall(true);
        armorStand.setNoPhysics(true);
        armorStand.setCanMove(false);
        armorStand.setCanTick(false);
        armorStand.setCanPickupItems(false);
        armorStand.setCollidable(false);
        XLogger.debug("Chair dropped at " + location.toString());
        return armorStand;
    }

    private static void clearSeat(Location blockLocation, Float height) {
        Location location = blockLocation.add(0.5, GET_HEIGHT(height), 0.5);
        for (Entity e : location.getWorld().getNearbyEntities(location, 0.4, 0.4, 0.4)) {
            if (e instanceof ArmorStand && e.getPersistentDataContainer().has(isChair, PersistentDataType.BYTE)) {
                e.remove();
            }
        }
    }

}
