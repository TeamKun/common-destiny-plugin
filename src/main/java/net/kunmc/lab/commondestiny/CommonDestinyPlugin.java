package net.kunmc.lab.commondestiny;

import com.mojang.brigadier.CommandDispatcher;
import net.kunmc.lab.commondestiny.command.PairCommand;
import net.kunmc.lab.commondestiny.config.ConfigManager;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.plugin.java.JavaPlugin;

public final class CommonDestinyPlugin extends JavaPlugin {
    private static CommonDestinyPlugin instance;
    private PairingManager pairingManager;
    private ConfigManager configManager;
    private GlowingManager glowingManager;

    @Override
    public void onEnable() {
        instance = this;
        pairingManager = new PairingManager();
        configManager = new ConfigManager();
        glowingManager = new GlowingManager(pairingManager);
        configManager.load();
        CommandDispatcher<CommandListenerWrapper> dispatcher = ((CraftServer)Bukkit.getServer()).getServer().vanillaCommandDispatcher.a();
        PairCommand.register(dispatcher);
        Bukkit.getPluginManager().registerEvents(new PairingListener(), this);
    }

    @Override
    public void onDisable() {
    }

    public static CommonDestinyPlugin getInstance() {
        return instance;
    }

    public static PairingManager getPairingManager() {
        return getInstance().pairingManager;
    }

    public static GlowingManager getGlowingManager() {
        return getInstance().glowingManager;
    }

    public static ConfigManager getConfigManager() {
        return getInstance().configManager;
    }
}
