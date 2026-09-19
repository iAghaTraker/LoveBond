package com.lovebond.command;

import com.lovebond.LoveBond;
import com.lovebond.config.Messages;
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

        String name = command.getName().toLowerCase();
        if (name.equals("sex")) {
            plugin.getMarriageManager().sex(player);
            return true;
        }
        if (name.equals("lovepoints")) {
            handleLovePoints(player, args);
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
            case "tp", "teleport" -> plugin.getMarriageManager().teleportToPartner(player);
            case "gift" -> plugin.getMarriageManager().gift(player);
            case "chat" -> plugin.getMarriageManager().toggleCoupleChat(player);
            case "lovepoints", "points", "balance" -> sendBalance(player, null);
            case "send" -> handleSendPoints(player, args);
            case "top" -> handleTop(player);
            case "add", "take", "set" -> handleAdminPoints(player, sub, args);
            case "stats" -> player.sendMessage(plugin.getMarriageManager().stats(player));
            case "list" -> handleList(player);
            case "reload" -> handleReload(player);
            default -> handlePropose(player, args);
        }
        return true;
    }

    // ------------------------------------------------------------------
    // /lovepoints
    // ------------------------------------------------------------------

    private void handleLovePoints(Player player, String[] args) {
        if (args.length == 0) {
            sendBalance(player, null);
            return;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "send" -> handleSendPoints(player, args);
            case "top" -> handleTop(player);
            case "add", "take", "set" -> handleAdminPoints(player, sub, args);
            case "check", "balance", "bal" -> sendBalance(player, null);
            default -> player.sendMessage(messages.format("lovepoints-usage"));
        }
    }

    private void sendBalance(Player player, Player target) {
        Player who = target != null ? target : player;
        int amount = plugin.getMarriageManager().points(who);
        String unit = plugin.getConfig().getString("lovepoints.points-name", "Love Points");
        player.sendMessage(messages.format("lovepoints-check",
                "amount", String.valueOf(amount), "unit", unit));
    }

    private void handleSendPoints(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(messages.format("lovepoints-usage"));
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage(messages.format("player-not-found", "player", args[1]));
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException ex) {
            player.sendMessage(messages.format("lovepoints-invalid-amount"));
            return;
        }
        String unit = plugin.getConfig().getString("lovepoints.points-name", "Love Points");
        if (amount <= 0 || !plugin.getMarriageManager().sendPoints(player, target, amount)) {
            player.sendMessage(messages.format("lovepoints-insufficient", "unit", unit));
            return;
        }
        player.sendMessage(messages.format("lovepoints-sent",
                "player", target.getName(), "amount", String.valueOf(amount), "unit", unit));
        target.sendMessage(messages.format("lovepoints-received",
                "player", player.getName(), "amount", String.valueOf(amount), "unit", unit));
    }

    private void handleTop(Player player) {
        if (!player.hasPermission("lovebond.admin")) {
            player.sendMessage(messages.format("no-permission"));
            return;
        }
        List<Map.Entry<UUID, Integer>> top = plugin.getMarriageManager().topPoints(5);
        player.sendMessage(messages.format("lovepoints-top-header"));
        if (top.isEmpty()) {
            player.sendMessage(messages.format("lovepoints-top-empty"));
            return;
        }
        int rank = 1;
        for (Map.Entry<UUID, Integer> entry : top) {
            String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
            player.sendMessage(messages.format("lovepoints-top-row",
                    "rank", String.valueOf(rank++),
                    "player", name,
                    "amount", String.valueOf(entry.getValue())));
        }
    }

    private void handleAdminPoints(Player player, String mode, String[] args) {
        if (!player.hasPermission("lovebond.admin")) {
            player.sendMessage(messages.format("no-permission"));
            return;
        }
        if (args.length < 3) {
            player.sendMessage(messages.format("lovepoints-usage"));
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage(messages.format("player-not-found", "player", args[1]));
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException ex) {
            player.sendMessage(messages.format("lovepoints-invalid-amount"));
            return;
        }
        if (!plugin.getMarriageManager().givePoints(target, amount, mode)) {
            player.sendMessage(messages.format("lovepoints-invalid-amount"));
            return;
        }
        String unit = plugin.getConfig().getString("lovepoints.points-name", "Love Points");
        String key = switch (mode) {
            case "take" -> "lovepoints-taken";
            case "set" -> "lovepoints-set";
            default -> "lovepoints-given";
        };
        player.sendMessage(messages.format(key,
                "player", target.getName(), "amount", String.valueOf(amount), "unit", unit));
        player.sendMessage(messages.format("lovepoints-check",
                "amount", String.valueOf(plugin.getMarriageManager().points(target)), "unit", unit));
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
        player.sendMessage(messages.format("list-header"));
        if (couples.isEmpty()) {
            player.sendMessage(messages.format("list-empty"));
            return;
        }
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
        String name = command.getName().toLowerCase();

        if (name.equals("sex")) {
            return List.of();
        }
        if (name.equals("lovepoints")) {
            if (args.length == 1) {
                List<String> subs = new ArrayList<>(Arrays.asList("check", "send", "top", "add", "take", "set"));
                if (!sender.hasPermission("lovebond.admin")) {
                    subs.removeAll(List.of("add", "take", "set", "top"));
                }
                return filter(subs, args[0]);
            }
            if (args.length == 2 && (args[0].equalsIgnoreCase("send")
                    || args[0].equalsIgnoreCase("add")
                    || args[0].equalsIgnoreCase("take")
                    || args[0].equalsIgnoreCase("set"))) {
                return filter(onlinePlayers(), args[1]);
            }
            return List.of();
        }

        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(Arrays.asList("accept", "deny", "divorce", "sethome", "home",
                    "hug", "kiss", "tp", "gift", "chat", "lovepoints", "send", "top",
                    "stats", "list", "reload"));
            if (sender.hasPermission("lovebond.admin")) {
                completions.addAll(List.of("add", "take", "set"));
            }
            return filter(completions, args[0]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("send")
                || args[0].equalsIgnoreCase("divorce")
                || args[0].equalsIgnoreCase("add")
                || args[0].equalsIgnoreCase("take")
                || args[0].equalsIgnoreCase("set"))) {
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