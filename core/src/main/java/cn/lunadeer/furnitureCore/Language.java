package cn.lunadeer.furnitureCore;

import cn.lunadeer.furnitureCore.commands.Apply;
import cn.lunadeer.furnitureCore.commands.CommandParser;
import cn.lunadeer.furnitureCore.commands.Give;
import cn.lunadeer.furnitureCore.commands.Reload;
import cn.lunadeer.furnitureCore.managers.ModelManagerImpl;
import cn.lunadeer.furnitureCore.managers.ResourcePackManagerImpl;
import cn.lunadeer.furnitureCore.models.FurnitureModelImpl;
import cn.lunadeer.furnitureCore.utils.Notification;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationFile;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationManager;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationPart;
import cn.lunadeer.furnitureCore.utils.configuration.PostProcess;
import cn.lunadeer.furnitureCoreApi.items.ScrewdriverItemStack;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.List;

public class Language extends ConfigurationFile {

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

    public static CommandParser.CommandParserText commandParserText = new CommandParser.CommandParserText();

    public static Reload.ReloadCommandText reloadCommandText = new Reload.ReloadCommandText();

    public static Give.GiveCommandText giveCommandText = new Give.GiveCommandText();

    public static Apply.ApplyCommandText applyCommandText = new Apply.ApplyCommandText();

    public static void load(CommandSender sender) {
        try {
            final List<String> languages = List.of(
                    "languages/en_us.yml",
                    "languages/zh_cn.yml"
            );
            for (String language : languages) {
                FurnitureCore.getInstance().saveResource(language, false);
            }
            File languageFile = new File(FurnitureCore.getInstance().getDataFolder(), "languages/" + Configuration.language + ".yml");
            ConfigurationManager.load(Language.class, languageFile);    // This will save the default language file if it doesn't exist.
        } catch (Exception e) {
            Notification.error(sender, Language.furnitureCoreText.failToLoadLanguage, e.getMessage());
        }
    }
}
