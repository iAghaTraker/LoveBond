package com.lovebond.config;

import com.lovebond.LoveBond;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Messages {

    private final LoveBond plugin;
    private YamlConfiguration messages;

    public Messages(LoveBond plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Resolve a message key, translate color codes and swap {prefix}.
     */
    public String get(String key) {
        String raw = messages.getString(key, key);
        String prefix = messages.getString("prefix", "&d&l[&5LoveBond&d&l] &r");
        raw = raw.replace("{prefix}", prefix);
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    /**
     * Resolve a message and replace {placeholder} values.
     * Usage: format("proposal-sent", "player", target.getName())
     */
    public String format(String key, String... replacements) {
        String message = get(key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            String value = replacements[i + 1] == null ? "?" : replacements[i + 1];
            message = message.replace("{" + replacements[i] + "}", value);
        }
        return message;
    }

    public YamlConfiguration getRaw() {
        return messages;
    }
}