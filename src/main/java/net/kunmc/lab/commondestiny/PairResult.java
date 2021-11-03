package net.kunmc.lab.commondestiny;

import org.bukkit.entity.Player;

public class PairResult {
    public final Player player1;
    public final Player player2;
    private final boolean matched;

    public PairResult(Player player1, Player player2, boolean matched) {
        this.player1 = player1;
        this.player2 = player2;
        this.matched = matched;
    }

    public boolean matched() {
        return matched;
    }

    public boolean voted() {
        return player2 != null;
    }

    public int ord() {
        return matched ? 2 : voted() ? 1 : 0;
    }
}
