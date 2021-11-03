package net.kunmc.lab.commondestiny;

import com.mojang.brigadier.CommandDispatcher;
import net.kunmc.lab.commondestiny.command.PairCommand;
import net.kunmc.lab.commondestiny.config.ConfigCommand;
import net.kunmc.lab.commondestiny.config.ConfigManager;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.plugin.java.JavaPlugin;

public final class CommonDestinyPlugin extends JavaPlugin {
    private static CommonDestinyPlugin instance;
    private PairingManager pairingManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;
        pairingManager = new PairingManager();
        configManager = new ConfigManager();
        configManager.load();
        CommandDispatcher<CommandListenerWrapper> dispatcher = ((CraftServer)Bukkit.getServer()).getServer().vanillaCommandDispatcher.a();
        ConfigCommand.register(dispatcher);
        PairCommand.register(dispatcher);
        Bukkit.getPluginManager().registerEvents(new PairingListener(), this);
    }

    @Override
    public void onDisable() {
    }

    public static CommonDestinyPlugin getInstance() {
        return instance;
    }

    public PairingManager getPairingManager() {
        return pairingManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
