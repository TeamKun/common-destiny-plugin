package net.kunmc.lab.commondestiny;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

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
}
