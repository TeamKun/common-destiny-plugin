package net.kunmc.lab.commondestiny.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kunmc.lab.commondestiny.CommonDestinyPlugin;
import net.kunmc.lab.commondestiny.PairResult;
import net.kunmc.lab.commondestiny.PairingManager;
import net.kunmc.lab.commondestiny.VoteSystem;
import net.kyori.adventure.text.Component;
import net.minecraft.server.v1_16_R3.ArgumentEntity;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static net.kunmc.lab.commondestiny.command.CommandUtils.*;

public class PairCommand {
    public static void register(CommandDispatcher<CommandListenerWrapper> dispatcher) {
        LiteralArgumentBuilder<CommandListenerWrapper> builder = literal("pair");
        builder.then(literal("vote")
                .then(player("partner")
                        .executes(PairCommand::vote))
                .then(literal("start")
                        .requires(requirePermission("commondestiny.paircommand"))
                        .executes(PairCommand::startVote))
                .then(literal("end")
                        .requires(requirePermission("commondestiny.paircommand"))
                        .executes(PairCommand::endVote)));
        builder.then(literal("list")
                .executes(PairCommand::listPairing));
        builder.then(literal("reset")
                .requires(requirePermission("commondestiny.paircommand"))
                .executes(PairCommand::resetPairing));
        builder.then(literal("force")
                .requires(requirePermission("commondestiny.paircommand"))
                .then(player("player1")
                .then(player("player2")
                .executes(PairCommand::forcePairing))));
        builder.then(literal("random")
                .requires(requirePermission("commondestiny.paircommand"))
                .executes(PairCommand::randomPairing));
        builder.then(literal("remaining")
                .requires(requirePermission("commondestiny.paircommand"))
                .executes(PairCommand::showRemaining));
        dispatcher.register(builder);
    }

    private static int startVote(CommandContext<CommandListenerWrapper> context) {
        if (VoteSystem.isVoteStarted()) {
            context.getSource().sendMessage(new ChatComponentText("とうひょうがすでにかいしされています"), false);
            return 0;
        }
        Bukkit.broadcast(Component.text("とうひょうをかいしするよ"));
        VoteSystem.start();
        return 0;
    }

    private static int endVote(CommandContext<CommandListenerWrapper> context) {
        PairingManager manager = CommonDestinyPlugin.getInstance().getPairingManager();
        if (!VoteSystem.isVoteStarted()) {
            context.getSource().sendMessage(new ChatComponentText("とうひょうがかいしされていません"), false);
            return 0;
        }
        Bukkit.broadcast(Component.text("とうひょうけっか"));
        List<PairResult> results = VoteSystem.getVoteInstance().matchResults();
        results.sort(Comparator.comparingInt(PairResult::ord)
                .thenComparing(pair -> pair.voted() ? pair.player2.getUniqueId() : null, Comparator.nullsLast(Comparator.naturalOrder())));
        for (PairResult pair : results) {
            if (!pair.voted()) {
                Bukkit.broadcast(Component.text(pair.player1.getName() + " => 未投票"));
            } else if (!pair.matched()) {
                Bukkit.broadcast(Component.text(pair.player1.getName() + " => " + pair.player2.getName()));
            } else {
                Bukkit.broadcast(Component.text(pair.player1.getName() + " <=> " + pair.player2.getName()));
            }
        }
        for (PairResult pair : results) {
            if (pair.matched()) {
                manager.form(pair.player1, pair.player2, false);
            }
        }
        VoteSystem.end();
        return 0;
    }

    private static int vote(CommandContext<CommandListenerWrapper> context) throws CommandSyntaxException {
        CraftPlayer partner = ArgumentEntity.e(context, "partner").getBukkitEntity();
        if (!(context.getSource().getBukkitSender() instanceof CraftPlayer)) {
            return 0;
        }
        if (!VoteSystem.isVoteStarted()) {
            context.getSource().sendMessage(new ChatComponentText("とうひょうがかいしされていません"), false);
            return 0;
        }
        PairingManager manager = CommonDestinyPlugin.getInstance().getPairingManager();
        VoteSystem voteSystem = VoteSystem.getVoteInstance();
        CraftPlayer sender = (CraftPlayer)context.getSource().getBukkitSender();
        if (manager.hasPartner(sender)) {
            context.getSource().sendMessage(new ChatComponentText("すでにペアが成立しています"), false);
        } else if (sender.equals(partner)) {
            context.getSource().sendMessage(new ChatComponentText("自分自身には投票できません"), false);
        } else if (manager.hasPartner(partner)) {
            context.getSource().sendMessage(new ChatComponentText("すでにペアが成立しているプレイヤーには投票できません"), false);
        } else {
            context.getSource().sendMessage(new ChatComponentText(partner.getName() + " にとうひょうしたよ"), false);
            voteSystem.vote(sender, partner);
        }
        return 0;
    }

    private static int listPairing(CommandContext<CommandListenerWrapper> context) {
        PairingManager manager = CommonDestinyPlugin.getInstance().getPairingManager();
        context.getSource().sendMessage(new ChatComponentText("せいりつづみのぺあ"), false);
        for (PairResult pair : manager.pairs()) {
            context.getSource().sendMessage(new ChatComponentText(pair.player1.getName() + " <=> " + pair.player2.getName()), false);
        }
        return 0;
    }

    private static int resetPairing(CommandContext<CommandListenerWrapper> context) {
        PairingManager manager = CommonDestinyPlugin.getInstance().getPairingManager();
        manager.reset();
        Bukkit.broadcast(Component.text("ぜんぶりせっとしたよ"));
        return 0;
    }

    private static int forcePairing(CommandContext<CommandListenerWrapper> context) throws CommandSyntaxException {
        PairingManager manager = CommonDestinyPlugin.getInstance().getPairingManager();
        CraftPlayer player1 = ArgumentEntity.e(context, "player1").getBukkitEntity();
        CraftPlayer player2 = ArgumentEntity.e(context, "player2").getBukkitEntity();
        if (player1.equals(player2)) {
            context.getSource().sendMessage(new ChatComponentText("一人でペアになることはできません"), false);
            return 0;
        }
        if (manager.isPair(player1, player2)) {
            manager.dissolve(player1, true);
            return 0;
        }
        if (manager.hasPartner(player1)) {
            manager.dissolve(player1, true);
        }
        if (manager.hasPartner(player2)) {
            manager.dissolve(player2, true);
        }
        manager.form(player1, player2, true);
        return 0;
    }

    private static int randomPairing(CommandContext<CommandListenerWrapper> context) {
        PairingManager manager = CommonDestinyPlugin.getInstance().getPairingManager();
        List<Player> remaining = manager.remainingPlayers();
        Collections.shuffle(remaining);
        for (int i = 0; i < remaining.size(); i += 2) {
            manager.form(remaining.get(i), remaining.get(i + 1), false);
        }
        int formed = remaining.size() / 2;
        Bukkit.broadcast(Component.text(formed + " くみのぺあをせいりつさせたよ"));
        return 0;
    }

    private static int showRemaining(CommandContext<CommandListenerWrapper> context) {
        PairingManager manager = CommonDestinyPlugin.getInstance().getPairingManager();
        List<Player> remaining = manager.remainingPlayers();
        context.getSource().sendMessage(new ChatComponentText("あまっているぷれいやー:"), false);
        for (Player player : remaining) {
            context.getSource().sendMessage(new ChatComponentText(player.getName()), false);
        }
        return 0;
    }
}
