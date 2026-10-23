package io.github.liangforstudy.creativeblueprint.mixin;

import io.github.liangforstudy.creativeblueprint.SelectionInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** While dragging, the scroll wheel moves corner 2 nearer or further instead of changing hotbar slot. */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void creativeblueprint$onScroll(long window, double xOffset, double yOffset, CallbackInfo ci) {
        if (SelectionInput.onScroll(Minecraft.getInstance(), yOffset)) ci.cancel();
    }
}
