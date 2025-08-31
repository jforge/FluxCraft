package com.coreflux.mqtt.events;

import org.bukkit.Location;
import java.util.Map;

public class PistonEvent {
    public final String action; // EXTEND or RETRACT
    public final Map<String, Double> location;
    public final long timestamp;

    public PistonEvent(String action, Location loc) {
        this.action = action;
        this.location = Map.of("x", loc.getX(), "y", loc.getY(), "z", loc.getZ());
        this.timestamp = System.currentTimeMillis();
    }
}
