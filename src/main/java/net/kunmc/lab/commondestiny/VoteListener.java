package net.kunmc.lab.commondestiny;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class VoteListener implements Listener {
    private final VoteSystem voteSystem;
    private final Set<Player> voteCanceled = new HashSet<>();

    public VoteListener(VoteSystem voteSystem) {
        this.voteSystem = voteSystem;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (voteCanceled.contains(player)) {
            player.sendMessage(ChatColor.RED + "投票期間中にログアウトしたため投票がキャンセルされました 再度投票してください");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (voteSystem.isVoted(player)) {
            voteCanceled.add(player);
            voteSystem.unvote(player);
        }
        Set<Player> votes = voteSystem.votedPlayers(player);
        for (Player voted : votes) {
            voted.sendMessage(ChatColor.RED + "投票先のプレイヤーがログアウトしました 再度投票してください");
            voteSystem.unvote(voted);
        }
    }
}
