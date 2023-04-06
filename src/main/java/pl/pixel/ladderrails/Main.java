package pl.pixel.ladderrails;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    public static Main instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        if(getConfig().getDouble("minecart-ladder-speed", 0.2) < 0.1) {
            getLogger().warning("minecart-ladder-speed cannot be less than 0.1. Setting it to 0.1.");
            getConfig().set("minecart-ladder-speed", 0.2);
            saveConfig();
        }

        getServer().getPluginManager().registerEvents(new Listeners(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
