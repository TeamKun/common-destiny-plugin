package net.kunmc.lab.commondestiny.config;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kunmc.lab.commondestiny.CommonDestinyPlugin;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.ChatColor;

import static net.kunmc.lab.commondestiny.command.CommandUtils.literal;
import static net.kunmc.lab.commondestiny.command.CommandUtils.word;

public class ConfigCommand {
    public static void register(CommandDispatcher<CommandListenerWrapper> dispatcher) {
        LiteralArgumentBuilder<CommandListenerWrapper> builder = literal("cdconfig")
                .requires(clw -> clw.getBukkitSender().hasPermission("commondestiny.configcommand"));
        builder.then(literal("reload").executes(ConfigCommand::reload));
        LiteralArgumentBuilder<CommandListenerWrapper> setBuilder = literal("set");
        for (String path : ConfigManager.getConfigPaths()) {
            setBuilder.then(literal(path).then(word("value").executes(context -> set(context, path))));
        }
        builder.then(setBuilder);
        dispatcher.register(builder);
    }

    private static int set(CommandContext<CommandListenerWrapper> context, String path) {
        ConfigManager configManager = CommonDestinyPlugin.getInstance().getConfigManager();
        String value = StringArgumentType.getString(context, "value");
        boolean result = configManager.setConfig(path, value);
        if (result) {
            context.getSource().sendMessage(new ChatComponentText(path + "を" + value + "にセットしました"), false);
        } else {
            context.getSource().sendMessage(new ChatComponentText(ChatColor.RED + "コンフィグの設定に失敗しました"), false);
        }
        return 0;
    }

    private static int reload(CommandContext<CommandListenerWrapper> context) {
        ConfigManager configManager = CommonDestinyPlugin.getInstance().getConfigManager();
        configManager.load();
        context.getSource().sendMessage(new ChatComponentText("コンフィグをリロードしました"), false);
        return 0;
    }
}