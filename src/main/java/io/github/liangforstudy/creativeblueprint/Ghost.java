package io.github.liangforstudy.creativeblueprint;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The copy you are holding: drawn at the crosshair, sitting on the face you look at and centred
 * on it. R or scrolling turns it a quarter (scroll down clockwise, scroll up counterclockwise);
 * right click cancels it. A left click stamps it (see {@link Stamp}) and keeps holding it, so
 * holding a copy never breaks blocks by accident.
 * <p>
 * Middle click (or H) locks it where it is, like Satisfactory's nudge mode: it stops following the
 * crosshair and the arrow keys move it a block at a time, relative to where you face; Page Up /
 * Page Down (or ] / [) move it up and down. Unlocking puts it back on the crosshair. All of these
 * are rebindable (see {@link Keys}).
 */
public final class Ghost {
    /** Where the ghost goes when the crosshair hits nothing, in blocks from the eye. */
    private static final double AIR_DISTANCE = 10;

    private static Clipboard held;
    /** Where the locked ghost's minimum corner is, or null while it follows the crosshair. */
    private static BlockPos locked;

    private Ghost() {}

    public static boolean isHeld() {
        return held != null;
    }

    public static boolean isLocked() {
        return held != null && locked != null;
    }

    public static Clipboard held() {
        return held;
    }

    public static void pickUp(Minecraft mc, Clipboard clipboard) {
        held = clipboard;
        locked = null;
        showHint(mc, "creativeblueprint.ghost.copied");
    }

    public static void cancel(Minecraft mc) {
        if (held == null) return;
        held = null;
        locked = null;
        if (mc.player != null) Compat.showActionBar(mc.player, Component.translatable("creativeblueprint.ghost.cancelled"));
    }

    public static void rotate(Minecraft mc, boolean clockwise) {
        if (held == null) return;
        Clipboard before = held;
        held = clockwise ? held.rotatedClockwise() : held.rotatedCounterclockwise();
        // Turn a locked ghost about its middle, as one on the crosshair is
        if (locked != null) {
            locked = locked.offset(before.sizeX / 2 - held.sizeX / 2, 0, before.sizeZ / 2 - held.sizeZ / 2);
        }
        showHint(mc, "creativeblueprint.ghost.rotated");
    }

    /** Locks the ghost where it is now, or puts it back on the crosshair. */
    public static void toggleLock(Minecraft mc) {
        if (held == null || mc.player == null) return;
        if (locked == null) {
            locked = origin(mc);
            Compat.showActionBar(mc.player, Component.translatable("creativeblueprint.ghost.locked"));
        } else {
            locked = null;
            Compat.showActionBar(mc.player, Component.translatable("creativeblueprint.ghost.unlocked"));
        }
    }

    /**
     * Moves a locked ghost, in blocks. {@code forward} and {@code right} are relative to the
     * horizontal direction the player faces most; {@code up} is straight up.
     */
    public static void nudge(Minecraft mc, int forward, int right, int up) {
        if (locked == null || mc.player == null) return;
        Direction facing = Direction.fromYRot(mc.player.getYRot());
        Direction side = facing.getClockWise();
        locked = locked.offset(
            facing.getStepX() * forward + side.getStepX() * right,
            up,
            facing.getStepZ() * forward + side.getStepZ() * right);
    }

    /**
     * From the start of {@code Minecraft.tick}, before vanilla handles key presses: the ghost's
     * keys (see {@link Keys}). Presses while no copy is held are dropped. When Lock shares its key
     * with Pick Block (middle click by default), a held copy takes that click from vanilla.
     */
    public static void beforeTick(Minecraft mc) {
        boolean active = canUseKeys(mc);
        int lock = Keys.presses(Keys.LOCK);
        if (active && Keys.sameKey(Keys.LOCK, mc.options.keyPickItem)) {
            // Both mappings see the same click; count it once
            lock = Math.max(lock, Keys.presses(mc.options.keyPickItem));
        }
        lock += Keys.presses(Keys.LOCK_ALT);
        int rotate = Keys.presses(Keys.ROTATE);
        int forward = Keys.presses(Keys.NUDGE_FORWARD) - Keys.presses(Keys.NUDGE_BACK);
        int right = Keys.presses(Keys.NUDGE_RIGHT) - Keys.presses(Keys.NUDGE_LEFT);
        int up = Keys.presses(Keys.NUDGE_UP) + Keys.presses(Keys.NUDGE_UP_ALT)
            - Keys.presses(Keys.NUDGE_DOWN) - Keys.presses(Keys.NUDGE_DOWN_ALT);
        if (!active) return;
        for (int i = 0; i < lock; i++) toggleLock(mc);
        for (int i = 0; i < rotate; i++) rotate(mc, true);
        if (forward != 0 || right != 0 || up != 0) nudge(mc, forward, right, up);
    }

    /** From the end of {@code Minecraft.tick}. */
    public static void tick(Minecraft mc) {
        if (mc.player == null) {
            held = null;
            locked = null;
        }
    }

    private static boolean canUseKeys(Minecraft mc) {
        return held != null && mc.player != null && mc.player.isCreative() && !Compat.isScreenOpen(mc);
    }

    /** The world position of the held copy's minimum corner this frame, or null when nothing is held. */
    public static BlockPos origin(Minecraft mc) {
        if (held == null || mc.player == null) return null;
        if (locked != null) return locked;
        BlockPos target = target(mc.player);
        return target.offset(-held.sizeX / 2, 0, -held.sizeZ / 2);
    }

    /** The block in front of the face the crosshair hits (where a placed block would go). */
    private static BlockPos target(Player player) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(SelectionInput.REACH));
        BlockHitResult hit = player.level().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit.getType() == HitResult.Type.BLOCK) return hit.getBlockPos().relative(hit.getDirection());
        return BlockPos.containing(eye.add(look.scale(AIR_DISTANCE)));
    }

    private static void showHint(Minecraft mc, String key) {
        if (mc.player == null) return;
        Compat.showActionBar(mc.player, Component.translatable(key, held.sizeX, held.sizeY, held.sizeZ, held.entries.size()));
    }

    /** One line of state for the dev client's test hook. */
    static String describe(Minecraft mc) {
        if (held == null) return "no ghost";
        return "ghost " + held.sizeX + "x" + held.sizeY + "x" + held.sizeZ + " blocks=" + held.entries.size()
            + " origin=" + origin(mc).toShortString() + (locked != null ? " locked" : "");
    }
}
