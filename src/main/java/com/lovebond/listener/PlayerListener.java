package com.lovebond.listener;

import com.lovebond.LoveBond;
import com.lovebond.gui.ProposalGUI;
import com.lovebond.manager.MarriageManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    private final LoveBond plugin;

    public PlayerListener(LoveBond plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().contains("Marriage Proposal")) return;

        event.setCancelled(true);
        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= event.getView().getTopInventory().getSize()) return;

        ItemStack current = event.getView().getTopInventory().getItem(rawSlot);
        if (ProposalGUI.isSlot(current, "Accept")) {
            plugin.getMarriageManager().accept(player);
            player.closeInventory();
        } else if (ProposalGUI.isSlot(current, "Deny")) {
            plugin.getMarriageManager().deny(player);
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // intentionally empty - proposals also expire on their own
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        MarriageManager manager = plugin.getMarriageManager();
        if (!manager.isMarried(event.getPlayer())) return;

        Player partner = manager.getPartner(event.getPlayer());
        if (partner != null && partner.isOnline()) {
            partner.sendMessage(plugin.getMessages().format("partner-joined", "player", event.getPlayer().getName()));
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        MarriageManager manager = plugin.getMarriageManager();
        if (!manager.isMarried(player)) return;

        Player partner = manager.getPartner(player);
        if (partner != null && partner.isOnline()) {
            partner.sendMessage(plugin.getMessages().format("partner-died", "player", player.getName()));
        }
    }

    @EventHandler
    public void onAsyncChat(AsyncPlayerChatEvent event) {
        MarriageManager manager = plugin.getMarriageManager();
        if (!manager.hasCoupleChat(event.getPlayer().getUniqueId())) return;

        Player partner = manager.getPartner(event.getPlayer());
        if (partner == null || !partner.isOnline()) return;

        event.setCancelled(true);
        String format = plugin.getMessages().getRaw().getString(
                "couple-chat-format", "&d[{player}] &7▸ &f{message}");
        format = ChatColor.translateAlternateColorCodes('&', format)
                .replace("{player}", event.getPlayer().getName())
                .replace("{message}", event.getMessage());
        event.getPlayer().sendMessage(format);
        partner.sendMessage(format);
    }
}