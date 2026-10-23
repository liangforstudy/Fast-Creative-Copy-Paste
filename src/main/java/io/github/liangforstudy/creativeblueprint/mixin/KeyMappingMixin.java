package io.github.liangforstudy.creativeblueprint.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Before 1.21.11 vanilla sends each key to one mapping only, so two mappings on one key (Select
 * and Sprint on Left Ctrl, Lock and Pick Block on middle click) would leave one dead. On those
 * versions this passes presses and releases on to every other mapping bound to the same key, as
 * 1.21.11+ does. (Neo)Forge already handles this, so it only does anything on older Fabric.
 */
@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin {
    //? if fabric && <1.21.11 {
    /*@Shadow @Final private static Map<String, KeyMapping> ALL;
    @Shadow @Final private static Map<InputConstants.Key, KeyMapping> MAP;
    @Shadow private InputConstants.Key key;
    @Shadow private int clickCount;

    @Inject(method = "click", at = @At("TAIL"))
    private static void creativeblueprint$click(InputConstants.Key key, CallbackInfo ci) {
        KeyMapping handled = MAP.get(key);
        for (KeyMapping mapping : ALL.values()) {
            KeyMappingMixin other = (KeyMappingMixin) (Object) mapping;
            if (mapping != handled && key.equals(other.key)) other.clickCount++;
        }
    }

    @Inject(method = "set", at = @At("TAIL"))
    private static void creativeblueprint$set(InputConstants.Key key, boolean down, CallbackInfo ci) {
        KeyMapping handled = MAP.get(key);
        for (KeyMapping mapping : ALL.values()) {
            if (mapping != handled && key.equals(((KeyMappingMixin) (Object) mapping).key)) mapping.setDown(down);
        }
    }
    *///?}
}
