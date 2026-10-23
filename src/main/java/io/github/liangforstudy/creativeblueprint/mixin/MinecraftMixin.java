package io.github.liangforstudy.creativeblueprint.mixin;

import io.github.liangforstudy.creativeblueprint.Ghost;
import io.github.liangforstudy.creativeblueprint.SelectionInput;
import io.github.liangforstudy.creativeblueprint.Stamp;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Routes the attack key to {@link SelectionInput} while Ctrl + left mouse is a selection drag. */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void creativeblueprint$startAttack(CallbackInfoReturnable<Boolean> cir) {
        if (SelectionInput.onAttackPressed((Minecraft) (Object) this)) cir.setReturnValue(false);
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void creativeblueprint$continueAttack(boolean leftClick, CallbackInfo ci) {
        // With leftClick false vanilla only stops any block breaking, which should still happen
        if (leftClick && SelectionInput.blocksHeldAttack((Minecraft) (Object) this)) ci.cancel();
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void creativeblueprint$startUseItem(CallbackInfo ci) {
        if (SelectionInput.onUsePressed((Minecraft) (Object) this)) ci.cancel();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void creativeblueprint$beforeTick(CallbackInfo ci) {
        Ghost.beforeTick((Minecraft) (Object) this);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void creativeblueprint$tick(CallbackInfo ci) {
        SelectionInput.tick((Minecraft) (Object) this);
        Ghost.tick((Minecraft) (Object) this);
        Stamp.tick((Minecraft) (Object) this);
    }
}
