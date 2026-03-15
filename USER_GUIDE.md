# FluxCraft User Guide

This guide provides detailed instructions on how to configure and use FluxCraft.

## Usage

Download the [latest release JAR file](https://github.com/jforge/FluxCraft/releases/latest) and place it in your `plugins` folder.

Restart your server to enable the plugin.
A new `config.yml` file will be created in the plugin's folder `../plugins/FluxCraft`.

Adjust the configuration as needed and restart the server to apply the changes.

### MQTT Broker Settings
You need at least proper MQTT broker settings to receive the events.
Any broker URI reachable by the Minecraft server should work.
The plugin uses MQTT version 5 by default and supports MQTT version 3.1.1 as well.

### TLS / Secure Connections
FluxCraft supports secure connections via TLS. The plugin automatically detects whether to use a secure connection based on the URI scheme provided in the configuration:

- **Unencrypted**: Use `tcp://` (e.g., `tcp://localhost:1883`)
- **Encrypted (TLS)**: Use `ssl://` or `tls://` (e.g., `ssl://broker.example.com:8883`)

**Note on Ports**:
- If no port is specified with `tcp://`, it defaults to **1883**.
- If no port is specified with `ssl://` or `tls://`, it defaults to **8883**.
- Custom ports are supported (e.g., `ssl://mybroker:8884`).

**Advanced Security Settings**:

The plugin uses the JVM's default trust store to verify the broker's certificate, which is sufficient for brokers using certificates from well-known authorities (like Let's Encrypt).

For development or environments with self-signed certificates, you can disable certificate validation:

> ⚠️ **WARNING**
>
> Disabling certificate validation makes the connection vulnerable to Man-in-the-Middle (MITM) attacks. Only use this for internal testing or with trusted brokers.


### Multiple Servers & Unique Client IDs
If you are running multiple Minecraft servers (e.g., a BungeeCord/Velocity network) connecting to the same MQTT broker, each connection must have a unique **Client ID**. 

FluxCraft handles this automatically using a two-tier strategy:
1. **Configurable Prefix**: Set `mqtt.broker.client-id` in your `config.yml` to identify the server (e.g., `survival-01`).
2. **UUID Suffix**: The plugin appends a unique UUID to every connection.

**Benefits of this approach:**
- **No Connection Flapping**: MQTT brokers disconnect existing clients if a new one connects with the same ID. The UUID suffix prevents servers from accidentally "kicking" each other off.
- **Traceability**: You can easily identify which specific server instance and connection type (Publisher vs. Subscriber) is active in your broker's logs.
- **Scalability**: You can spin up multiple instances of the same server template without worrying about ID collisions.

### UNS Topic Templates

The plugin uses a set of predefined topic templates to generate the actual MQTT topics.
You can customize the template strings to fit your needs.

You might want to use the plugin on multiple minecaft servers and the same MQTT broker.
In this case, you should especially adjust the `topics.base` configuration value to include the server name.

### Event Settings
You can also enable or disable individual events in the `events` section of the `config.yml` file.

### Logging Settings
You can adjust the logging level and MQTT debug messages in the `logging` section of the `config.yml` file.

### Experimental Features
The configuration file also includes experimental features that are not yet fully tested.
This includes the `chat` and `commands` events.

## Example configuration
```yaml
# FluxCraft UNS Configuration
mqtt:
  broker:
    uri: "ssl://broker.example.org:8883"
    username: "server"
    password: "your-secret-password"
    client-id: "fluxcraft"
    tls:
      disableCertificateValidation: false
    keep-alive: 60
    connection-timeout: 30
    automatic-reconnect: true
  qos: 1
  retain: false

  # Unified Namespace (UNS) Topic Templates
  # Placeholders: {world}, {playerName}, {deviceType}, {deviceId}, {blockType}
  topics:
    base: "server/YourServerName"

    world:
      time: "{base}/world/{world}/time"
      weather: "{base}/world/{world}/weather"

    players:
      base: "{base}/players/{playerName}/events"
      craft: "{players.base}/craft"
      build: "{players.base}/build"
      mine: "{players.base}/mine"
      died: "{players.base}/died"
      respawn: "{players.base}/respawn"
      eat: "{players.base}/eat"
      damage: "{players.base}/damage"
      join: "{players.base}/join"
      quit: "{players.base}/quit"
      # Future additions: fight, healthloss, kill, trade

    enemies:
      base: "{base}/enemies/{enemyType}/{enemyId}/events"
      damage: "{enemies.base}/damage"
      death: "{enemies.base}/death"
      spawn: "{enemies.base}/spawn"

    commands:
      execute: "{base}/commands/execute"
 
    chat:
      messages: "{base}/chat/messages"
      send: "{base}/chat/send"

    devices:
      base: "{base}/devices/{deviceType}/{deviceId}/events"
      furnace_burn: "{devices.base}/burn"
      furnace_smelt: "{devices.base}/smelt"
      furnace_extract: "{devices.base}/extract"
      lever_toggle: "{devices.base}/toggle"
      button_press: "{devices.base}/press"
      pressure_plate_trigger: "{devices.base}/trigger"
      door_toggle: "{devices.base}/toggle"
      trapdoor_toggle: "{devices.base}/toggle"
      piston_extend: "{devices.base}/extend"
      piston_retract: "{devices.base}/retract"
      crafting_station_new: "{devices.base}/new"
      crafting_station_craft: "{devices.base}/craft"
      
    special_blocks:
      base: "{base}/specialblocks/{blockType}/{blockId}/events"
      tnt_explosion: "{special_blocks.base}/explosion"

events:
  time:
    enabled: true
    interval: 15
  
  weather:
    enabled: true

  players:
    enabled: true
    # Fine-grained control for player events
    craft: true
    build: true
    mine: true
    died: true
    respawn: true
    eat: true
    damage: true
    join: true
    quit: true

  enemies:
    enabled: true
    damage: true
    death: true
    spawn: true

  commands:
    enabled: true

  chat:
    enabled: true

  devices:
    enabled: true
    furnace: true
    lever: true
    button: true
    pressure_plate: true
    door: true
    trapdoor: true
    piston: true
    crafting_station: true
    
  special_blocks:
    enabled: true
    tnt: true

logging:
  level: "INFO"
  mqtt-debug: false
  event-debug: false
```