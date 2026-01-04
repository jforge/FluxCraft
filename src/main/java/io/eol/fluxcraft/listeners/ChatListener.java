package io.eol.fluxcraft.listeners;

import io.eol.fluxcraft.ConfigManager;
import io.eol.fluxcraft.FluxCraft;
import io.eol.fluxcraft.events.ChatMessageEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    private final FluxCraft plugin;
    private final ConfigManager config;

    public ChatListener(FluxCraft plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!config.isChatEnabled() || event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        String message = event.getMessage();

        ChatMessageEvent payload = new ChatMessageEvent(player.getName(), message);

        plugin.getMqttManager().publish(config.getChatMessagesTopic(), null, payload);
    }
}
