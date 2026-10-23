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
 * The Ctrl + left mouse gesture: press sets corner 1, dragging moves corner 2 with the crosshair,
 * letting go keeps the box. While Ctrl is held in creative the attack key never reaches vanilla,
 * so nothing is broken or hit. Ctrl stays held for the whole drag: letting go of it cancels, as
 * do opening a screen (Esc) and leaving creative. After a cancel the left click stays ours until
 * it is released, so a still-held button doesn't start breaking blocks.
 */
public final class SelectionInput {
    /** How far the crosshair reaches when picking corners, in blocks. */
    public static final double REACH = 128;
    /** Where corner 1 goes when Ctrl-clicking at nothing (the sky). */
    private static final double AIR_CLICK_DISTANCE = 5;

    private static boolean dragging;
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
        if (!canStart(mc)) return false;
        Player player = mc.player;
        Vec3 eye = player.getEyePosition(1.0F);
        BlockPos hit = raycast(player, REACH);
        corner1 = hit != null ? hit : BlockPos.containing(eye.add(player.getViewVector(1.0F).scale(AIR_CLICK_DISTANCE)));
        corner2 = corner1;
        airDistance = Math.max(1, eye.distanceTo(Vec3.atCenterOf(corner1)));
        scrolledDistance = -1;
        dragging = true;
        showSize(mc, "creativeblueprint.selection.size");
        return true;
    }

    /** From the start of {@code Minecraft.continueAttack} while the attack key is held. True cancels it. */
    public static boolean blocksHeldAttack(Minecraft mc) {
        // Also covers pressing Ctrl in the middle of a normal hold, so creative's repeat-break stops
        return dragging || swallowAttack || canStart(mc);
    }

    /** From the start of {@code Minecraft.startUseItem} (right click). True cancels it. */
    public static boolean onUsePressed(Minecraft mc) {
        if (!dragging && corner1 != null && canStart(mc)) {
            cancel(mc, "creativeblueprint.selection.cleared");
            swallowUse = true;
        }
        return swallowUse;
    }

    /** From the start of {@code MouseHandler.onScroll}. True when the scroll is ours (no hotbar change). */
    public static boolean onScroll(Minecraft mc, double amount) {
        if (!dragging || amount == 0) return false;
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
            dragging = false;
            showSize(mc, "creativeblueprint.selection.selected");
        } else if (!Compat.isCtrlDown(mc)) {
            cancel(mc, "creativeblueprint.selection.cancelled");
        } else {
            showSize(mc, "creativeblueprint.selection.size");
        }
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
        return (dragging ? "dragging" : "selected") + " corner1=" + corner1.toShortString() + " corner2=" + corner2.toShortString()
            + (scrolledDistance >= 0 ? " scrolledDistance=" + scrolledDistance : "");
    }

    private static void showSize(Minecraft mc, String key) {
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

    private static boolean canStart(Minecraft mc) {
        return mc.player != null && mc.player.isCreative() && !Compat.isScreenOpen(mc) && Compat.isCtrlDown(mc);
    }
}
