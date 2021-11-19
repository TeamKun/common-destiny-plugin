package net.kunmc.lab.commondestiny;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VoteListener implements Listener {
    private final VoteSystem voteSystem;
    private final Set<UUID> voteCanceled = new HashSet<>();

    public VoteListener(VoteSystem voteSystem) {
        this.voteSystem = voteSystem;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (voteCanceled.contains(player.getUniqueId())) {
            voteCanceled.remove(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "投票期間中にログアウトしたため投票がキャンセルされました 再度投票してください");
        }
        PairingManager manager = CommonDestinyPlugin.getPairingManager();
        if (!manager.hasPartner(player)) {
            player.playerListName(Component.text("× ", NamedTextColor.GRAY).append(player.displayName().color(NamedTextColor.WHITE)));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (voteSystem.isVoted(player)) {
            voteCanceled.add(player.getUniqueId());
            voteSystem.unvote(player);
        }
        Set<Player> votes = voteSystem.votedPlayers(player);
        for (Player voted : votes) {
            voted.sendMessage(ChatColor.RED + "投票先のプレイヤーがログアウトしました 再度投票してください");
            voteSystem.unvote(voted);
        }
    }
}
