package cn.lunadeer.furnitureCore;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class FurnitureItem extends ItemStack {

    /**
     * Check if the item is a furniture item.
     *
     * @param item The item stack.
     * @return True if the item is a type of furniture.
     */
    public static boolean IsFurnitureItem(ItemStack item) {
        if (item == null) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(FurnitureCore.getPDCKey());
    }

    /**
     * Create an item stack of furniture model.
     *
     * @param callableName The callable name of the model.
     * @return The item stack.
     */
    public static ItemStack CreateItemStackOf(String callableName) {
        return CreateItemStackOf(callableName, 1);
    }

    /**
     * Create an item stack of furniture model.
     *
     * @param callableName The callable name of the model.
     * @param size         The size of the item stack.
     * @return The item stack.
     */
    public static ItemStack CreateItemStackOf(String callableName, Integer size) {
        FurnitureModel model = ModelManager.getInstance().getModelByCallableName(callableName);
        if (model == null) {
            throw new IllegalArgumentException("Model not found.");
        }
        return CreateItemStackOf(model, size);
    }

    /**
     * Create an item stack of furniture model.
     *
     * @param furnitureModel The furniture model.
     * @return The item stack.
     */
    public static ItemStack CreateItemStackOf(@NotNull FurnitureModel furnitureModel) {
        return CreateItemStackOf(furnitureModel, 1);
    }

    /**
     * Create an item stack of furniture model.
     *
     * @param furnitureModel The furniture model.
     * @param size           The size of the item stack.
     * @return The item stack.
     */
    public static ItemStack CreateItemStackOf(@NotNull FurnitureModel furnitureModel, Integer size) {
        if (!ResourcePackManager.getInstance().isReady()) {
            throw new IllegalStateException("Resource pack not ready.");
        }
        ItemStack item = new ItemStack(Material.ITEM_FRAME, size);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(furnitureModel.getCustomName()));
        meta.getPersistentDataContainer().set(FurnitureCore.getPDCKey(), PersistentDataType.BYTE, (byte) 1);
        meta.setCustomModelData(furnitureModel.getIndex());
        item.setItemMeta(meta);
        return item;
    }
}
