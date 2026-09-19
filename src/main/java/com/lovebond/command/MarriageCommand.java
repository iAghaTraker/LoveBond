package com.lovebond.command;

import com.lovebond.LoveBond;
import com.lovebond.config.Messages;
import com.lovebond.data.DataStorage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MarriageCommand implements TabExecutor {

    private final LoveBond plugin;
    private final Messages messages;

    public MarriageCommand(LoveBond plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.format("only-players"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(messages.format("usage-propose"));
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "accept" -> plugin.getMarriageManager().accept(player);
            case "deny" -> plugin.getMarriageManager().deny(player);
            case "divorce" -> handleDivorce(player, args);
            case "sethome" -> plugin.getMarriageManager().setHome(player);
            case "home" -> plugin.getMarriageManager().teleportHome(player);
            case "hug" -> plugin.getMarriageManager().hugKiss(player, false);
            case "kiss" -> plugin.getMarriageManager().hugKiss(player, true);
            case "stats" -> player.sendMessage(plugin.getMarriageManager().stats(player));
            case "list" -> handleList(player);
            case "reload" -> handleReload(player);
            default -> handlePropose(player, args);
        }
        return true;
    }

    // ------------------------------------------------------------------

    private void handlePropose(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(messages.format("usage-propose"));
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage(messages.format("player-not-found", "player", args[1]));
            return;
        }
        plugin.getMarriageManager().propose(player, target);
    }

    private void handleDivorce(Player player, String[] args) {
        if (args.length >= 2 && player.hasPermission("lovebond.admin")) {
            Player other = Bukkit.getPlayerExact(args[1]);
            Player partner = plugin.getMarriageManager().getPartner(player);
            if (other == null) {
                player.sendMessage(messages.format("player-not-found", "player", args[1]));
                return;
            }
            if (!eitherMatches(player, other)) {
                player.sendMessage(messages.format("not-married"));
                return;
            }
            plugin.getMarriageManager().forceDivorce(player, other);
            return;
        }
        plugin.getMarriageManager().divorce(player);
    }

    private boolean eitherMatches(Player a, Player b) {
        Map<UUID, UUID> couples = plugin.getStorage().getCouples();
        UUID partnerA = couples.get(a.getUniqueId());
        UUID partnerB = couples.get(b.getUniqueId());
        return (partnerA != null && partnerA.equals(b.getUniqueId()))
                || (partnerB != null && partnerB.equals(a.getUniqueId()));
    }

    private void handleList(Player player) {
        if (!player.hasPermission("lovebond.admin")) {
            player.sendMessage(messages.format("no-permission"));
            return;
        }
        Map<UUID, UUID> couples = plugin.getStorage().getCouples();
        if (couples.isEmpty()) {
            player.sendMessage(messages.format("list-header"));
            player.sendMessage(messages.format("list-empty"));
            return;
        }
        player.sendMessage(messages.format("list-header"));
        var seen = new java.util.HashSet<UUID>();
        for (Map.Entry<UUID, UUID> entry : couples.entrySet()) {
            UUID a = entry.getKey();
            UUID b = entry.getValue();
            if (seen.contains(a) || seen.contains(b)) continue;
            seen.add(a);
            seen.add(b);
            player.sendMessage(messages.format("list-row",
                    "player1", Bukkit.getOfflinePlayer(a).getName(),
                    "player2", Bukkit.getOfflinePlayer(b).getName()));
        }
    }

    private void handleReload(Player player) {
        if (!player.hasPermission("lovebond.admin")) {
            player.sendMessage(messages.format("no-permission"));
            return;
        }
        plugin.reloadConfig();
        messages.reload();
        player.sendMessage(messages.format("config-reloaded"));
    }

    // ------------------------------------------------------------------
    // Tab completion
    // ------------------------------------------------------------------

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(Arrays.asList("accept", "deny", "divorce", "sethome", "home",
                    "hug", "kiss", "stats", "list", "reload"));
            return filter(completions, args[0]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("divorce"))) {
            return filter(onlinePlayers(), args[1]);
        }
        if (args.length == 2) {
            return filter(onlinePlayers(), args[1]);
        }
        return completions;
    }

    private List<String> onlinePlayers() {
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }
        return names;
    }

    private List<String> filter(List<String> source, String prefix) {
        List<String> result = new ArrayList<>();
        for (String s : source) {
            if (s.toLowerCase().startsWith(prefix.toLowerCase())) {
                result.add(s);
            }
        }
        return result;
    }
}