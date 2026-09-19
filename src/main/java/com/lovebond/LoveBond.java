package com.lovebond;

import com.lovebond.command.MarriageCommand;
import com.lovebond.config.Messages;
import com.lovebond.data.DataStorage;
import com.lovebond.listener.PlayerListener;
import com.lovebond.manager.MarriageManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class LoveBond extends JavaPlugin {

    private static LoveBond instance;

    private DataStorage storage;
    private Messages messages;
    private MarriageManager marriageManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        messages = new Messages(this);
        storage = new DataStorage(this);
        marriageManager = new MarriageManager(this);

        MarriageCommand command = new MarriageCommand(this);
        getCommand("marry").setExecutor(command);
        getCommand("marry").setTabCompleter(command);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        getLogger().info("LoveBond v" + getDescription().getVersion()
                + " enabled! " + storage.getCoupleCount() + " couple(s) loaded.");
    }

    @Override
    public void onDisable() {
        storage.save();
        getLogger().info("LoveBond disabled.");
    }

    public static LoveBond getInstance() {
        return instance;
    }

    public DataStorage getStorage() {
        return storage;
    }

    public Messages getMessages() {
        return messages;
    }

    public MarriageManager getMarriageManager() {
        return marriageManager;
    }
}