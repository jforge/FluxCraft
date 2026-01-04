# FluxCraft

**FluxCraft** is an open-source Minecraft Bukkit plugin focused on streaming in-game events from a Minecraft server to an MQTT broker.
It is designed to follow the Unified Namespace (UNS) architecture, providing a structured and scalable way to
monitor server and player activities in real-time. This plugin captures a wide range of events,
including player actions, enemy engagements, and world changes, and publishes them to specific MQTT topics.

![Minecraft FluxCraft Logo](fluxcraft.png)

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
Third-party notices and attributions are documented in **[`NOTICE.md`](NOTICE.md)**.

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
