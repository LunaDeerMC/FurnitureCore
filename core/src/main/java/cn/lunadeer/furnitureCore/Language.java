package cn.lunadeer.furnitureCore;

import cn.lunadeer.furnitureCore.managers.ModelManagerImpl;
import cn.lunadeer.furnitureCore.managers.ResourcePackManagerImpl;
import cn.lunadeer.furnitureCore.models.FurnitureModelImpl;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationFile;

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
}
