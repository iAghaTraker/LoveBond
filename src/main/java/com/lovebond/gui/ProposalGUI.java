package com.lovebond.gui;

import com.lovebond.LoveBond;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class ProposalGUI {

    private ProposalGUI() {
    }

    public static void open(LoveBond plugin, Player proposer, Player target) {
        int size = plugin.getConfig().getInt("settings.gui.size", 27);
        String title = translate(plugin.getConfig().getString("settings.gui.title", "&d&lMarriage Proposal"));
        Inventory inv = Bukkit.createInventory(null, size, title);

        String fillerKey = plugin.getConfig().getString("settings.gui.filler-item", "GRAY_STAINED_GLASS_PANE");
        Material fillerMaterial = Material.matchMaterial(fillerKey);
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack filler = new ItemStack(
                    fillerMaterial != null ? fillerMaterial : Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = filler.getItemMeta();
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
            inv.setItem(i, filler);
        }

        ItemStack accept = namedItem(
                plugin.getConfig().getString("settings.gui.accept-item", "LIME_DYE"),
                "&a&lAccept",
                List.of("&7Marry &e" + proposer.getName()));
        ItemStack deny = namedItem(
                plugin.getConfig().getString("settings.gui.deny-item", "RED_DYE"),
                "&c&lDeny",
                List.of("&7Decline " + proposer.getName() + "'s proposal"));
        ItemStack info = namedItem(
                plugin.getConfig().getString("settings.gui.info-item", "DIAMOND"),
                "&d&l" + proposer.getName() + " proposes to you!",
                List.of(
                        "&7Accept or deny this proposal.",
                        "&7It expires in &e" + plugin.getConfig().getInt("settings.proposal-expire-seconds", 60) + "s&7."));

        inv.setItem(11, accept);
        inv.setItem(13, info);
        inv.setItem(15, deny);

        target.openInventory(inv);
    }

    public static boolean isSlot(ItemStack item, String displayText) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }
        return item.getItemMeta().getDisplayName().contains(displayText);
    }

    private static ItemStack namedItem(String materialName, String display, java.util.List<String> lore) {
        Material material = Material.matchMaterial(materialName);
        if (material == null) material = Material.PAPER;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(translate(display));
        meta.setLore(lore.stream().map(ProposalGUI::translate).toList());
        item.setItemMeta(meta);
        return item;
    }

    private static String translate(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}