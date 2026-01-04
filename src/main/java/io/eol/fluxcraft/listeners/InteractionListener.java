package io.eol.fluxcraft.listeners;

import io.eol.fluxcraft.ConfigManager;
import io.eol.fluxcraft.FluxCraft;
import io.eol.fluxcraft.events.InteractionEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

public class InteractionListener implements Listener {

    private final FluxCraft plugin;
    private final ConfigManager config;

    public InteractionListener(FluxCraft plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    private String getDeviceId(Block block) {
        return block.getWorld().getName() + "@" + block.getX() + "_" + block.getY() + "_" + block.getZ();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled() || event.getClickedBlock() == null) {
            return;
        }

        Block block = event.getClickedBlock();
        Material material = block.getType();
        Action action = event.getAction();
        Player player = event.getPlayer();

        String topic = null;
        String deviceType = null;

        boolean isLever = material.toString().endsWith("_LEVER") && config.isLeverEventsEnabled() && action == Action.RIGHT_CLICK_BLOCK;
        boolean isButton = material.toString().endsWith("_BUTTON") && config.isButtonEventsEnabled() && action == Action.RIGHT_CLICK_BLOCK;
        boolean isPressurePlate = material.toString().endsWith("_PRESSURE_PLATE") && config.isPressurePlateEventsEnabled() && action == Action.PHYSICAL;
        boolean isDoor = material.toString().endsWith("_DOOR") && config.isDoorEventsEnabled() && action == Action.RIGHT_CLICK_BLOCK;
        boolean isTrapdoor = material.toString().endsWith("_TRAPDOOR") && config.isTrapdoorEventsEnabled() && action == Action.RIGHT_CLICK_BLOCK;

        if (isLever) {
            topic = config.getLeverToggleTopic();
            deviceType = "lever";
        } else if (isButton) {
            topic = config.getButtonPressTopic();
            deviceType = "button";
        } else if (isPressurePlate) {
            topic = config.getPressurePlateTriggerTopic();
            deviceType = "pressure_plate";
        } else if (isDoor) {
            topic = config.getDoorToggleTopic();
            deviceType = "door";
        } else if (isTrapdoor) {
            topic = config.getTrapdoorToggleTopic();
            deviceType = "trapdoor";
        }

        if (topic != null && deviceType != null) {
            boolean isPowered = false;

            if (block.getBlockData() instanceof Powerable) {
                isPowered = !((Powerable) block.getBlockData()).isPowered();
            } else if (block.getBlockData() instanceof Openable) {
                isPowered = !((Openable) block.getBlockData()).isOpen();
            } else {
                return; // Not a block we can get a state from in this listener
            }

            InteractionEvent payload = new InteractionEvent(
                    material.toString(),
                    isPowered,
                    block.getLocation()
            );

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("deviceType", deviceType);
            placeholders.put("deviceId", getDeviceId(block));
            placeholders.put("playerName", player.getName());

            plugin.getMqttManager().publish(topic, placeholders, payload);
        }
    }
}
