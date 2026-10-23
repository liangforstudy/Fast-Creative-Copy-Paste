package io.github.liangforstudy.creativeblueprint.mixin;

import io.github.liangforstudy.creativeblueprint.DevTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Catches the dev client's {@code #cbtest} chat messages before they are sent (see {@link DevTools}). */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
    private void creativeblueprint$sendChat(String message, CallbackInfo ci) {
        if (DevTools.onChat(Minecraft.getInstance(), message)) ci.cancel();
    }
}
