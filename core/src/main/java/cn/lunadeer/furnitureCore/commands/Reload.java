package cn.lunadeer.furnitureCore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * /furniture-reload [type]
 * - `type`：重载类型。
 *   - `all`：全部重载（默认）；
 *   - `config`：重载配置文件；
 *   - `resource`：重新生成资源文件；
 */
public class Reload implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("all", "config", "resource");
        }
        return null;
    }
}
