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
    public static void register(LiteralArgumentBuilder<CommandListenerWrapper> builder) {
        LiteralArgumentBuilder<CommandListenerWrapper> subCommand = literal("config")
                .requires(clw -> clw.getBukkitSender().hasPermission("commondestiny.configcommand"));
        subCommand.then(literal("reload").executes(ConfigCommand::reload));
        LiteralArgumentBuilder<CommandListenerWrapper> setBuilder = literal("set");
        LiteralArgumentBuilder<CommandListenerWrapper> getBuilder = literal("get");
        for (String path : ConfigManager.getConfigPaths()) {
            setBuilder.then(literal(path).then(word("value").executes(context -> set(context, path))));
            getBuilder.then(literal(path).executes(context -> get(context, path)));
        }
        subCommand.then(setBuilder);
        subCommand.then(getBuilder);
        builder.then(subCommand);
    }

    private static int get(CommandContext<CommandListenerWrapper> context, String path) {
        ConfigManager configManager = CommonDestinyPlugin.getConfigManager();
        String value = configManager.get(path);
        context.getSource().sendMessage(new ChatComponentText(path + " = " + value), false);
        return 0;
    }

    private static int set(CommandContext<CommandListenerWrapper> context, String path) {
        ConfigManager configManager = CommonDestinyPlugin.getConfigManager();
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
        ConfigManager configManager = CommonDestinyPlugin.getConfigManager();
        configManager.load();
        context.getSource().sendMessage(new ChatComponentText("コンフィグをリロードしました"), false);
        return 0;
    }
}
