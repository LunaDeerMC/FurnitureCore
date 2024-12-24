package cn.lunadeer.furnitureCore.items;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ScrewdriverItemStack extends ItemStack {
    private static final NamespacedKey pdcKey = new NamespacedKey("furniture_core", "tools/screwdriver");

    public ScrewdriverItemStack() {
        super(Material.STICK);
        setItemMeta();
    }

    public ScrewdriverItemStack(int amount) {
        super(Material.STICK, amount);
        setItemMeta();
    }

    public ScrewdriverItemStack(ItemStack item) throws IllegalArgumentException {
        super(Material.STICK, item.getAmount());
        if (item.getType() != Material.STICK) {
            throw new IllegalArgumentException("Not a stick.");
        }
        if (item.getItemMeta() == null) {
            throw new IllegalArgumentException("Item meta not found.");
        }
        if (!item.getItemMeta().getPersistentDataContainer().has(pdcKey)) {
            throw new IllegalArgumentException("Not a Screwdriver item.");
        }
        setItemMeta();
    }

    private void setItemMeta() {
        ItemMeta meta = getItemMeta();
        meta.displayName(Component.text("Screwdriver"));
        meta.getPersistentDataContainer().set(pdcKey, PersistentDataType.INTEGER, 1);
        meta.setItemModel(pdcKey);
        meta.lore(List.of(Component.text("Left click to break furniture.")));
        setItemMeta(meta);
    }

    public static ShapelessRecipe getRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(pdcKey, new ScrewdriverItemStack());
        recipe.addIngredient(Material.STICK);
        recipe.addIngredient(Material.IRON_BARS);
        return recipe;
    }

}
