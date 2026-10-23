package io.github.liangforstudy.creativeblueprint;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;

/**
 * Draws the selection box. 1.21.11+ uses vanilla's gizmos (drawn through walls); older versions
 * draw lines from the debug renderer, which runs every frame whether or not debug views are on.
 */
public final class SelectionRenderer {
    /** White while dragging, green once let go. ARGB. */
    private static final int DRAGGING_COLOR = 0xFFFFFFFF;
    private static final int SELECTED_COLOR = 0xFF55FF55;
    /** Pushes the outline just outside the blocks so it doesn't flicker against their faces. */
    private static final double OUTSET = 0.002;

    private SelectionRenderer() {}

    private static AABB boxToDraw() {
        SelectionInput.updateCorner2(Minecraft.getInstance());
        AABB box = SelectionInput.box();
        return box == null ? null : box.inflate(OUTSET);
    }

    private static int color() {
        return SelectionInput.isDragging() ? DRAGGING_COLOR : SELECTED_COLOR;
    }

    //? if >=1.21.11 {
    /** From the start of {@code DebugRenderer.emitGizmos}, while vanilla collects this frame's gizmos. */
    public static void emitGizmos() {
        AABB box = boxToDraw();
        if (box == null) return;
        int color = color();
        int fill = (color & 0x00FFFFFF) | 0x20000000;
        net.minecraft.gizmos.Gizmos.cuboid(box, net.minecraft.gizmos.GizmoStyle.strokeAndFill(color, 2.0F, fill)).setAlwaysOnTop();
    }
    //?} else {
    /*/^* From the start of {@code DebugRenderer.render}; the pose already has the camera's rotation. ^/
    public static void render(com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.MultiBufferSource.BufferSource buffers,
                              double camX, double camY, double camZ) {
        AABB box = boxToDraw();
        if (box == null) return;
        int color = color();
        float r = (color >> 16 & 0xFF) / 255F, g = (color >> 8 & 0xFF) / 255F, b = (color & 0xFF) / 255F;
        var lines = net.minecraft.client.renderer.RenderType.lines();
        net.minecraft.client.renderer.LevelRenderer.renderLineBox(pose, buffers.getBuffer(lines), box.move(-camX, -camY, -camZ), r, g, b, 1.0F);
        buffers.endBatch(lines);
    }
    *///?}
}
