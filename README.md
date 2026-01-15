# FluxCraft

**FluxCraft** is an open-source Minecraft Paper/Bukkit plugin focused on streaming in-game events from a Minecraft server to an MQTT broker.
It is designed to follow the Unified Namespace (UNS) architecture, providing a structured and scalable way to
monitor server and player activities in real-time. This plugin captures a wide range of events,
including player actions, enemy engagements, and world changes, and publishes them to specific MQTT topics.

![Minecraft FluxCraft Logo](fluxcraft.png)

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
```yaml
mqtt:
  broker:
    tls:
      disableCertificateValidation: true
```

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

## Fork notice

This is a fork of **[Minecraft MQTT Streamer](https://github.com/HugoAVaz/minecraft-mqtt-streamer)** by **Hugo Vaz / CoreFlux**.
This fork is not affiliated with or endorsed by CoreFlux.
It includes renaming and packaging changes plus selected modifications.
See [`LICENSE`](LICENSE) for the upstream MIT license.

## Official Project & Downloads

To protect users from reuploads, malware, and unofficial builds, **official releases** are published only via the channels below:

- **GitHub Releases (primary):** https://github.com/jforge/FluxCraft/releases
- **Modrinth:** https://modrinth.com/project/fluxcraft

If you downloaded FluxCraft anywhere else, it may be unofficial.

## Name & Logo Policy

“**FluxCraft**” and the FluxCraft logo are **project identifiers** for the official distribution of this plugin.

You may fork and redistribute the source code under the terms of the license, 
**but you may not use the name “FluxCraft” or the FluxCraft logo** in a way that suggests your build is official, 
endorsed, or affiliated with this project without written permission.

## License & Attribution

FluxCraft is distributed under the **MIT License** (see [`LICENSE`](LICENSE)).  
Third-party notices and attributions are documented in [`NOTICE.md`](NOTICE.md).

## Support / Donations

- If FluxCraft helps you, donations are welcome: https://ko-fi.com/jforge
- For support, please use GitHub Issues: https://github.com/jforge/FluxCraft/issues
- For commercial or special-case support: mailto:github@jforge

## Getting Started

This guide will walk you through setting up the development environment, 
which includes a Minecraft server and an MQTT broker.

### Prerequisites

-   **Docker and Docker Compose**: Essential for running the entire environment.
-   **Minecraft Java Edition Client**: Version 1.21 or later to connect to the server.
-   **(Optional) Java 21 and Maven**: If you prefer to build the plugin locally without Docker.

### Build and Run

Convenient scripts are provided to build the plugin and start the environment.

#### For Windows Users:

1.  **Build the plugin**:
    ```shell
    .\build.bat
    ```
    This script uses Docker to compile the Java plugin. The resulting `.jar` file is automatically copied to the `plugins` directory.

2.  **Start the services**:
    ```shell
    .\start.bat
    ```
    This command starts the Minecraft server and Coreflux MQTT broker using `docker-compose`.

3.  **Stop the services**:
    ```shell
    .\close.bat
    ```
    This command gracefully stops all the running services.

#### For Linux and macOS Users:

1.  **Build the plugin**:
    ```shell
    chmod +x build.sh
    ./build.sh
    ```
    This script uses Docker to compile the Java plugin. The resulting `.jar` file is automatically copied to the `plugins` directory.

2.  **Start the services**:
    ```shell
    chmod +x start.sh
    ./start.sh
    ```
    This command starts the Minecraft server and Coreflux MQTT broker using `docker-compose`.

3.  **Stop the services**:
    ```shell
    chmod +x close.sh
    ./close.sh
    ```
    This command gracefully stops all the running services.


### Accessing the Services

Once the services are running, you can connect to them:

-   **Minecraft Server**: Open your Minecraft client and connect to `localhost:25565`.
-   **MQTT Broker**: You can connect to the broker at `localhost:1883` with a client like [MQTT Explorer](http://mqtt-explorer.com/) or any other MQTT client. Use the following credentials:
    -   **Username**: `root`
    -   **Password**: `coreflux`
    
    Subscribe to the topic `server/#` to see all the real-time events.


## System Components

This solution is composed of four main components working together:

1.  **Minecraft FluxCraft Plugin**: A Bukkit plugin that runs on the Minecraft server. It captures a wide range of in-game events and publishes them as structured MQTT messages following the Unified Namespace (UNS) architecture.
2.  **Coreflux MQTT Broker**: A high-performance MQTT broker that acts as the central messaging hub. It receives events from the Minecraft plugin and routes them to subscribers. The broker is included in the `docker-compose.yml` file for easy setup.

## Features

- **Unified Namespace (UNS)**: The plugin is designed around the UNS architecture, ensuring that all MQTT topics are structured in a consistent and predictable way.
- **Real-Time Event Streaming**: Events are published to the MQTT broker as they happen, providing a live feed of server activities.
- **Comprehensive Event Coverage**: The plugin captures a wide range of events, including:
  - **Player Events**: Crafting, building, mining, dying, respawning, eating, and taking damage.
  - **Enemy Events**: Taking damage and dying.
  - **World Events**: Time changes and weather changes.
- **Configurable**: All events can be individually enabled or disabled in the `config.yml` file.

### Trademarks and Disclaimers

- **Coreflux**: "Coreflux" is a trademark of Coreflux Portugal SA.
- **Minecraft**: This project is not affiliated with Mojang or Microsoft. "Minecraft" is a trademark of Mojang Synergies AB.
