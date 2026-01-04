package io.eol.fluxcraft.listeners;

import io.eol.fluxcraft.ConfigManager;
import io.eol.fluxcraft.FluxCraft;
import io.eol.fluxcraft.events.BlockEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashMap;
import java.util.Map;

public class PlayerActionListener implements Listener {

    private final FluxCraft plugin;
    private final ConfigManager config;

    public PlayerActionListener(FluxCraft plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!config.isPlayerBuildEnabled() || event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        BlockEvent payload = new BlockEvent(
                event.getBlock().getType().toString(),
                event.getBlock().getLocation()
        );

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("playerName", player.getName());

        plugin.getMqttManager().publish(config.getPlayerBuildTopic(), placeholders, payload);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!config.isPlayerMineEnabled() || event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        BlockEvent payload = new BlockEvent(
                event.getBlock().getType().toString(),
                event.getBlock().getLocation()
        );

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("playerName", player.getName());

        plugin.getMqttManager().publish(config.getPlayerMineTopic(), placeholders, payload);
    }
}
