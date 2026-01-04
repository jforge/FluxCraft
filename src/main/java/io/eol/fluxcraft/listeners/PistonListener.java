package io.eol.fluxcraft.listeners;

import io.eol.fluxcraft.ConfigManager;
import io.eol.fluxcraft.FluxCraft;
import io.eol.fluxcraft.events.PistonEvent;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.HashMap;
import java.util.Map;

public class PistonListener implements Listener {

    private final FluxCraft plugin;
    private final ConfigManager config;

    public PistonListener(FluxCraft plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    private String getDeviceId(Block block) {
        return block.getWorld().getName() + "@" + block.getX() + "_" + block.getY() + "_" + block.getZ();
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (!config.isPistonEventsEnabled() || event.isCancelled()) {
            return;
        }

        Block block = event.getBlock();
        PistonEvent payload = new PistonEvent("EXTEND", block.getLocation());

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("deviceType", "piston");
        placeholders.put("deviceId", getDeviceId(block));

        plugin.getMqttManager().publish(config.getPistonExtendTopic(), placeholders, payload);
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (!config.isPistonEventsEnabled() || event.isCancelled()) {
            return;
        }

        Block block = event.getBlock();
        PistonEvent payload = new PistonEvent("RETRACT", block.getLocation());

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("deviceType", "piston");
        placeholders.put("deviceId", getDeviceId(block));

        plugin.getMqttManager().publish(config.getPistonRetractTopic(), placeholders, payload);
    }
}
