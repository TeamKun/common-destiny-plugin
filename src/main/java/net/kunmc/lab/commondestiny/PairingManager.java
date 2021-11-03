package net.kunmc.lab.commondestiny;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PairingManager {
    private final Map<Player, Player> partners = new HashMap<>();

    public void reset() {
        partners.clear();
    }

    public List<Player> remainingPlayers() {
        return Bukkit.getOnlinePlayers().stream()
                .filter(Predicate.not(this::hasPartner))
                .collect(Collectors.toList());
    }

    public List<PairResult> pairs() {
        return Bukkit.getOnlinePlayers().stream()
                .filter(this::hasPartner)
                .map(player -> new PairResult(player, getPartner(player), true))
                .filter(pair -> pair.player1.getUniqueId().compareTo(pair.player2.getUniqueId()) < 0)
                .collect(Collectors.toList());
    }

    public void form(Player player1, Player player2, boolean broadcast) {
        if (hasPartner(player1)) {
            throw new IllegalStateException(player1.getName() + " already has a partner");
        }
        if (hasPartner(player2)) {
            throw new IllegalStateException(player2.getName() + " already has a partner");
        }
        partners.put(player1, player2);
        partners.put(player2, player1);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.equals(player1)) {
                player1.sendMessage(Component.text(player2.getName() + " とペアになったよ"));
            } else if (player.equals(player2)) {
                player2.sendMessage(Component.text(player1.getName() + " とペアになったよ"));
            } else if (broadcast) {
                Bukkit.broadcast(Component.text(player1.getName() + " と " + player2.getName() + " がペアになったよ"));
            }
        }
    }

    public void dissolve(Player player1, boolean broadcast) {
        if (!hasPartner(player1)) {
            throw new IllegalStateException(player1.getName() + " has no partner");
        }
        Player player2 = getPartner(player1);
        partners.remove(player1, player2);
        partners.remove(player2, player1);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.equals(player1)) {
                player1.sendMessage(Component.text(player2.getName() + " とのペアを解散したよ"));
            } else if (player.equals(player2)) {
                player2.sendMessage(Component.text(player1.getName() + " とのペアを解散したよ"));
            } else if (broadcast) {
                Bukkit.broadcast(Component.text(player1.getName() + " と " + player2.getName() + " をかいさんさせたよ"));
            }
        }
    }

    public boolean isPair(Player player1, Player player2) {
        return player2.equals(getPartner(player1));
    }

    public boolean hasPartner(Player player) {
        return getPartner(player) != null;
    }

    public Player getPartner(Player player) {
        return partners.get(player);
    }
}
