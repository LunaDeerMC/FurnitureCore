package cn.lunadeer.furnitureCore.commands;

import cn.lunadeer.furnitureCore.FurnitureCore;
import cn.lunadeer.furnitureCore.Language;
import cn.lunadeer.furnitureCore.utils.configuration.ConfigurationPart;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.bukkit.Bukkit.getOnlinePlayers;

public class CommandParser {

    public static class CommandParserText extends ConfigurationPart {
        public String mustBePlayerWithAtS = "You must be a player to use this command with @s.";
        public String mustBrPlayerWithAtP = "You must be a player to use this command with @p.";
        public String playerNotFound = "Player %s not found.";
    }

    /**
     * Get players with argument.
     * <p>
     * `@s`: Self;
     * <p>
     * `@a`: All players;
     * <p>
     * `@r`: Random player;
     * <p>
     * `@p`: Nearest player.
     * <p>
     * Player name.
     *
     * @param sender Command sender.
     * @param arg    Argument.
     * @return Players.
     */
    public static List<Player> getPlayersWithArg(CommandSender sender, String arg) throws IllegalArgumentException {
        List<Player> targets = new ArrayList<>(getOnlinePlayers());
        switch (arg) {
            case "@s" -> {
                if (sender instanceof Player player) {
                    targets.clear();
                    targets.add(player);
                } else {
                    throw new IllegalArgumentException(Language.commandParserText.mustBePlayerWithAtS);
                }
            }
            case "@a" -> {
                // Do nothing
            }
            case "@r" -> {
                Random random = new Random();
                Player player = targets.get(random.nextInt(targets.size()));
                targets.clear();
                targets.add(player);
            }
            case "@p" -> {
                if (sender instanceof Player player) {
                    targets.clear();
                    targets.add(player);
                } else {
                    throw new IllegalArgumentException(Language.commandParserText.mustBrPlayerWithAtP);
                }
            }
            default -> {
                targets.clear();
                Player player = FurnitureCore.getInstance().getServer().getPlayer(arg);
                if (player != null) {
                    targets.add(player);
                } else {
                    throw new IllegalArgumentException(Language.commandParserText.playerNotFound.formatted(arg));
                }
            }
        }
        return targets;
    }

}
