package com.lovebond.data;

import com.lovebond.LoveBond;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataStorage {

    private final LoveBond plugin;
    private final File dataFile;
    private final Map<UUID, UUID> couples = new HashMap<>();
    private final Map<UUID, Location> homes = new HashMap<>();
    private final Map<UUID, Integer> points = new HashMap<>();

    public DataStorage(LoveBond plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        load();
    }

    public void load() {
        if (!dataFile.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);
        var section = yaml.getConfigurationSection("couples");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                try {
                    UUID one = UUID.fromString(yaml.getString("couples." + key + ".partner1"));
                    UUID two = UUID.fromString(yaml.getString("couples." + key + ".partner2"));
                    couples.put(one, two);
                    couples.put(two, one);

                    String path = "couples." + key + ".home";
                    if (yaml.getString(path + ".world") != null) {
                        Location home = deserialize(yaml, path);
                        if (home != null) homes.put(one, home);
                    }
                } catch (Exception ex) {
                    plugin.getLogger().warning("Skipped invalid couple data: " + key);
                }
            }
        }

        var pointsSection = yaml.getConfigurationSection("points");
        if (pointsSection != null) {
            for (String key : pointsSection.getKeys(false)) {
                try {
                    points.put(UUID.fromString(key), pointsSection.getInt(key));
                } catch (Exception ignored) {
                }
            }
        }
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        int index = 0;
        for (Map.Entry<UUID, UUID> entry : couples.entrySet()) {
            UUID a = entry.getKey();
            UUID b = entry.getValue();
            if (a.toString().compareTo(b.toString()) > 0) continue;

            String base = "couples.pair-" + (index++);
            yaml.set(base + ".partner1", a.toString());
            yaml.set(base + ".partner2", b.toString());

            Location home = homes.get(a);
            if (home != null) {
                yaml.set(base + ".home.world", home.getWorld() != null ? home.getWorld().getName() : "world");
                yaml.set(base + ".home.x", home.getX());
                yaml.set(base + ".home.y", home.getY());
                yaml.set(base + ".home.z", home.getZ());
                yaml.set(base + ".home.yaw", (double) home.getYaw());
                yaml.set(base + ".home.pitch", (double) home.getPitch());
            }
        }
        for (Map.Entry<UUID, Integer> entry : points.entrySet()) {
            yaml.set("points." + entry.getKey(), entry.getValue());
        }
        try {
            yaml.save(dataFile);
        } catch (IOException ex) {
            plugin.getLogger().severe("Could not save data.yml: " + ex.getMessage());
        }
    }

    private Location deserialize(YamlConfiguration yaml, String path) {
        World world = Bukkit.getWorld(yaml.getString(path + ".world", "world"));
        if (world == null) return null;
        return new Location(
                world,
                yaml.getDouble(path + ".x"),
                yaml.getDouble(path + ".y"),
                yaml.getDouble(path + ".z"),
                (float) yaml.getDouble(path + ".yaw", 0),
                (float) yaml.getDouble(path + ".pitch", 0));
    }

    public Map<UUID, UUID> getCouples() {
        return couples;
    }

    public Map<UUID, Location> getHomes() {
        return homes;
    }

    public int getCoupleCount() {
        return couples.size() / 2;
    }

    // ------------------------------------------------------------------
    // Love points
    // ------------------------------------------------------------------

    public int getPoints(UUID uuid) {
        return points.getOrDefault(uuid, 0);
    }

    public void setPoints(UUID uuid, int value) {
        points.put(uuid, Math.max(0, value));
    }

    public void addPoints(UUID uuid, int amount) {
        setPoints(uuid, getPoints(uuid) + amount);
    }

    public int takePoints(UUID uuid, int amount) {
        int current = getPoints(uuid);
        int taken = Math.min(current, amount);
        setPoints(uuid, current - taken);
        return taken;
    }

    public Map<UUID, Integer> getPointsMap() {
        return points;
    }
}