package revive.smp;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Rise extends JavaPlugin {

    public static Rise plugin;

    public static Rise getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        Bukkit.getLogger().info("[RiseSMP] Has started Successfully");
        registerCommandsAndEvents();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getLogger().info("[RiseSMP] Disabled");
    }

    private void registerCommandsAndEvents() {
        getServer().getPluginManager().registerEvents(new PlayerEventHandler(), plugin);
    }
}
