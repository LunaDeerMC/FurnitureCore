package cn.lunadeer.furnitureCore;

import cn.lunadeer.furnitureCore.managers.ModelManagerImpl;
import cn.lunadeer.furnitureCore.managers.ResourcePackManagerImpl;
import cn.lunadeer.furnitureCore.models.FurnitureModelImpl;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationFile;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationPart;
import cn.lunadeer.furnitureCore.utils.configuration.PostProcess;
import cn.lunadeer.furnitureCoreApi.items.ScrewdriverItemStack;
import net.kyori.adventure.text.Component;

import java.util.List;

public class Language extends ConfigurationFile {

    public static int version = 1;

    public static FurnitureCore.FurnitureCoreText
            furnitureCoreText = new FurnitureCore.FurnitureCoreText();

    public static ModelManagerImpl.ModelManagerText
            modelManagerText = new ModelManagerImpl.ModelManagerText();

    public static ResourcePackManagerImpl.ResourcePackManagerText
            resourcePackManagerText = new ResourcePackManagerImpl.ResourcePackManagerText();

    public static FurnitureModelImpl.FurnitureModelText
            furnitureModelText = new FurnitureModelImpl.FurnitureModelText();

    public static FurnitureModelImpl.ParseRecipeText
            parseRecipeText = new FurnitureModelImpl.ParseRecipeText();

    public static ScrewdriverItemStackText screwdriverItemStackText = new ScrewdriverItemStackText();
    public static class ScrewdriverItemStackText extends ConfigurationPart {
        public String displayName = "Screwdriver";
        public String desc1 = "Left click to break the furniture.";
        public String desc2 = "Right click to rotate the furniture.";
    }
    @PostProcess
    public static void applyScrewdriverText() {
        ScrewdriverItemStack.displayName = Component.text(screwdriverItemStackText.displayName);
        ScrewdriverItemStack.lore = List.of(
                Component.text(screwdriverItemStackText.desc1),
                Component.text(screwdriverItemStackText.desc2)
        );
    }
}
