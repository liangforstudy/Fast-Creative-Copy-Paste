package io.github.liangforstudy.creativeblueprint;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

/**
 * Draws the selection box while dragging (corner A blue, corner B red) and the ghost of the held
 * copy. 1.21.11+ uses vanilla's gizmos; older versions draw from the debug renderer, which runs
 * every frame whether or not debug views are on. The ghost is see-through cubes in each block's
 * map colour.
 */
public final class SelectionRenderer {
    /** ARGB. */
    private static final int SELECTION_COLOR = 0xFFFFFFFF;
    /** A box the deconstruction tool will clear. */
    private static final int CLEAR_SELECTION_COLOR = 0xFFFF4040;
    /** Corner A (where the drag started) is blue, corner B (following the crosshair) red. */
    private static final int CORNER_A_COLOR = 0xFF3377FF;
    private static final int CORNER_B_COLOR = 0xFFFF3333;
    /** Alpha of the corner blocks' fill, as the top byte of an ARGB colour. */
    private static final int CORNER_ALPHA = 0x50000000;
    /** Corner blocks sit a little outside the selection outline so both stay visible. */
    private static final double CORNER_OUTSET = 0.02;
    private static final int GHOST_OUTLINE_COLOR = 0xFF55CCFF;
    /** Outline of a locked ghost (one that ignores the crosshair and is moved with the arrow keys). */
    private static final int LOCKED_GHOST_OUTLINE_COLOR = 0xFFFFAA33;
    /** Alpha of each ghost cube's fill, as the top byte of an ARGB colour. */
    private static final int GHOST_ALPHA = 0x70000000;
    /** Above this many blocks only the ghost's outline is drawn, to keep the frame rate up. */
    private static final int MAX_GHOST_CUBES = 20_000;
    /** Pushes outlines just outside the blocks so they don't flicker against their faces. */
    private static final double OUTSET = 0.002;
    /** Pulls ghost cubes just inside their block so they don't flicker against real blocks there. */
    private static final double CUBE_INSET = 0.01;

    private SelectionRenderer() {}

    private static AABB selectionBox() {
        SelectionInput.updateCorner2(Minecraft.getInstance());
        AABB box = SelectionInput.box();
        return box == null ? null : box.inflate(OUTSET);
    }

    private static AABB cornerBox(BlockPos pos) {
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1).inflate(CORNER_OUTSET);
    }

    private static AABB ghostBounds(BlockPos origin, Clipboard copy) {
        return new AABB(origin.getX(), origin.getY(), origin.getZ(),
            origin.getX() + copy.sizeX, origin.getY() + copy.sizeY, origin.getZ() + copy.sizeZ).inflate(OUTSET);
    }

    private static int selectionColor() {
        return SelectionInput.isClearing() ? CLEAR_SELECTION_COLOR : SELECTION_COLOR;
    }

    private static int ghostOutlineColor() {
        return Ghost.isLocked() ? LOCKED_GHOST_OUTLINE_COLOR : GHOST_OUTLINE_COLOR;
    }

    private static AABB ghostCube(BlockPos origin, Clipboard.Entry e) {
        double x = origin.getX() + e.x(), y = origin.getY() + e.y(), z = origin.getZ() + e.z();
        return new AABB(x, y, z, x + 1, y + 1, z + 1).deflate(CUBE_INSET);
    }

    //? if >=1.21.11 {
    /** From the start of {@code DebugRenderer.emitGizmos}, while vanilla collects this frame's gizmos. */
    public static void emitGizmos() {
        AABB box = selectionBox();
        if (box != null) {
            int color = selectionColor();
            int fill = (color & 0x00FFFFFF) | 0x20000000;
            net.minecraft.gizmos.Gizmos.cuboid(box, net.minecraft.gizmos.GizmoStyle.strokeAndFill(color, 2.0F, fill)).setAlwaysOnTop();
            emitCorner(SelectionInput.corner1(), CORNER_A_COLOR);
            emitCorner(SelectionInput.corner2(), CORNER_B_COLOR);
        }
        Minecraft mc = Minecraft.getInstance();
        BlockPos origin = Ghost.origin(mc);
        if (origin == null || SelectionInput.isDragging()) return;
        Clipboard copy = Ghost.held();
        net.minecraft.gizmos.Gizmos.cuboid(ghostBounds(origin, copy), net.minecraft.gizmos.GizmoStyle.stroke(ghostOutlineColor(), 2.0F));
        if (copy.entries.size() > MAX_GHOST_CUBES) return;
        for (Clipboard.Entry e : copy.entries) {
            net.minecraft.gizmos.Gizmos.cuboid(ghostCube(origin, e), net.minecraft.gizmos.GizmoStyle.fill(GHOST_ALPHA | e.color()));
        }
    }

    private static void emitCorner(BlockPos pos, int color) {
        int fill = (color & 0x00FFFFFF) | CORNER_ALPHA;
        net.minecraft.gizmos.Gizmos.cuboid(cornerBox(pos), net.minecraft.gizmos.GizmoStyle.strokeAndFill(color, 3.0F, fill)).setAlwaysOnTop();
    }
    //?} else {
    /*/^* From the start of {@code DebugRenderer.render}; the pose already has the camera's rotation. ^/
    public static void render(com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.MultiBufferSource.BufferSource buffers,
                              double camX, double camY, double camZ) {
        AABB box = selectionBox();
        if (box != null) {
            drawOutline(pose, buffers, box.move(-camX, -camY, -camZ), selectionColor());
            drawCorner(pose, buffers, cornerBox(SelectionInput.corner1()).move(-camX, -camY, -camZ), CORNER_A_COLOR);
            drawCorner(pose, buffers, cornerBox(SelectionInput.corner2()).move(-camX, -camY, -camZ), CORNER_B_COLOR);
        }
        Minecraft mc = Minecraft.getInstance();
        BlockPos origin = Ghost.origin(mc);
        if (origin == null || SelectionInput.isDragging()) return;
        Clipboard copy = Ghost.held();
        drawOutline(pose, buffers, ghostBounds(origin, copy).move(-camX, -camY, -camZ), ghostOutlineColor());
        if (copy.entries.size() > MAX_GHOST_CUBES) return;
        float a = (GHOST_ALPHA >>> 24) / 255F;
        for (Clipboard.Entry e : copy.entries) {
            int c = e.color();
            net.minecraft.client.renderer.debug.DebugRenderer.renderFilledBox(pose, buffers, ghostCube(origin, e).move(-camX, -camY, -camZ),
                (c >> 16 & 0xFF) / 255F, (c >> 8 & 0xFF) / 255F, (c & 0xFF) / 255F, a);
        }
        buffers.endBatch(net.minecraft.client.renderer.RenderType.debugFilledBox());
    }

    private static void drawOutline(com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.MultiBufferSource.BufferSource buffers,
                                    AABB box, int color) {
        float r = (color >> 16 & 0xFF) / 255F, g = (color >> 8 & 0xFF) / 255F, b = (color & 0xFF) / 255F;
        var lines = net.minecraft.client.renderer.RenderType.lines();
        net.minecraft.client.renderer.LevelRenderer.renderLineBox(pose, buffers.getBuffer(lines), box, r, g, b, 1.0F);
        buffers.endBatch(lines);
    }

    private static void drawCorner(com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.MultiBufferSource.BufferSource buffers,
                                   AABB box, int color) {
        float r = (color >> 16 & 0xFF) / 255F, g = (color >> 8 & 0xFF) / 255F, b = (color & 0xFF) / 255F;
        net.minecraft.client.renderer.debug.DebugRenderer.renderFilledBox(pose, buffers, box, r, g, b, (CORNER_ALPHA >>> 24) / 255F);
        buffers.endBatch(net.minecraft.client.renderer.RenderType.debugFilledBox());
        drawOutline(pose, buffers, box, color);
    }
    *///?}
}
