package com.thulium.client;

import com.thulium.config.ClumpConfig;
import net.minecraft.world.entity.Entity;

import java.util.List;

public class ClientMobGroupManager {

    public static boolean shouldSkipRender(Entity entity, List<Entity> nearby, long tick, ClumpConfig config) {
        if (entity == null || nearby == null || nearby.isEmpty() || config == null) {
            return false;
        }

        double radius = config.getRadius();
        double radiusSq = radius * radius;
        int maxRendered = Math.max(1, config.getMaxRenderedPerGroup());

        int countInGroup = 0;
        int lowerIdCount = 0;

        for (Entity other : nearby) {
            if (other == null || !other.isAlive()) {
                continue;
            }
            if (other.getType() != entity.getType()) {
                continue;
            }
            double distSq = other.distanceToSqr(entity);
            if (distSq <= radiusSq) {
                countInGroup++;
                if (other.getId() < entity.getId()) {
                    lowerIdCount++;
                }
            }
        }

        if (countInGroup <= maxRendered) {
            return false;
        }

        return lowerIdCount >= maxRendered;
    }
}