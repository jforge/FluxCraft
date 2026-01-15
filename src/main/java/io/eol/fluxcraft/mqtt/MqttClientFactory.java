package io.eol.fluxcraft.mqtt;

import io.eol.fluxcraft.ConfigManager;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;
import com.hivemq.client.mqtt.MqttClientSslConfig;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MqttClientFactory {

    public static Mqtt5ClientBuilder createBaseBuilder(ConfigManager config, String clientIdSuffix) {
        // Use full UUID for a collision-free guarantee
        String clientId = config.getClientId() + clientIdSuffix + "-" + UUID.randomUUID();
        URI brokerUri = URI.create(config.getBrokerUri());
        String scheme = brokerUri.getScheme() != null ? brokerUri.getScheme().toLowerCase() : "tcp";
        boolean isSsl = scheme.startsWith("ssl") || scheme.startsWith("tls");

        int port = brokerUri.getPort();
        if (port == -1) {
            port = isSsl ? 8883 : 1883;
        }

        // Warn about common scheme/port mismatches to help catch configuration errors.
        if (isSsl && port == 1883) {
            System.err.println("Warning: MQTT broker URI '" + config.getBrokerUri()
                    + "' uses a TLS/SSL scheme with the default non-TLS port 1883. "
                    + "This may indicate a configuration error.");
        } else if (!isSsl && port == 8883) {
            System.err.println("Warning: MQTT broker URI '" + config.getBrokerUri()
                    + "' uses a non-TLS scheme with the default TLS/SSL port 8883. "
                    + "This may indicate a configuration error.");
        }
        Mqtt5ClientBuilder builder = MqttClient.builder()
                .useMqttVersion5()
                .identifier(clientId)
                .serverHost(brokerUri.getHost())
                .serverPort(port);

        if (isSsl) {
            if (config.isDisableCertificateValidation()) {
                builder.sslConfig(MqttClientSslConfig.builder()
                        .trustManagerFactory(null)
                        .hostnameVerifier((hostname, session) -> true)
                        .build());
            } else {
                builder.sslWithDefaultConfig();
            }
        }

        return builder
                .simpleAuth()
                    .username(config.getBrokerUsername())
                    .password(config.getBrokerPassword().getBytes())
                    .applySimpleAuth()
                .automaticReconnect()
                    .initialDelay(1, TimeUnit.SECONDS)
                    .applyAutomaticReconnect();
    }
}
