package cn.lunadeer.furnitureCore.item;

import cn.lunadeer.furnitureCore.FurnitureCore;
import cn.lunadeer.furnitureCore.FurnitureModel;
import cn.lunadeer.furnitureCore.ModelManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class FurnitureItemStack extends ItemStack {
    private FurnitureModel model;
    private static final NamespacedKey pdcKey = new NamespacedKey("furniture_core", "item");

    /**
     * Cast an item stack to furniture item.
     * If the item stack is not a furniture item, an exception will be thrown.
     * If the item stack is a furniture item, the model will be set.
     *
     * @param item The item stack.
     */
    public FurnitureItemStack(@NotNull ItemStack item) throws IllegalArgumentException {
        super(Material.ITEM_FRAME, item.getAmount());
        if (item.getType() != Material.ITEM_FRAME) {
            throw new IllegalArgumentException("Not an item frame.");
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            throw new IllegalArgumentException("Item meta not found.");
        }
        if (!meta.getPersistentDataContainer().has(pdcKey, PersistentDataType.STRING)) {
            throw new IllegalArgumentException("Not a furniture item.");
        }
        if (meta.getItemModel() == null) {
            throw new IllegalArgumentException("Not a furniture item.");
        }
        FurnitureModel model = ModelManager.getInstance().getModel(meta.getItemModel());
        if (model == null) {
            throw new IllegalArgumentException("Model not found.");
        }
        setItemMeta(model);
    }

    /**
     * Create an item stack of furniture model.
     *
     * @param callableName The callable name of the model.
     */
    public FurnitureItemStack(String callableName) {
        this(callableName, 1);
    }

    /**
     * Create an item stack of furniture model.
     *
     * @param callableName The callable name of the model.
     * @param size         The size of the item stack.
     */
    public FurnitureItemStack(String callableName, Integer size) {
        super(Material.ITEM_FRAME, size);
        FurnitureModel model = ModelManager.getInstance().getModel(callableName);
        if (model == null) {
            throw new IllegalArgumentException("Model not found.");
        }
        setItemMeta(model);
    }

    /**
     * Create an item stack of furniture model.
     *
     * @param furnitureModel The furniture model.
     */
    public FurnitureItemStack(@NotNull FurnitureModel furnitureModel) {
        this(furnitureModel, 1);
    }

    /**
     * Create an item stack of furniture model.
     *
     * @param furnitureModel The furniture model.
     * @param size           The size of the item stack.
     */
    public FurnitureItemStack(@NotNull FurnitureModel furnitureModel, Integer size) {
        super(Material.ITEM_FRAME, size);
        setItemMeta(furnitureModel);
    }

    private void setItemMeta(@NotNull FurnitureModel furnitureModel) {
        model = furnitureModel;
        ItemMeta meta = getItemMeta();
        meta.displayName(Component.text(furnitureModel.getCustomName()));
        meta.getPersistentDataContainer().set(pdcKey, PersistentDataType.STRING, furnitureModel.getCallableNameWithNamespace());
        meta.setItemModel(model.getItemModelKey());
        setItemMeta(meta);
    }

    public FurnitureModel getModel() {
        return model;
    }
}
