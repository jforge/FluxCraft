package io.eol.fluxcraft.mqtt;

import io.eol.fluxcraft.ConfigManager;
import io.eol.fluxcraft.FluxCraft;
import io.eol.fluxcraft.events.ChatMessageEvent;
import com.hivemq.client.mqtt.MqttClientState;
import org.bukkit.Bukkit;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CommandSubscriber {
    private final FluxCraft plugin;
    private final ConfigManager config;
    private Mqtt5Client mqttClient;

    public CommandSubscriber(FluxCraft plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        connect();
    }

    private void connect() {
        try {
            String clientId = config.getClientId() + "-subscriber-" + UUID.randomUUID().toString().substring(0, 8);
            URI brokerUri = URI.create(config.getBrokerUri());

            // Let the client auto-negotiate (tries MQTT 5, falls back to 3.1.1)
            mqttClient = MqttClient.builder()
                    .useMqttVersion5()
                    .identifier(clientId)
                    .serverHost(brokerUri.getHost())
                    .serverPort(brokerUri.getPort())
                    .simpleAuth()
                    .username(config.getBrokerUsername())
                    .password(config.getBrokerPassword().getBytes())
                    .applySimpleAuth()
                    .automaticReconnect()
                    .initialDelay(1, TimeUnit.SECONDS)
                    .applyAutomaticReconnect()
                    .addConnectedListener(context -> {
                        plugin.getLogger().info("Subscriber connected to MQTT broker. Subscribing to command topic...");
                        subscribeToTopics();
                    })
                    .addDisconnectedListener(context -> {
                        plugin.getLogger().warning("Subscriber connection lost: " + context.getCause().getMessage());
                    })
                    .build();

            plugin.getLogger().info("Connecting subscriber to MQTT broker: " + config.getBrokerUri());

            mqttClient.toBlocking().connectWith()
                    .cleanStart(true) // Translates to cleanSession=true for MQTT 3.1.1
                    .send();

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to connect subscriber to MQTT broker: " + e.getMessage());
        }
    }

    private void subscribeToTopics() {
        // Subscribe to Command Topic
        mqttClient.toAsync().subscribeWith()
                .topicFilter(config.getCommandExecuteTopic())
                .qos(MqttQos.fromCode(config.getQos()))
                .callback(this::handleMessage)
                .send();

        // Subscribe to Chat Topic if enabled
        if (config.isChatEnabled()) {
            mqttClient.toAsync().subscribeWith()
                    .topicFilter(config.getChatSendTopic())
                    .qos(MqttQos.fromCode(config.getQos()))
                    .callback(this::handleMessage)
                    .send();
        }
    }

    private void handleMessage(Mqtt5Publish publish) {
        String topic = publish.getTopic().toString();
        String payload = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);

        if (topic.equals(config.getCommandExecuteTopic())) {
            plugin.getLogger().info("Received command: " + payload);
            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), payload);
            });
        } else if (topic.equals(config.getChatSendTopic())) {
            plugin.getLogger().info("Received chat message: " + payload);

            // Broadcast to in-game chat
            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.broadcastMessage("§e[Chatbot] §f" + payload);
            });

            // Re-publish to the chat messages topic
            ChatMessageEvent chatPayload = new ChatMessageEvent("Chatbot", payload);
            plugin.getMqttManager().publish(config.getChatMessagesTopic(), null, chatPayload);
        }
    }

    public boolean isConnected() {
        return mqttClient != null && mqttClient.getState() == MqttClientState.CONNECTED;
    }

    public void disconnect() {
        if (isConnected()) {
            try {
                mqttClient.toBlocking().disconnect();
            } catch (Exception e) {
                plugin.getLogger().severe("Error disconnecting subscriber: " + e.getMessage());
            }
        }
    }
}
