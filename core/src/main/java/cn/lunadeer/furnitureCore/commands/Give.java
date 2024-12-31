package cn.lunadeer.furnitureCore.commands;

import cn.lunadeer.furnitureCore.FurnitureCore;
import cn.lunadeer.furnitureCore.Language;
import cn.lunadeer.furnitureCore.utils.Notification;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationPart;
import cn.lunadeer.furnitureCoreApi.items.FurnitureItemStack;
import cn.lunadeer.furnitureCoreApi.managers.ModelManager;
import cn.lunadeer.furnitureCoreApi.models.FurnitureModel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static cn.lunadeer.furnitureCore.utils.Common.getOnlinePlayerNames;

/**
 * /furniture-give <target> <model_name> [amount]
 * - `target`：要被给予家具物品的玩家。
 * - `@s`：自己；
 * - `@a`：所有玩家；
 * - `@r`：随机玩家；
 * - `@p`：最近的玩家。
 * - 玩家名称。
 * - `model_name`：模型名称。
 * - 名称格式：`<namespace>:<category>/<model>`。
 * - `namespace`：命名空间（配置文件中设置，默认为 `furniture_core`）；
 * - `category`：模型分类（默认为 `furniture`）；
 * - `model`：模型名称。
 * - 例如：`furniture_core:furniture/chair`。
 * - `amount`：数量（默认为 1）。
 */
public class Give implements TabExecutor {

    public static class GiveCommandText extends ConfigurationPart {
        public String modelNotFound = "Model %s not found.";
        public String giveSuccess = "Successfully give %d %s to %s.";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        try {
            if (args.length < 2) {
                return false;
            }

            String targetStr = args[0];
            String modelStr = args[1];
            int amount = args.length > 2 ? Integer.parseInt(args[2]) : 1;

            List<Player> targets = CommandParser.getPlayersWithArg(sender, targetStr);
            FurnitureModel model = ModelManager.getInstance().getModel(modelStr);
            if (model == null) {
                Notification.error(sender, Language.giveCommandText.modelNotFound, modelStr);
                return true;
            }
            FurnitureItemStack item = new FurnitureItemStack(model, amount);
            for (Player target : targets) {
                target.getInventory().addItem(item);
                Notification.info(sender, Language.giveCommandText.giveSuccess.formatted(amount, model.getDisplayName(), target.getName()));
            }
            return true;
        } catch (Exception e) {
            Notification.error(sender, e.getMessage());
            return true;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
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
