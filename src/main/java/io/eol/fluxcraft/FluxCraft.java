package io.eol.fluxcraft;

import io.eol.fluxcraft.commands.MQTTCommand;
import io.eol.fluxcraft.listeners.ChatListener;
import io.eol.fluxcraft.listeners.CraftingListener;
import io.eol.fluxcraft.listeners.EntityListener;
import io.eol.fluxcraft.listeners.ExplosionListener;
import io.eol.fluxcraft.listeners.FurnaceListener;
import io.eol.fluxcraft.listeners.InteractionListener;
import io.eol.fluxcraft.listeners.PistonListener;
import io.eol.fluxcraft.listeners.PlayerActionListener;
import io.eol.fluxcraft.listeners.PlayerConsumeListener;
import io.eol.fluxcraft.listeners.PlayerLifecycleListener;
import io.eol.fluxcraft.listeners.WeatherListener;
import io.eol.fluxcraft.mqtt.CommandSubscriber;
import io.eol.fluxcraft.mqtt.MQTTManager;
import io.eol.fluxcraft.tasks.TimeTask;
import org.bukkit.plugin.java.JavaPlugin;

public class FluxCraft extends JavaPlugin {

    private static FluxCraft instance;
    private MQTTManager mqttManager;
    private ConfigManager configManager;
    private TimeTask timeTask;
    private CommandSubscriber commandSubscriber;

    @Override
    public void onEnable() {
        instance = this;

        configManager = new ConfigManager(this);
        configManager.loadConfig();

        mqttManager = new MQTTManager(this);
        if (configManager.isCommandsEnabled()) {
            commandSubscriber = new CommandSubscriber(this);
        }

        registerListeners();
        registerCommands();
        startTasks();

        getLogger().info("Minecraft MQTT Streamer enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (timeTask != null) {
            timeTask.cancel();
        }
        if (mqttManager != null) {
            mqttManager.disconnect();
        }
        if (commandSubscriber != null) {
            commandSubscriber.disconnect();
        }
        getLogger().info("Minecraft MQTT Streamer disabled!");
    }

    public void reload() {
        configManager.reload();

        if (timeTask != null) {
            timeTask.cancel();
        }

        if (mqttManager != null) {
            mqttManager.disconnect();
        }
        if (commandSubscriber != null) {
            commandSubscriber.disconnect();
        }

        mqttManager = new MQTTManager(this);
        if (configManager.isCommandsEnabled()) {
            commandSubscriber = new CommandSubscriber(this);
        }
        startTasks();
        getLogger().info("Configuration reloaded!");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new CraftingListener(this), this);
        getServer().getPluginManager().registerEvents(new FurnaceListener(this), this);
        getServer().getPluginManager().registerEvents(new WeatherListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerActionListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerLifecycleListener(this), this);
        getServer().getPluginManager().registerEvents(new ExplosionListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerConsumeListener(this), this);
        getServer().getPluginManager().registerEvents(new InteractionListener(this), this);
        getServer().getPluginManager().registerEvents(new PistonListener(this), this);
        if (configManager.isChatEnabled()) {
            getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        }
    }

    private void registerCommands() {
        getCommand("mqtt").setExecutor(new MQTTCommand(this));
    }

    private void startTasks() {
        if (configManager.isTimeEnabled()) {
            timeTask = new TimeTask(this);
            timeTask.runTaskTimer(this, 0, configManager.getTimeInterval() * 20L);
        }
    }

    public static FluxCraft getInstance() {
        return instance;
    }

    public MQTTManager getMqttManager() {
        return mqttManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
