package net.kunmc.lab.commondestiny;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.*;

public class VoteSystem {
    private static VoteSystem instance;
    private static boolean started;
    private final PairingManager manager;
    private final Map<Player, Player> votes = new HashMap<>();
    private final Map<Player, Set<Player>> votesRev = new HashMap<>();
    private final VoteListener listener;

    private VoteSystem(PairingManager manager) {
        this.manager = manager;
        this.listener = new VoteListener(this);
        Bukkit.getPluginManager().registerEvents(listener, CommonDestinyPlugin.getInstance());
    }

    public static void start() {
        PairingManager manager = CommonDestinyPlugin.getPairingManager();
        instance = new VoteSystem(manager);
        started = true;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!manager.hasPartner(player)) {
                player.playerListName(Component.text("× ", NamedTextColor.GRAY).append(player.displayName().color(NamedTextColor.WHITE)));
            }
        }
    }

    public static void end() {
        HandlerList.unregisterAll(instance.listener);
        instance = null;
        started = false;
        PairingManager manager = CommonDestinyPlugin.getPairingManager();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!manager.hasPartner(player)) {
                player.playerListName(player.displayName());
            }
        }
    }

    public static VoteSystem getVoteInstance() {
        return instance;
    }

    public static boolean isVoteStarted() {
        return started;
    }

    public Set<Player> votedPlayers(Player dest) {
        return votesRev.containsKey(dest) ? Collections.unmodifiableSet(votesRev.get(dest)) : Collections.emptySet();
    }

    public List<PairResult> matchResults() {
        List<PairResult> results = new ArrayList<>();
        Set<Player> remainingPlayers = new HashSet<>(manager.remainingPlayers());
        for (Map.Entry<Player, Player> entry : votes.entrySet()) {
            Player from = entry.getKey();
            Player to = entry.getValue();
            remainingPlayers.remove(from);
            if (!from.equals(votes.get(to))) {
                results.add(new PairResult(from, to, false));
            } else if (to.getUniqueId().compareTo(from.getUniqueId()) < 0) {
                results.add(new PairResult(from, to, true));
            }
        }
        for (Player remaining : remainingPlayers) {
            results.add(new PairResult(remaining, null, false));
        }
        return results;
    }

    public void vote(Player from, Player to) {
        if (!started) {
            throw new IllegalStateException();
        }
        votes.put(from, to);
        votesRev.computeIfAbsent(to, key -> new HashSet<>()).add(from);
        from.playerListName(Component.text("✓ ", NamedTextColor.GOLD).append(from.displayName().color(NamedTextColor.WHITE)));
    }

    public void unvote(Player player) {
        Player dest = votes.remove(player);
        Set<Player> set = votesRev.get(dest);
        set.remove(player);
        if (set.isEmpty()) {
            votesRev.remove(dest);
        }
        player.playerListName(Component.text("× ", NamedTextColor.GRAY).append(player.displayName().color(NamedTextColor.WHITE)));
    }

    public boolean isVoted(Player player) {
        return votes.containsKey(player);
    }
}
