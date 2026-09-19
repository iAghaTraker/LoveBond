package com.lovebond.manager;

import com.lovebond.LoveBond;
import com.lovebond.config.Messages;
import com.lovebond.data.DataStorage;
import com.lovebond.gui.ProposalGUI;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MarriageManager {

    private final LoveBond plugin;
    private final DataStorage storage;
    private final Messages messages;

    private final Map<UUID, UUID> proposals = new HashMap<>();
    private final Map<UUID, Long> proposalCooldowns = new HashMap<>();
    private final Map<UUID, Long> divorceCooldowns = new HashMap<>();
    private final Map<UUID, Integer> expirationTasks = new HashMap<>();

    public MarriageManager(LoveBond plugin) {
        this.plugin = plugin;
        this.storage = plugin.getStorage();
        this.messages = plugin.getMessages();
    }

    // ------------------------------------------------------------------
    // Lookup helpers
    // ------------------------------------------------------------------

    public boolean isMarried(Player player) {
        return storage.getCouples().containsKey(player.getUniqueId());
    }

    public UUID getPartnerUuid(UUID uuid) {
        return storage.getCouples().get(uuid);
    }

    public Player getPartner(Player player) {
        UUID partner = getPartnerUuid(player.getUniqueId());
        return partner == null ? null : Bukkit.getPlayer(partner);
    }

    public String getPartnerName(Player player) {
        UUID partner = getPartnerUuid(player.getUniqueId());
        return partner == null ? null : Bukkit.getOfflinePlayer(partner).getName();
    }

    // ------------------------------------------------------------------
    // Proposals
    // ------------------------------------------------------------------

    public boolean propose(Player proposer, Player target) {
        if (proposer.equals(target)) {
            proposer.sendMessage(messages.format("proposal-self"));
            return false;
        }
        if (isMarried(proposer)) {
            proposer.sendMessage(messages.format("proposal-already-married",
                    "partner", getPartnerName(proposer)));
            return false;
        }
        if (isMarried(target)) {
            proposer.sendMessage(messages.format("proposal-target-married", "player", target.getName()));
            return false;
        }
        if (proposals.containsValue(proposer.getUniqueId())) {
            proposer.sendMessage(messages.format("proposal-already-sent"));
            return false;
        }
        if (isOnProposalCooldown(proposer)) return false;

        proposals.put(target.getUniqueId(), proposer.getUniqueId());
        proposalCooldowns.put(proposer.getUniqueId(), System.currentTimeMillis());

        proposer.sendMessage(messages.format("proposal-sent", "player", target.getName()));
        target.sendMessage(messages.format("proposal-sent-gui", "player", proposer.getName()));
        ProposalGUI.open(plugin, proposer, target);
        scheduleExpiration(proposer.getUniqueId(), target.getUniqueId());
        return true;
    }

    public void accept(Player target) {
        UUID proposerId = proposals.remove(target.getUniqueId());
        if (proposerId == null) {
            target.sendMessage(messages.format("proposal-no-pending"));
            return;
        }
        Player proposer = Bukkit.getPlayer(proposerId);
        if (proposer == null) {
            target.sendMessage(messages.format("proposal-target-offline",
                    "player", Bukkit.getOfflinePlayer(proposerId).getName()));
            return;
        }
        marry(proposer, target);
    }

    public void deny(Player target) {
        UUID proposerId = proposals.remove(target.getUniqueId());
        if (proposerId == null) {
            target.sendMessage(messages.format("proposal-no-pending"));
            return;
        }
        Player proposer = Bukkit.getPlayer(proposerId);
        String name = proposer == null
                ? Bukkit.getOfflinePlayer(proposerId).getName()
                : proposer.getName();
        if (proposer != null) {
            proposer.sendMessage(messages.format("proposal-denied-target", "player", target.getName()));
        }
        target.sendMessage(messages.format("proposal-denied", "player", name));
    }

    private boolean isOnProposalCooldown(Player proposer) {
        long cooldown = plugin.getConfig().getLong("settings.proposal-cooldown-seconds", 30) * 1000L;
        long last = proposalCooldowns.getOrDefault(proposer.getUniqueId(), 0L);
        long wait = (last + cooldown - System.currentTimeMillis()) / 1000L;
        if (wait > 0) {
            proposer.sendMessage(messages.format("proposal-cooldown", "seconds", String.valueOf(wait)));
            return true;
        }
        return false;
    }

    private void scheduleExpiration(UUID proposerId, UUID targetId) {
        int seconds = plugin.getConfig().getInt("settings.proposal-expire-seconds", 60);
        int task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (proposals.remove(targetId, proposerId)) {
                Player proposer = Bukkit.getPlayer(proposerId);
                if (proposer != null) {
                    proposer.sendMessage(messages.format("proposal-expired",
                            "player", Bukkit.getOfflinePlayer(targetId).getName()));
                }
            }
        }, seconds * 20L).getTaskId();
        expirationTasks.put(targetId, task);
    }

    // ------------------------------------------------------------------
    // Marriage / Divorce
    // ------------------------------------------------------------------

    public void marry(Player a, Player b) {
        storage.getCouples().put(a.getUniqueId(), b.getUniqueId());
        storage.getCouples().put(b.getUniqueId(), a.getUniqueId());
        storage.save();

        if (plugin.getConfig().getBoolean("settings.announce-marriage-globally", true)) {
            Bukkit.broadcastMessage(messages.format("marriage-announce",
                    "player1", a.getName(), "player2", b.getName()));
        }
        a.sendMessage(messages.format("proposal-accepted", "player", b.getName()));
        b.sendMessage(messages.format("proposal-accepted-target", "player", a.getName()));

        celebrate(a, b);
    }

    public boolean divorce(Player player) {
        UUID partnerId = getPartnerUuid(player.getUniqueId());
        if (partnerId == null) {
            player.sendMessage(messages.format("divorce-not-married"));
            return false;
        }
        long cooldown = plugin.getConfig().getLong("settings.divorce-cooldown-seconds", 300) * 1000L;
        long last = divorceCooldowns.getOrDefault(player.getUniqueId(), 0L);
        long wait = (last + cooldown - System.currentTimeMillis()) / 1000L;
        if (wait > 0) {
            player.sendMessage(messages.format("divorce-cooldown", "seconds", String.valueOf(wait)));
            return false;
        }

        Player partner = Bukkit.getPlayer(partnerId);
        String name = partner == null
                ? Bukkit.getOfflinePlayer(partnerId).getName()
                : partner.getName();

        storage.getCouples().remove(player.getUniqueId());
        storage.getCouples().remove(partnerId);
        storage.save();

        player.sendMessage(messages.format("divorce-success", "player", name));
        if (partner != null) {
            partner.sendMessage(messages.format("divorce-success-target", "player", player.getName()));
        }
        divorceCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        return true;
    }

    public void forceDivorce(Player a, Player b) {
        storage.getCouples().remove(a.getUniqueId());
        storage.getCouples().remove(b.getUniqueId());
        storage.save();
        for (Player player : new Player[]{a, b}) {
            if (player != null) {
                player.sendMessage(messages.format("divorce-admin-force",
                        "player1", a.getName(), "player2", b.getName()));
            }
        }
    }

    // ------------------------------------------------------------------
    // Shared home
    // ------------------------------------------------------------------

    public void setHome(Player player) {
        UUID partner = getPartnerUuid(player.getUniqueId());
        if (partner == null) {
            player.sendMessage(messages.format("not-married"));
            return;
        }
        UUID key = player.getUniqueId().toString().compareTo(partner.toString()) < 0
                ? player.getUniqueId()
                : partner;
        storage.getHomes().put(key, player.getLocation().clone());
        storage.save();
        player.sendMessage(messages.format("home-set"));
    }

    public Location getHome(Player player) {
        UUID partner = getPartnerUuid(player.getUniqueId());
        if (partner == null) return null;
        UUID key = player.getUniqueId().toString().compareTo(partner.toString()) < 0
                ? player.getUniqueId()
                : partner;
        return storage.getHomes().get(key);
    }

    public boolean teleportHome(Player player) {
        Location home = getHome(player);
        if (home == null) {
            player.sendMessage(messages.format("home-not-set"));
            return false;
        }
        player.teleport(home);
        player.sendMessage(messages.format("home-teleported"));

        if (plugin.getConfig().getBoolean("home.sync-teleport", true)) {
            Player partner = getPartner(player);
            if (partner != null && !partner.equals(player)) {
                partner.teleport(home);
                partner.sendMessage(messages.format("home-teleported"));
                player.sendMessage(messages.format("home-sync-teleport"));
            }
        }
        if (plugin.getConfig().getBoolean("home.teleport-particles", true)) {
            spawnHearts(home);
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Fun commands
    // ------------------------------------------------------------------

    public boolean hugKiss(Player player, boolean kiss) {
        if (!plugin.getConfig().getBoolean("settings.enable-fun-commands", true)) return false;

        Player partner = getPartner(player);
        if (partner == null) {
            player.sendMessage(messages.format("fun-not-married"));
            return false;
        }

        int maxDistance = plugin.getConfig().getInt("settings.hug-kiss-max-distance", 25);
        if (!partner.getWorld().equals(player.getWorld())
                || player.getLocation().distance(partner.getLocation()) > maxDistance) {
            double distance = partner.getWorld().equals(player.getWorld())
                    ? player.getLocation().distance(partner.getLocation())
                    : Double.POSITIVE_INFINITY;
            String dist = distance == Double.POSITIVE_INFINITY ? "?" : String.valueOf((int) distance);
            player.sendMessage(messages.format("fun-too-far", "distance", dist, "max", String.valueOf(maxDistance)));
            return false;
        }

        String sent = kiss ? "kiss-sent" : "hug-sent";
        String received = kiss ? "kiss-received" : "hug-received";
        player.sendMessage(messages.format(sent, "player", partner.getName()));
        partner.sendMessage(messages.format(received, "player", player.getName()));

        if (plugin.getConfig().getBoolean("settings.effects.marriage-fireworks", true) && kiss) {
            spawnFirework(player.getLocation());
        }
        spawnHearts(partner.getLocation());
        spawnHearts(player.getLocation());
        return true;
    }

    public String stats(Player player) {
        UUID partner = getPartnerUuid(player.getUniqueId());
        if (partner == null) return messages.get("stats-single");

        long marriedSince = System.currentTimeMillis();
        String home = getHome(player) == null ? "&cNot set" : "&aSet";
        return messages.format("stats-married",
                "partner", Bukkit.getOfflinePlayer(partner).getName(),
                "date", java.time.LocalDate.now().toString(),
                "home", home);
    }

    // ------------------------------------------------------------------
    // Effects
    // ------------------------------------------------------------------

    private void celebrate(Player a, Player b) {
        int regen = plugin.getConfig().getInt("settings.effects.marriage-regeneration-seconds", 15);
        if (regen > 0) {
            a.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, regen * 20, 0));
            b.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, regen * 20, 0));
        }
        if (plugin.getConfig().getBoolean("settings.effects.marriage-fireworks", true)) {
            spawnFirework(a.getLocation());
            spawnFirework(b.getLocation());
        }
    }

    private void spawnHearts(Location loc) {
        loc.getWorld().spawnParticle(Particle.HEART, loc.clone().add(0, 2, 0), 8, 0.5, 0.5, 0.5, 0);
    }

    private void spawnFirework(Location loc) {
        loc.getWorld().spawn(loc, Firework.class, firework -> {
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder()
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .withColor(Color.FUCHSIA)
                    .withColor(Color.RED)
                    .withFade(Color.WHITE)
                    .trail(true)
                    .flicker(true)
                    .build());
            firework.setFireworkMeta(meta);
        });
    }

    public Map<UUID, UUID> getProposals() {
        return proposals;
    }
}