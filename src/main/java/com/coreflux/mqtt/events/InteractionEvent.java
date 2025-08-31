package com.coreflux.mqtt.events;

import org.bukkit.Location;
import java.util.Map;

public class InteractionEvent {
    public final String blockType;
    public final boolean powered;
    public final Map<String, Double> location;
    public final long timestamp;

    public InteractionEvent(String blockType, boolean powered, Location loc) {
        this.blockType = blockType;
        this.powered = powered;
        this.location = Map.of("x", loc.getX(), "y", loc.getY(), "z", loc.getZ());
        this.timestamp = System.currentTimeMillis();
    }
}
