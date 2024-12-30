package cn.lunadeer.furnitureCore.commands;

import cn.lunadeer.furnitureCore.FurnitureCore;
import cn.lunadeer.furnitureCore.Language;
import cn.lunadeer.furnitureCore.utils.Notification;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationPart;
import cn.lunadeer.furnitureCoreApi.managers.ResourcePackManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static cn.lunadeer.furnitureCore.utils.Common.getOnlinePlayerNames;

/**
 * /furniture-apply <player> [force]
 * - `player`：要应用资源包的玩家。
 *   - `@s`：自己；
 *   - `@a`：所有玩家；
 *   - `@r`：随机玩家；
 *   - `@p`：最近的玩家。
 *   - 玩家名称。
 * - `force`：是否强制应用（默认为 `false`）。
 */
public class Apply implements TabExecutor {

    public static class ApplyCommandText extends ConfigurationPart {
        public String applyResourcePack = "Applying resource pack to %s...";
        public String applyResourcePackSuccess = "Resource pack applied successfully to %s!";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            if (args.length < 1) {
                return false;
            }
            List<Player> targets = CommandParser.getPlayersWithArg(sender, args[0]);
            boolean force = args.length > 1 && Boolean.parseBoolean(args[1]);

            for (Player target : targets) {
                Notification.info(sender, Language.applyCommandText.applyResourcePack.formatted(target.getName()));
                try {
                    ResourcePackManager.getInstance().applyToPlayer(target, force);
                    Notification.info(sender, Language.applyCommandText.applyResourcePackSuccess.formatted(target.getName()));
                } catch (Exception e) {
                    Notification.error(sender, e.getMessage());
                }
            }

            return true;
        } catch (Exception e) {
            Notification.error(sender, e.getMessage());
            return false;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> names = new java.util.ArrayList<>(List.of("@s", "@a", "@r", "@p"));
            names.addAll(getOnlinePlayerNames(FurnitureCore.getInstance()));
            return names;
        } else if (args.length == 2) {
            return List.of("true", "false");
        }
        return null;
    }
}
