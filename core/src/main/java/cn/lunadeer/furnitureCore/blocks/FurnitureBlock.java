package cn.lunadeer.furnitureCore.blocks;

import cn.lunadeer.furnitureCore.items.FurnitureItemStack;
import cn.lunadeer.furnitureCore.utils.XLogger;
import cn.lunadeer.furnitureCoreApi.events.FurnitureBrokenEvent;
import cn.lunadeer.furnitureCoreApi.events.FurniturePlacedEvent;
import cn.lunadeer.furnitureCoreApi.events.HangingFurniturePlacedEvent;
import cn.lunadeer.furnitureCoreApi.events.RotateFurniturePlacedEvent;
import cn.lunadeer.furnitureCoreApi.models.FurnitureModel;
import cn.lunadeer.furnitureCoreApi.models.Rotation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.Locale;
import java.util.UUID;

import static cn.lunadeer.furnitureCore.utils.Common.LocationToHash;

public class FurnitureBlock {
    private final FurnitureItemStack furnitureItemStack;
    private final Location location;
    private final NamespacedKey key;
    private ItemDisplay itemDisplay;

    /**
     * Construct a furniture block from a furniture item stack with specific location.
     * <p>
     * FOR PLACE.
     *
     * @param furnitureItemStack The furniture item stack.
     * @param location           The location to place the furniture.
     */
    public FurnitureBlock(FurnitureItemStack furnitureItemStack, Location location) {
        this.furnitureItemStack = furnitureItemStack;
        this.location = location;
        this.key = new NamespacedKey("furniture", LocationToHash(location));
    }

    /**
     * Construct a furniture block from a bukkit block.
     * <p>
     * FOR BREAK.
     *
     * @param block The block might be a furniture block.
     * @throws IllegalArgumentException If the block is not a furniture block.
     */
    public FurnitureBlock(Block block) throws IllegalArgumentException {
        this.location = block.getLocation();
        this.key = new NamespacedKey("furniture", LocationToHash(location));
        // get item display uid
        String itemDisplayUidStr = location.getWorld().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (itemDisplayUidStr == null) {
            throw new IllegalArgumentException("ItemDisplay not found by key: %s".formatted(key));
        }
        // get item display
        UUID itemDisplayUid = UUID.fromString(itemDisplayUidStr);
        itemDisplay = (ItemDisplay) location.getWorld().getEntity(itemDisplayUid);
        if (itemDisplay == null) {
            clearBarrierInCase(location);
            throw new IllegalArgumentException("ItemDisplay not found by uid: %s".formatted(itemDisplayUidStr));
        }
        // get furniture item stack
        try {
            furnitureItemStack = new FurnitureItemStack(itemDisplay.getItemStack());
        } catch (IllegalArgumentException e) {
            clearBarrierInCase(location);
            throw new IllegalArgumentException("Not a furniture: %s".formatted(e.getMessage()));
        }
    }

    public Location getLocation() {
        return location;
    }

    public FurnitureItemStack getFurnitureItemStack() {
        return furnitureItemStack;
    }

    public NamespacedKey getKey() {
        return key;
    }


    /**
     * The placeholder block type.
     * <p>
     * This can be overridden to place a custom block.
     */
    private Material getPlaceholderBlockType() {
        return Material.BARRIER;
    }

    /**
     * Place an item display at the location.
     *
     * @param player    The player who places the item display.
     * @param blockFace The block face to place the item display.
     * @param rotate    The rotation of the item display.
     * @return The item display. Null if failed.
     */
    public ItemDisplay tryPlace(Player player, BlockFace blockFace, Rotation rotate) {
        FurnitureModel model = furnitureItemStack.getModel();
        if (model.canHanging()) {
            if (!new HangingFurniturePlacedEvent(player, location, furnitureItemStack.getModel(), blockFace).callEvent()) {
                XLogger.debug("HangingFurniturePlacedEvent cancelled");
                return null;
            }
        } else if (model.canRotate()) {
            if (!new RotateFurniturePlacedEvent(player, location, furnitureItemStack.getModel(), rotate).callEvent()) {
                XLogger.debug("RotateFurniturePlacedEvent cancelled");
                return null;
            }
        } else {
            if (!new FurniturePlacedEvent(player, location, furnitureItemStack.getModel()).callEvent()) {
                XLogger.debug("FurniturePlacedEvent cancelled");
                return null;
            }
        }
        location.getBlock().setType(getPlaceholderBlockType());
        location.getBlock().getState().update();
        location.add(0.5, 0.5, 0.5);
        itemDisplay = (ItemDisplay) location.getWorld().spawnEntity(location, EntityType.ITEM_DISPLAY);
        itemDisplay.setItemStack(furnitureItemStack);
        itemDisplay.getPersistentDataContainer().set(new NamespacedKey("FurnitureCore".toLowerCase(Locale.ROOT), "furniture"), PersistentDataType.BOOLEAN, true); // optimize from DeerFolia will skip entity on tick with this key
        itemDisplay.setGravity(false);
        itemDisplay.setInvulnerable(true);
        if (model.canHanging()) {
            float yaw = itemDisplay.getYaw();
            float pitch = itemDisplay.getPitch();
            if (blockFace == BlockFace.DOWN) {
                // todo don't know why pitch can only between +/-90
                // pitch = 180;
            } else if (blockFace == BlockFace.NORTH) {
                pitch = 90;
                yaw = 180;
            } else if (blockFace == BlockFace.EAST) {
                pitch = 90;
                yaw = 270;
            } else if (blockFace == BlockFace.SOUTH) {
                pitch = 90;
            } else if (blockFace == BlockFace.WEST) {
                pitch = 90;
                yaw = 90;
            }
            XLogger.debug("yaw: %f, pitch: %f, block_face: %s", yaw, pitch, blockFace);
            itemDisplay.setRotation(yaw, pitch);
        } else if (model.canRotate() && !model.canHanging()) {
            itemDisplay.setRotation(rotate.getAngle(), itemDisplay.getPitch());
        }
        location.getWorld().getPersistentDataContainer().set(key, PersistentDataType.STRING, itemDisplay.getUniqueId().toString());
        return itemDisplay;
    }

    public boolean tryBreak(Player player) {
        if (itemDisplay == null) {
            clearBarrierInCase(location);
            throw new IllegalArgumentException("ItemDisplay is null for location: %s".formatted(location));
        }
        // call FurnitureBrokenEvent if not cancelled do the following
        if (!new FurnitureBrokenEvent(player, location, itemDisplay, furnitureItemStack.getModel()).callEvent()) {
            XLogger.debug("FurnitureBrokenEvent cancelled");
            return false;
        }
        location.getBlock().setType(Material.AIR);
        location.getBlock().getState().update();
        itemDisplay.remove();
        location.getWorld().getPersistentDataContainer().remove(key);
        location.getWorld().dropItem(location, new FurnitureItemStack(furnitureItemStack, 1));
        return true;
    }

    /**
     * Just in case the item display is not found but the unbreakable block is found.
     * Mostly because the item display is removed by accident.
     *
     * @param location The location to clear the unbreakable block.
     */
    private static void clearBarrierInCase(Location location) {
        if (location.getBlock().getType().getHardness() == -1) {
            XLogger.warn("Unbreakable block at %s but no furniture item display associated. Removed.", location);
            location.getBlock().setType(Material.AIR);
            location.getBlock().getState().update();
        }
    }
}
