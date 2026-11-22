package com.thulium.mixin.client;

import com.thulium.ThuliumClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public class ChestBlockEntityRenderMixin {

    private static final int HALF_CHUNK_SPAN = 3;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private <E extends BlockEntity> void thulium$skipFarChests(E blockEntity, CallbackInfo ci) {
        if (!(blockEntity instanceof ChestBlockEntity)) {
            return;
        }

        if (ThuliumClient.getConfig() == null || !ThuliumClient.getConfig().isEnabled()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        BlockPos pos = blockEntity.getBlockPos();
        int playerChunkX = SectionPos.blockToSectionCoord(mc.player.getX());
        int playerChunkZ = SectionPos.blockToSectionCoord(mc.player.getZ());
        int chestChunkX = SectionPos.blockToSectionCoord(pos.getX());
        int chestChunkZ = SectionPos.blockToSectionCoord(pos.getZ());

        if (Math.abs(chestChunkX - playerChunkX) > HALF_CHUNK_SPAN
                || Math.abs(chestChunkZ - playerChunkZ) > HALF_CHUNK_SPAN) {
            ci.cancel();
        }
    }
}