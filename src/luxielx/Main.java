package luxielx;


import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener {

    public static Main main;
    public static FileConfiguration config;

    public static Main getPlugin() {
        return main;
    }


    @Override
    public void onEnable() {
        this.main = this;
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        reloadConfig();
        config = this.getConfig();
        this.getServer().getPluginManager().registerEvents(new Lis(),this);
    }

    public void onDisable() {
    }


}

