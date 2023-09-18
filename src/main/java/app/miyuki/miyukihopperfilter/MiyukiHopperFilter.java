package app.miyuki.miyukihopperfilter;

import app.miyuki.miyukihopperfilter.listener.HopperFilterListener;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MiyukiHopperFilter extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        Bukkit.getPluginManager().registerEvents(new HopperFilterListener(this), this);

        getLogger().info("MiyukiHopperFilter enabled!");

        setupMetrics();
    }

    private void setupMetrics() {
        new Metrics(this, 19830);
    }

}
