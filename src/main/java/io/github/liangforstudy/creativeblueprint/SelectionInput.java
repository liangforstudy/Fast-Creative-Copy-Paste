package io.github.liangforstudy.creativeblueprint;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The selection drag: press sets corner 1, dragging moves corner 2 with the crosshair, letting go
 * acts on the box. It starts with Ctrl + left mouse, or a left drag while holding one of the tool
 * items (see {@link Config}): the blueprint tool or Ctrl copies the box (see {@link Ghost}), the
 * deconstruction tool clears it (see {@link Stamp#clear}). While a drag can start in creative the
 * attack key never reaches vanilla, so nothing is broken or hit. Whatever started the drag must
 * stay held: letting go of Ctrl (or switching away from the tool) cancels, as do right click,
 * opening a screen (Esc) and leaving creative. After a cancel the left click stays ours until it
 * is released, so a still-held button doesn't start breaking blocks.
 */
public final class SelectionInput {
    /** How far the crosshair reaches when picking corners, in blocks. */
    public static final double REACH = 128;
    /** Where corner 1 goes when Ctrl-clicking at nothing (the sky). */
    private static final double AIR_CLICK_DISTANCE = 5;

    /** What letting go does. */
    public enum Mode { COPY, CLEAR }

    private static boolean dragging;
    private static Mode mode = Mode.COPY;
    /** True when the drag was started by holding a tool item rather than Ctrl. */
    private static boolean byTool;
    private static BlockPos corner1;
    private static BlockPos corner2;
    /** Distance used when the crosshair hits nothing: as far as corner 1 was. */
    private static double airDistance;
    /** Set once the player scrolls: corner 2 then sits this far along the crosshair, ignoring blocks. */
    private static double scrolledDistance = -1;
    /** Set by a cancel: the rest of that left click must not break or hit anything. */
    private static boolean swallowAttack;
    /** Set by a Ctrl + right click clear: the rest of that right click must not place or use anything. */
    private static boolean swallowUse;

    private SelectionInput() {}

    public static boolean isDragging() {
        return dragging;
    }

    /** True while dragging a box the deconstruction tool will clear. */
    public static boolean isClearing() {
        return dragging && mode == Mode.CLEAR;
    }

    /** Corner A, where the drag started, or null when not dragging. */
    public static BlockPos corner1() {
        return corner1;
    }

    /** Corner B, the one following the crosshair, or null when not dragging. */
    public static BlockPos corner2() {
        return corner2;
    }

    /** The selected blocks as a box in world coordinates, or null when nothing is selected. */
    public static AABB box() {
        if (corner1 == null || corner2 == null) return null;
        return new AABB(
            Math.min(corner1.getX(), corner2.getX()), Math.min(corner1.getY(), corner2.getY()), Math.min(corner1.getZ(), corner2.getZ()),
            Math.max(corner1.getX(), corner2.getX()) + 1, Math.max(corner1.getY(), corner2.getY()) + 1, Math.max(corner1.getZ(), corner2.getZ()) + 1);
    }

    /** From the start of {@code Minecraft.startAttack}. True when the click is ours. */
    public static boolean onAttackPressed(Minecraft mc) {
        if (dragging || swallowAttack) return true;
        Mode start = startMode(mc);
        // A held copy takes plain clicks with the blueprint tool too; Ctrl or the deconstruction tool start a new drag
        if (holdsGhost(mc) && (start == null || start == Mode.COPY && !Compat.isSelectDown(mc))) {
            Stamp.stamp(mc, Ghost.held(), Ghost.origin(mc));
            return true;
        }
        if (start == null) return false;
        mode = start;
        byTool = start == Mode.CLEAR || !Compat.isSelectDown(mc);
        Player player = mc.player;
        Vec3 eye = player.getEyePosition(1.0F);
        BlockPos hit = raycast(player, REACH);
        corner1 = hit != null ? hit : BlockPos.containing(eye.add(player.getViewVector(1.0F).scale(AIR_CLICK_DISTANCE)));
        corner2 = corner1;
        airDistance = Math.max(1, eye.distanceTo(Vec3.atCenterOf(corner1)));
        scrolledDistance = -1;
        dragging = true;
        showSize(mc);
        return true;
    }

    /** From the start of {@code Minecraft.continueAttack} while the attack key is held. True cancels it. */
    public static boolean blocksHeldAttack(Minecraft mc) {
        // Also covers pressing Ctrl in the middle of a normal hold, so creative's repeat-break stops
        return dragging || swallowAttack || startMode(mc) != null || holdsGhost(mc);
    }

    /** From the start of {@code Minecraft.startUseItem} (right click). True cancels it. */
    public static boolean onUsePressed(Minecraft mc) {
        if (dragging) {
            cancel(mc, "creativeblueprint.selection.cancelled");
            swallowUse = true;
        } else if (holdsGhost(mc)) {
            Ghost.cancel(mc);
            swallowUse = true;
        }
        return swallowUse;
    }

    /** While a copy is held, the left click stamps it (see {@link Stamp}), so it never breaks blocks. */
    private static boolean holdsGhost(Minecraft mc) {
        return Ghost.isHeld() && mc.player != null && mc.player.isCreative() && !Compat.isScreenOpen(mc);
    }

    /**
     * From the start of {@code MouseHandler.onScroll}. True when the scroll is ours (no hotbar change):
     * while dragging it moves corner 2, while holding a copy it turns the copy (down is clockwise).
     */
    public static boolean onScroll(Minecraft mc, double amount) {
        if (amount == 0) return false;
        if (!dragging) {
            if (!holdsGhost(mc)) return false;
            Ghost.rotate(mc, amount < 0);
            return true;
        }
        if (scrolledDistance < 0) {
            scrolledDistance = mc.player.getEyePosition(1.0F).distanceTo(Vec3.atCenterOf(corner2));
        }
        scrolledDistance = Math.max(1, Math.min(REACH, Math.round(scrolledDistance) + Math.signum(amount)));
        updateCorner2(mc);
        return true;
    }

    /** Every frame while dragging, so the box follows the crosshair smoothly. */
    public static void updateCorner2(Minecraft mc) {
        if (!dragging || mc.player == null) return;
        Player player = mc.player;
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        if (scrolledDistance >= 0) {
            corner2 = BlockPos.containing(eye.add(look.scale(scrolledDistance)));
            return;
        }
        BlockPos hit = raycast(player, REACH);
        corner2 = hit != null ? hit : BlockPos.containing(eye.add(look.scale(airDistance)));
    }

    /** From the end of {@code Minecraft.tick}. */
    public static void tick(Minecraft mc) {
        if (swallowUse && !mc.options.keyUse.isDown()) swallowUse = false;
        if (swallowAttack && !Compat.isAttackDown(mc)) swallowAttack = false;
        if (mc.player == null) {
            dragging = false;
            corner1 = corner2 = null;
            return;
        }
        if (!dragging) return;
        if (!mc.player.isCreative() || Compat.isScreenOpen(mc)) {
            cancel(mc, "creativeblueprint.selection.cancelled");
            return;
        }
        updateCorner2(mc);
        // Mouse first: letting go of both in the same tick keeps the selection
        if (!Compat.isAttackDown(mc)) {
            if (mode == Mode.CLEAR) clearSelection(mc);
            else copySelection(mc);
        } else if (!stillHeld(mc)) {
            cancel(mc, "creativeblueprint.selection.cancelled");
        } else {
            showSize(mc);
        }
    }

    /** Whatever started the drag (Ctrl, or the tool in hand) is still held. */
    private static boolean stillHeld(Minecraft mc) {
        if (!byTool) return Compat.isSelectDown(mc);
        var hand = mc.player.getMainHandItem();
        return mode == Mode.CLEAR ? Config.isDeconstructTool(hand) : Config.isBlueprintTool(hand);
    }

    /** Letting go with the deconstruction tool: the box is cleared to air. */
    private static void clearSelection(Minecraft mc) {
        dragging = false;
        if (Clipboard.volume(corner1, corner2) > Clipboard.MAX_VOLUME) {
            corner1 = corner2 = null;
            Compat.showActionBar(mc.player, Component.translatable("creativeblueprint.selection.too_big", Clipboard.MAX_VOLUME));
            return;
        }
        BlockPos a = corner1, b = corner2;
        corner1 = corner2 = null;
        Stamp.clear(mc, a, b);
    }

    /** Letting go: the box becomes the held copy (the ghost) and the selection goes away. */
    private static void copySelection(Minecraft mc) {
        dragging = false;
        if (Clipboard.volume(corner1, corner2) > Clipboard.MAX_VOLUME) {
            corner1 = corner2 = null;
            Compat.showActionBar(mc.player, Component.translatable("creativeblueprint.selection.too_big", Clipboard.MAX_VOLUME));
            return;
        }
        Clipboard copy = Clipboard.copy(mc.player.level(), corner1, corner2);
        corner1 = corner2 = null;
        Ghost.pickUp(mc, copy);
    }

    /** The block the crosshair points at within {@code reach}, ignoring fluids, or null. */
    private static BlockPos raycast(Player player, double reach) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(reach));
        BlockHitResult hit = player.level().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.BLOCK ? hit.getBlockPos() : null;
    }

    /** One line of state for the dev client's test hook. */
    static String describe() {
        if (corner1 == null) return "no selection";
        return "dragging " + mode + (byTool ? " (tool)" : "") + " corner1=" + corner1.toShortString() + " corner2=" + corner2.toShortString()
            + (scrolledDistance >= 0 ? " scrolledDistance=" + scrolledDistance : "");
    }

    private static void showSize(Minecraft mc) {
        String key = mode == Mode.CLEAR ? "creativeblueprint.selection.clear_size" : "creativeblueprint.selection.size";
        int x = Math.abs(corner1.getX() - corner2.getX()) + 1;
        int y = Math.abs(corner1.getY() - corner2.getY()) + 1;
        int z = Math.abs(corner1.getZ() - corner2.getZ()) + 1;
        Compat.showActionBar(mc.player, Component.translatable(key, x, y, z, (long) x * y * z));
    }

    private static void cancel(Minecraft mc, String key) {
        if (dragging) swallowAttack = true;
        dragging = false;
        corner1 = corner2 = null;
        if (mc.player != null) Compat.showActionBar(mc.player, Component.translatable(key));
    }

    /**
     * What a left click would start now, or null for nothing: holding the deconstruction tool
     * clears, holding the blueprint tool or Ctrl copies. Creative only, with no screen open.
     */
    private static Mode startMode(Minecraft mc) {
        if (mc.player == null || !mc.player.isCreative() || Compat.isScreenOpen(mc)) return null;
        var hand = mc.player.getMainHandItem();
        if (Config.isDeconstructTool(hand)) return Mode.CLEAR;
        if (Compat.isSelectDown(mc) || Config.isBlueprintTool(hand)) return Mode.COPY;
        return null;
    }
}
