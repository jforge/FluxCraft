package io.eol.fluxcraft.listeners;

import io.eol.fluxcraft.ConfigManager;
import io.eol.fluxcraft.FluxCraft;
import io.eol.fluxcraft.events.PlayerEatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class PlayerConsumeListener implements Listener {

    private final FluxCraft plugin;
    private final ConfigManager config;

    public PlayerConsumeListener(FluxCraft plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        if (!config.isPlayerEatEnabled() || event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack consumedItem = event.getItem();

        PlayerEatEvent payload = new PlayerEatEvent(
            consumedItem.getType().toString(),
            player.getLocation()
        );

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("playerName", player.getName());

        plugin.getMqttManager().publish(config.getPlayerEatTopic(), placeholders, payload);
    }
}
