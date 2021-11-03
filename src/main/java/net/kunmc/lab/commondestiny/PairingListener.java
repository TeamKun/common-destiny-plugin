package net.kunmc.lab.commondestiny;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import net.kunmc.lab.commondestiny.config.ConfigManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class PairingListener implements Listener {
    private final Map<Player, BukkitTask> differentWorldTimers = new HashMap<>();

    private void startDifferentWorldTimer(Player player1, Player player2) {
        ConfigManager configManager = CommonDestinyPlugin.getInstance().getConfigManager();
        long differentWorldTimer = configManager.getDifferentWorldTimer();
        BukkitTask task = Bukkit.getScheduler().runTaskLater(CommonDestinyPlugin.getInstance(), () -> {
            if (configManager.isEnabled()) {
                player1.setHealth(0);
            }
            stopDifferentWorldTimer(player1, player2);
        }, differentWorldTimer);
        differentWorldTimers.put(player1, task);
        differentWorldTimers.put(player2, task);
    }

    private void stopDifferentWorldTimer(Player player1, Player player2) {
        if (differentWorldTimers.containsKey(player1)) {
            differentWorldTimers.get(player1).cancel();
        }
        if (differentWorldTimers.containsKey(player2)) {
            differentWorldTimers.remove(player2).cancel();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PairingManager manager = CommonDestinyPlugin.getInstance().getPairingManager();
        Player player = event.getPlayer();
        Player lastPartner = manager.getLastPartner(player);
        if (lastPartner == null) {
            return;
        }
        Player rev = manager.getLastPartner(lastPartner);
        if (player.equals(rev)) {
            manager.form(player, lastPartner, false);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PairingManager manager = CommonDestinyPlugin.getInstance().getPairingManager();
        Player player = event.getPlayer();
        if (VoteSystem.isVoteStarted()) {
            VoteSystem voteSystem = VoteSystem.getVoteInstance();
            voteSystem.remove(player);
        }
        if (manager.hasPartner(player)) {
            manager.dissolve(player, true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        ConfigManager configManager = CommonDestinyPlugin.getInstance().getConfigManager();
        if (!configManager.isEnabled()) {
            return;
        }
        PairingManager manager = CommonDestinyPlugin.getInstance().getPairingManager();
        Player player = event.getEntity();
        if (!manager.hasPartner(player)) {
            return;
        }
        Player partner = manager.getPartner(player);
        if (!partner.isDead()) {
            partner.setHealth(0);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        ConfigManager configManager = CommonDestinyPlugin.getInstance().getConfigManager();
        if (!configManager.isEnabled()) {
            return;
        }
        PairingManager manager = CommonDestinyPlugin.getInstance().getPairingManager();
        Player player = event.getPlayer();
        if (!manager.hasPartner(player)) {
            return;
        }
        Player partner = manager.getPartner(player);
        if (!partner.isDead()) {
            event.setRespawnLocation(partner.getLocation());
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        ConfigManager configManager = CommonDestinyPlugin.getInstance().getConfigManager();
        if (!configManager.isEnabled()) {
            return;
        }
        PairingManager manager = CommonDestinyPlugin.getInstance().getPairingManager();
        Player player = event.getPlayer();
        if (!manager.hasPartner(player)) {
            return;
        }
        Player partner = manager.getPartner(player);
        if (!player.getWorld().equals(partner.getWorld())) {
            startDifferentWorldTimer(player, partner);
        } else {
            stopDifferentWorldTimer(player, partner);
        }
    }

    @EventHandler
    public void onTick(ServerTickStartEvent event) {
        ConfigManager configManager = CommonDestinyPlugin.getInstance().getConfigManager();
        if (!configManager.isEnabled()) {
            return;
        }
        PairingManager manager = CommonDestinyPlugin.getInstance().getPairingManager();
        double range = configManager.getRange();
        for (PairResult pair : manager.pairs()) {
            if (pair.player1.isDead() || pair.player2.isDead()) {
                continue;
            }
            if (!pair.player1.getWorld().equals(pair.player2.getWorld())) {
                continue;
            }
            Location location1 = pair.player1.getEyeLocation().clone().add(pair.player1.getLocation()).multiply(0.5);
            Location location2 = pair.player2.getEyeLocation().clone().add(pair.player2.getLocation()).multiply(0.5);
            double distance = location1.distance(location2);
            if (distance > range) {
                pair.player1.setHealth(0);
                continue;
            }
            Color color;
            if (distance < range * 0.6) {
                color = Color.WHITE;
            } else if (distance < range * 0.8) {
                color = Color.YELLOW;
            } else {
                color = Color.RED;
            }
            drawLine(location1, location2, color);
        }
    }

    private void drawLine(Location loc1, Location loc2, Color color) {
        double distance = loc1.distance(loc2);
        loc1 = loc1.clone();
        int numParticle = Math.max(3, (int)(distance * 2));
        Vector vector = loc2.clone().subtract(loc1).toVector().multiply(1.0 / (numParticle - 1));
        World world = loc1.getWorld();
        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1);
        for (int i = 0; i < numParticle; i++) {
            world.spawnParticle(Particle.REDSTONE, loc1, 0, dustOptions);
            loc1.add(vector);
        }
    }
}
