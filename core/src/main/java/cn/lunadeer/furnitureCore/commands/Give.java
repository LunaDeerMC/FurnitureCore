package cn.lunadeer.furnitureCore.commands;

import cn.lunadeer.furnitureCore.FurnitureCore;
import cn.lunadeer.furnitureCoreApi.managers.ModelManager;
import cn.lunadeer.furnitureCoreApi.models.FurnitureModel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static cn.lunadeer.furnitureCore.utils.Common.getOnlinePlayerNames;

/**
 * /furniture-give <target> <model_name> [amount]
 * - `target`：要被给予家具物品的玩家。
 *   - `@s`：自己；
 *   - `@a`：所有玩家；
 *   - `@r`：随机玩家；
 *   - `@p`：最近的玩家。
 *   - 玩家名称。
 * - `model_name`：模型名称。
 *   - 名称格式：`<namespace>:<category>/<model>`。
 *     - `namespace`：命名空间（配置文件中设置，默认为 `furniture_core`）；
 *     - `category`：模型分类（默认为 `furniture`）；
 *     - `model`：模型名称。
 *   - 例如：`furniture_core:furniture/chair`。
 * - `amount`：数量（默认为 1）。
 */
public class Give implements TabExecutor {
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
            return ModelManager.getInstance().getModels().stream().map(FurnitureModel::getCallableNameWithNamespace).toList();
        } else if (args.length == 3) {
            return List.of("1", "5", "16", "64");
        }
        return null;
    }
}
