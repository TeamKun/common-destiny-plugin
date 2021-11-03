package net.kunmc.lab.commondestiny.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.server.v1_16_R3.ArgumentEntity;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import net.minecraft.server.v1_16_R3.EntitySelector;

import java.util.function.Predicate;

public class CommandUtils {
    public static LiteralArgumentBuilder<CommandListenerWrapper> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public static RequiredArgumentBuilder<CommandListenerWrapper, EntitySelector> player(String name) {
        return RequiredArgumentBuilder.argument(name, ArgumentEntity.c());
    }

    public static RequiredArgumentBuilder<CommandListenerWrapper, String> word(String name) {
        return RequiredArgumentBuilder.argument(name, StringArgumentType.word());
    }

    public static Predicate<CommandListenerWrapper> requirePermission(String name) {
        return clw -> clw.getBukkitSender().hasPermission(name);
    }
}
