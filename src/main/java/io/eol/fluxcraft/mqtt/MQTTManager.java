package io.eol.fluxcraft.mqtt;

import io.eol.fluxcraft.ConfigManager;
import io.eol.fluxcraft.FluxCraft;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MQTTManager {

    private final FluxCraft plugin;
    private final ConfigManager config;
    private final ObjectMapper objectMapper;
    private Mqtt5Client mqttClient;
    private boolean isConnected = false;

    public MQTTManager(FluxCraft plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.objectMapper = new ObjectMapper();
        connect();
    }

    public void connect() {
        try {
            String clientId = config.getClientId() + "-" + UUID.randomUUID().toString().substring(0, 8);
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
                    .build();

            plugin.getLogger().info("Connecting to MQTT broker: " + config.getBrokerUri());

            mqttClient.toBlocking().connectWith()
                    .cleanStart(true) // Translates to cleanSession=true for MQTT 3.1.1
                    .send();

            isConnected = true;
            plugin.getLogger().info("Connected to MQTT broker: " + config.getBrokerUri());
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to connect to MQTT broker: " + e.getMessage());
            if (config.isMqttDebug()) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect() {
        if (isConnected && mqttClient != null) {
            try {
                mqttClient.toBlocking().disconnect();
                isConnected = false;
                plugin.getLogger().info("Disconnected from MQTT broker.");
            } catch (Exception e) {
                plugin.getLogger().severe("Error disconnecting from MQTT broker: " + e.getMessage());
            }
        }
    }

    public void publish(String topicTemplate, Map<String, String> placeholders, Object payload) {
        if (!isConnected) {
            return;
        }

        String topic = formatTopic(topicTemplate, placeholders);

        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);

            Mqtt5Publish message = Mqtt5Publish.builder()
                    .topic(topic)
                    .payload(jsonPayload.getBytes())
                    .qos(MqttQos.fromCode(config.getQos()))
                    .retain(config.isRetain())
                    .build();

            mqttClient.toBlocking().publish(message);

            if (config.isMqttDebug()) {
                plugin.getLogger().info("Published to " + topic + ": " + jsonPayload);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to publish message to " + topic + ": " + e.getMessage());
        }
    }

    private String formatTopic(String template, Map<String, String> placeholders) {
        String topic = template;

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                topic = topic.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return topic;
    }

    public boolean isConnected() {
        return isConnected;
    }
}
