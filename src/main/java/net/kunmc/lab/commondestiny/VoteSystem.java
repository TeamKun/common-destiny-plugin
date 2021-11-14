package net.kunmc.lab.commondestiny;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class VoteSystem {
    private static VoteSystem instance;
    private static boolean started;
    private final PairingManager manager;
    private final Map<Player, Player> votes = new HashMap<>();

    private VoteSystem(PairingManager manager) {
        this.manager = manager;
    }

    public static void start() {
        PairingManager manager = CommonDestinyPlugin.getPairingManager();
        instance = new VoteSystem(manager);
        started = true;
    }

    public static void end() {
        started = false;
    }

    public static VoteSystem getVoteInstance() {
        return instance;
    }

    public static boolean isVoteStarted() {
        return started;
    }

    public void remove(Player removed) {
        List<Player> removeVote = new ArrayList<>();
        for (Map.Entry<Player, Player> entry : votes.entrySet()) {
            if (entry.getValue().equals(removed)) {
                Player player = entry.getKey();
                player.sendMessage(ChatColor.GREEN + "投票先のプレイヤーがログアウトしました 再度投票してください");
                removeVote.add(player);
            }
        }
        votes.remove(removed);
        removeVote.forEach(votes::remove);
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
    }
}
