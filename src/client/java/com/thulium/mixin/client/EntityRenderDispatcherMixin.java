package com.thulium.mixin.client;

import com.thulium.ThuliumClient;
import com.thulium.client.ClientMobGroupManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    private static final int HALF_CHUNK_SPAN = 3;
    private static final List<Entity> thulium$entityBuffer = new ArrayList<>(128);
    private static long thulium$lastTick = Long.MIN_VALUE;

    private static boolean thulium$isBoatOrRaft(Entity entity) {
        String simpleName = entity.getClass().getSimpleName();
        return simpleName.contains("Boat") || simpleName.contains("Raft");
    }

    private static List<Entity> thulium$getTickEntities(Minecraft mc, double range) {
        if (mc.level == null || mc.player == null) {
            thulium$entityBuffer.clear();
            return thulium$entityBuffer;
        }

        long tick = mc.level.getGameTime();
        if (tick != thulium$lastTick) {
            thulium$lastTick = tick;
            thulium$entityBuffer.clear();
            AABB box = mc.player.getBoundingBox().inflate(range);
            List<Entity> found = mc.level.getEntitiesOfClass(
                    Entity.class,
                    box,
                    e -> e != null && e.isAlive() && e != mc.player
            );
            thulium$entityBuffer.addAll(found);
        }

        return thulium$entityBuffer;
    }

    @Inject(method = "shouldRender", at = @At("RETURN"), cancellable = true)
    private void thulium$onShouldRender(
            Entity entity,
            Frustum frustum,
            double x,
            double y,
            double z,
            float partialTick,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!Boolean.TRUE.equals(cir.getReturnValue()) || entity == null) {
            return;
        }

        if (ThuliumClient.getConfig() == null || !ThuliumClient.getConfig().isEnabled()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        if (entity == mc.player || entity.getType().toString().contains("player")) {
            return;
        }

        if (entity instanceof ItemEntity || thulium$isBoatOrRaft(entity)) {
            int playerChunkX = SectionPos.blockToSectionCoord(mc.player.getX());
            int playerChunkZ = SectionPos.blockToSectionCoord(mc.player.getZ());
            int entityChunkX = SectionPos.blockToSectionCoord(entity.getX());
            int entityChunkZ = SectionPos.blockToSectionCoord(entity.getZ());

            if (Math.abs(entityChunkX - playerChunkX) > HALF_CHUNK_SPAN
                    || Math.abs(entityChunkZ - playerChunkZ) > HALF_CHUNK_SPAN) {
                cir.setReturnValue(false);
                return;
            }
        }

        double range = Math.min(48.0, Math.max(16.0, ThuliumClient.getConfig().getRadius() * 4.0));
        List<Entity> nearby = thulium$getTickEntities(mc, range);

        if (ClientMobGroupManager.shouldSkipRender(entity, nearby, mc.level.getGameTime(), ThuliumClient.getConfig())) {
            cir.setReturnValue(false);
        }
    }
}