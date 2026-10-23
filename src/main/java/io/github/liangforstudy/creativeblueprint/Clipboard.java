package io.github.liangforstudy.creativeblueprint;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import java.util.ArrayList;
import java.util.List;

/**
 * A copied box of blocks, read from what the client has loaded. Only non-air blocks are kept,
 * relative to the box's minimum corner. Blocks in unloaded chunks read as air.
 */
public final class Clipboard {
    /** Largest box that can be copied, in blocks (air included). */
    public static final long MAX_VOLUME = 1_000_000;
    /** Ghost colour for blocks that have no map colour (glass, for one). RGB. */
    private static final int NO_MAP_COLOR = 0xC0C0C0;

    /** One copied block. {@code color} is its map colour, RGB, for drawing the ghost. */
    public record Entry(int x, int y, int z, BlockState state, int color) {}

    public final int sizeX, sizeY, sizeZ;
    public final List<Entry> entries;

    private Clipboard(int sizeX, int sizeY, int sizeZ, List<Entry> entries) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.entries = entries;
    }

    public static long volume(BlockPos a, BlockPos b) {
        return (long) (Math.abs(a.getX() - b.getX()) + 1) * (Math.abs(a.getY() - b.getY()) + 1) * (Math.abs(a.getZ() - b.getZ()) + 1);
    }

    /** Copies the box between two corners (inclusive). Check {@link #volume} against {@link #MAX_VOLUME} first. */
    public static Clipboard copy(Level level, BlockPos a, BlockPos b) {
        BlockPos min = new BlockPos(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()));
        BlockPos max = new BlockPos(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
        List<Entry> entries = new ArrayList<>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = min.getY(); y <= max.getY(); y++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                for (int x = min.getX(); x <= max.getX(); x++) {
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) continue;
                    MapColor mapColor = state.getMapColor(level, pos);
                    int color = mapColor == MapColor.NONE ? NO_MAP_COLOR : mapColor.col;
                    entries.add(new Entry(x - min.getX(), y - min.getY(), z - min.getZ(), state, color));
                }
            }
        }
        return new Clipboard(max.getX() - min.getX() + 1, max.getY() - min.getY() + 1, max.getZ() - min.getZ() + 1, entries);
    }

    /** This copy turned a quarter clockwise (seen from above), block states included. */
    public Clipboard rotatedClockwise() {
        List<Entry> rotated = new ArrayList<>(entries.size());
        for (Entry e : entries) {
            rotated.add(new Entry(sizeZ - 1 - e.z(), e.y(), e.x(), e.state().rotate(Rotation.CLOCKWISE_90), e.color()));
        }
        return new Clipboard(sizeZ, sizeY, sizeX, rotated);
    }

    /** This copy turned a quarter counterclockwise (seen from above), block states included. */
    public Clipboard rotatedCounterclockwise() {
        List<Entry> rotated = new ArrayList<>(entries.size());
        for (Entry e : entries) {
            rotated.add(new Entry(e.z(), e.y(), sizeX - 1 - e.x(), e.state().rotate(Rotation.COUNTERCLOCKWISE_90), e.color()));
        }
        return new Clipboard(sizeZ, sizeY, sizeX, rotated);
    }
}
