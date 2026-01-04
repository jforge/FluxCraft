package io.eol.fluxcraft.tasks;

import io.eol.fluxcraft.ConfigManager;
import io.eol.fluxcraft.FluxCraft;
import io.eol.fluxcraft.events.TimeEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class TimeTask extends BukkitRunnable {

    private final FluxCraft plugin;
    private final ConfigManager config;

    public TimeTask(FluxCraft plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    @Override
    public void run() {
        if (!config.isTimeEnabled()) {
            return;
        }

        for (World world : Bukkit.getWorlds()) {
            long time = world.getTime();
            boolean isNight = world.getTime() > 12300 && world.getTime() < 23850;

            TimeEvent payload = new TimeEvent(time, isNight);

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("world", world.getName());

            plugin.getMqttManager().publish(config.getTimeTopic(), placeholders, payload);
        }
    }
}
