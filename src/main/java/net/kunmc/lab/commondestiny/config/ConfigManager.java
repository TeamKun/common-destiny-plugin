package net.kunmc.lab.commondestiny.config;

import net.kunmc.lab.commondestiny.CommonDestinyPlugin;
import net.kunmc.lab.commondestiny.config.parser.BooleanParser;
import net.kunmc.lab.commondestiny.config.parser.DoubleParser;
import net.kunmc.lab.commondestiny.config.parser.LongParser;
import net.kunmc.lab.commondestiny.config.parser.Parser;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private static final Map<String, Parser<?>> CONFIGS = new HashMap<>() {{
        put("range", new DoubleParser(0, Integer.MAX_VALUE));
        put("enabled", new BooleanParser());
        put("differentWorldTimer", new LongParser(0, Long.MAX_VALUE));
    }};
    private FileConfiguration config;

    public static String[] getConfigPaths() {
        return CONFIGS.keySet().toArray(new String[0]);
    }

    public void load() {
        CommonDestinyPlugin plugin = CommonDestinyPlugin.getInstance();
        plugin.saveDefaultConfig();
        if (config != null) {
            plugin.reloadConfig();
        }
        config = plugin.getConfig();
    }

    public boolean setConfig(String path, String valueString) {
        if (!CONFIGS.containsKey(path)) {
            return false;
        }
        Parser<?> parser = CONFIGS.get(path);
        Object value = parser.parse(valueString);
        return setConfig(path, value);
    }

    private boolean setConfig(String path, Object value) {
        if (value == null) {
            return false;
        }
        CommonDestinyPlugin plugin = CommonDestinyPlugin.getInstance();
        config.set(path, value);
        plugin.saveConfig();
        return true;
    }

    public String get(String path) {
        return String.valueOf(config.get(path));
    }

    public double getRange() {
        return config.getDouble("range");
    }

    public boolean isEnabled() {
        return config.getBoolean("enabled");
    }

    public long getDifferentWorldTimer() {
        return config.getLong("differentWorldTimer");
    }
}
