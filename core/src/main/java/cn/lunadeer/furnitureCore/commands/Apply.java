package cn.lunadeer.furnitureCore.commands;

import cn.lunadeer.furnitureCore.FurnitureCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
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
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return false;
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
