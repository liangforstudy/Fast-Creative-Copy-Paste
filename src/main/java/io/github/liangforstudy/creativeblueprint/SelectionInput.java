package io.github.liangforstudy.creativeblueprint;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * The Ctrl + left mouse gesture: press starts a drag, holding continues it, letting go ends it.
 * While Ctrl is held in creative the attack key never reaches vanilla, so nothing is broken or hit.
 * Ctrl only has to be down when the drag starts; after that the drag lasts until the mouse is
 * released, or is cancelled by a right click, opening a screen (Esc) or leaving creative.
 */
public final class SelectionInput {
    private static boolean dragging;
    private static int dragTicks;
    /** Set by a right-click cancel: the rest of that right click must not place or use anything. */
    private static boolean swallowUse;

    private SelectionInput() {}

    public static boolean isDragging() {
        return dragging;
    }

    /** From the start of {@code Minecraft.startAttack}. True when the click is ours. */
    public static boolean onAttackPressed(Minecraft mc) {
        if (dragging) return true;
        if (!canStart(mc)) return false;
        dragging = true;
        dragTicks = 0;
        Compat.showActionBar(mc.player, Component.translatable("creativeblueprint.selection.dragging"));
        return true;
    }

    /** From the start of {@code Minecraft.continueAttack} while the attack key is held. True cancels it. */
    public static boolean blocksHeldAttack(Minecraft mc) {
        // Also covers pressing Ctrl in the middle of a normal hold, so creative's repeat-break stops
        return dragging || canStart(mc);
    }

    /** From the start of {@code Minecraft.startUseItem} (right click). True cancels it. */
    public static boolean onUsePressed(Minecraft mc) {
        if (dragging) {
            cancel(mc);
            swallowUse = true;
        }
        return swallowUse;
    }

    /** From the end of {@code Minecraft.tick}. */
    public static void tick(Minecraft mc) {
        if (swallowUse && !mc.options.keyUse.isDown()) swallowUse = false;
        if (!dragging) return;
        if (mc.player == null || !mc.player.isCreative() || Compat.isScreenOpen(mc)) {
            cancel(mc);
            return;
        }
        if (mc.options.keyAttack.isDown()) {
            dragTicks++;
        } else {
            dragging = false;
            Compat.showActionBar(mc.player, Component.translatable("creativeblueprint.selection.released", dragTicks));
        }
    }

    private static void cancel(Minecraft mc) {
        dragging = false;
        if (mc.player != null) {
            Compat.showActionBar(mc.player, Component.translatable("creativeblueprint.selection.cancelled"));
        }
    }

    private static boolean canStart(Minecraft mc) {
        return mc.player != null && mc.player.isCreative() && !Compat.isScreenOpen(mc) && Compat.isCtrlDown(mc);
    }
}
