package com.thulium.server;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class ServerTickOptimizer {

    private static final List<Entity> thulium$serverBuffer = new ArrayList<>(128);
    private static long thulium$lastServerTick = Long.MIN_VALUE;

    public static List<Entity> getCachedEntities(ServerLevel level, Entity center, double radius) {
        if (level == null || center == null) {
            thulium$serverBuffer.clear();
            return thulium$serverBuffer;
        }

        long tick = level.getGameTime();
        if (tick != thulium$lastServerTick) {
            thulium$lastServerTick = tick;
            thulium$serverBuffer.clear();
            double safeRadius = Math.min(48.0, Math.max(8.0, radius));
            AABB box = center.getBoundingBox().inflate(safeRadius);
            List<Entity> found = level.getEntitiesOfClass(
                    Entity.class,
                    box,
                    e -> e != null && e.isAlive() && !(e instanceof Player)
            );
            thulium$serverBuffer.addAll(found);
        }

        return thulium$serverBuffer;
    }
}