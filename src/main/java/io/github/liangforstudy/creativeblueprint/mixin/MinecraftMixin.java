package io.github.liangforstudy.creativeblueprint.mixin;

import io.github.liangforstudy.creativeblueprint.CreativeBlueprint;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Placeholder until the input mixins exist: proves mixins apply on every loader and version. */
@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void creativeblueprint$onInit(CallbackInfo ci) {
        CreativeBlueprint.LOGGER.info("Mixins applied");
    }
}
