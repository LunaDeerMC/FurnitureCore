package cn.lunadeer.furnitureCore.commands;

import cn.lunadeer.furnitureCore.Configuration;
import cn.lunadeer.furnitureCore.Language;
import cn.lunadeer.furnitureCore.utils.Notification;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationPart;
import cn.lunadeer.furnitureCoreApi.managers.ResourcePackManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * /furniture-reload [type]
 * - `type`：重载类型。
 * - `all`：全部重载（默认）；
 * - `config`：重载配置文件；
 * - `resource`：重新生成资源文件；
 */
public class Reload implements TabExecutor {

    public static class ReloadCommandText extends ConfigurationPart {
        public String reloadingConfig = "Reloading configuration file...";
        public String reloadConfigSuccess = "Configuration file reloaded successfully!";
        public String reloadingResource = "Reloading resource files...";
        public String reloadResourceSuccess = "Resource files reloaded successfully!";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            if (args.length == 0) {
                // Reload all
                reloadConfig(sender);
                reloadResource(sender);
                return true;
            } else {
                switch (args[0]) {
                    case "all" -> {
                        // Reload all
                        reloadConfig(sender);
                        reloadResource(sender);
                        return true;
                    }
                    case "config" -> {
                        // Reload config
                        reloadConfig(sender);
                        return true;
                    }
                    case "resource" -> {
                        // Reload resource
                        reloadResource(sender);
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            Notification.error(sender, e.getMessage());
            return false;
        }
    }

    public static void reloadConfig(CommandSender sender) {
        Notification.info(sender, Language.reloadCommandText.reloadingConfig);
        if (Configuration.load(sender)) {
            Notification.info(sender, Language.reloadCommandText.reloadConfigSuccess);
        }
    }

    public static void reloadResource(CommandSender sender) {
        try {
            Notification.info(sender, Language.reloadCommandText.reloadingResource);
            ResourcePackManager.getInstance().loadModelsFromDisk(sender);
            ResourcePackManager.getInstance().generateResourcePack(sender);
            Notification.info(sender, Language.reloadCommandText.reloadResourceSuccess);
        } catch (Exception e) {
            Notification.error(sender, e.getMessage());
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("all", "config", "resource");
        }
        return null;
    }
}
