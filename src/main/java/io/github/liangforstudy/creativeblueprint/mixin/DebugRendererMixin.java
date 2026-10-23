package io.github.liangforstudy.creativeblueprint.mixin;

import io.github.liangforstudy.creativeblueprint.SelectionRenderer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A per-frame hook into world rendering that exists, unconditionally, on every version. */
@Mixin(DebugRenderer.class)
public abstract class DebugRendererMixin {
    //? if >=1.21.11 {
    @Inject(method = "emitGizmos", at = @At("HEAD"))
    private void creativeblueprint$emitGizmos(net.minecraft.client.renderer.culling.Frustum frustum, double camX, double camY, double camZ,
                                              float partialTick, CallbackInfo ci) {
        SelectionRenderer.emitGizmos();
    }
    //?} else {
    /*@Inject(method = "render", at = @At("HEAD"))
    private void creativeblueprint$render(com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.MultiBufferSource.BufferSource buffers,
                                          double camX, double camY, double camZ, CallbackInfo ci) {
        SelectionRenderer.render(pose, buffers, camX, camY, camZ);
    }
    *///?}
}
