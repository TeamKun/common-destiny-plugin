package net.kunmc.lab.commondestiny;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PairingManager {
    private final Map<Player, UUID> partners = new HashMap<>();
    private final Map<UUID, UUID> lastPartners = new HashMap<>();
    private List<PairResult> pairsCache = null;

    public void reset() {
        for (PairResult pair : pairs()) {
            dissolve(pair.player1, false);
        }
        partners.clear();
        lastPartners.clear();
        pairsCache = null;
    }

    public List<Player> remainingPlayers() {
        return Bukkit.getOnlinePlayers().stream()
                .filter(Predicate.not(this::hasPartner))
                .collect(Collectors.toList());
    }

    private void updatePairsCache() {
        pairsCache = partners.keySet().stream()
                .map(player -> new PairResult(player, getPartner(player), true))
                .filter(pair -> pair.player1.getUniqueId().compareTo(pair.player2.getUniqueId()) < 0)
                .collect(Collectors.toList());
    }

    public List<PairResult> pairs() {
        if (pairsCache == null) {
            updatePairsCache();
        }
        return Collections.unmodifiableList(pairsCache);
    }

    public void form(Player player1, Player player2, boolean broadcast) {
        if (hasPartner(player1)) {
            throw new IllegalStateException(player1.getName() + " already has a partner");
        }
        if (hasPartner(player2)) {
            throw new IllegalStateException(player2.getName() + " already has a partner");
        }
        partners.put(player1, player2.getUniqueId());
        partners.put(player2, player1.getUniqueId());
        lastPartners.put(player1.getUniqueId(), player2.getUniqueId());
        lastPartners.put(player2.getUniqueId(), player1.getUniqueId());
        updatePairsCache();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.equals(player1) || player.equals(player2)) {
                player.sendMessage(Component.text(player2.getName() + ChatColor.GREEN + " ????????????????????????"));
            } else if (broadcast) {
                player.sendMessage(Component.text(player1.getName() + ChatColor.GREEN + " ??? " + ChatColor.RESET + player2.getName() + ChatColor.GREEN + " ????????????????????????"));
            }
        }
        player1.playerListName(player1.displayName().color(NamedTextColor.WHITE).append(Component.text(" <=> ", NamedTextColor.GRAY)).append(player2.displayName().color(NamedTextColor.WHITE)));
        player2.playerListName(player2.displayName().color(NamedTextColor.WHITE).append(Component.text(" <=> ", NamedTextColor.GRAY)).append(player1.displayName().color(NamedTextColor.WHITE)));
        GlowingManager glowingManager = CommonDestinyPlugin.getGlowingManager();
        glowingManager.setGlowing(player1, player2, true);
        glowingManager.setGlowing(player2, player1, true);
    }

    public void dissolve(Player player1, boolean broadcast) {
        if (!hasPartner(player1)) {
            throw new IllegalStateException(player1.getName() + " has no partner");
        }
        Player player2 = getPartner(player1);
        partners.remove(player1);
        partners.remove(player2);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.equals(player1) || player.equals(player2)) {
                player.sendMessage(Component.text(player1.getName() + ChatColor.GREEN + " ??????????????????????????????"));
            } else if (broadcast) {
                player.sendMessage(Component.text(player1.getName() + ChatColor.GREEN + " ??? " + ChatColor.RESET + player2.getName() + ChatColor.GREEN + " ??????????????????????????????"));
            }
        }
        player1.playerListName(player1.displayName());
        player2.playerListName(player2.displayName());
        GlowingManager glowingManager = CommonDestinyPlugin.getGlowingManager();
        glowingManager.setGlowing(player1, player2, false);
        glowingManager.setGlowing(player2, player1, false);
    }

    public boolean isPair(Player player1, Player player2) {
        return player2.equals(getPartner(player1));
    }

    public boolean hasPartner(Player player) {
        return getPartner(player) != null;
    }

    public Player getPartner(Player player) {
        return Bukkit.getPlayer(partners.get(player));
    }

    public Player getLastPartner(Player player) {
        UUID uuid = lastPartners.get(player.getUniqueId());
        return uuid == null ? null : Bukkit.getPlayer(uuid);
    }
}
