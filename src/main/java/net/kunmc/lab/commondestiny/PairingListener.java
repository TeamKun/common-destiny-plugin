package net.kunmc.lab.commondestiny;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PairingListener implements Listener {
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
        PairingManager manager = CommonDestinyPlugin.getInstance().getPairingManager();
        Player player = event.getEntity();
        if (!manager.hasPartner(player)) {
            return;
        }
        Player partner = manager.getPartner(player);
        if (!partner.isDead()) {
            partner.remove();
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
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
}
