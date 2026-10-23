package io.github.liangforstudy.creativeblueprint;

import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Pastes the held copy with {@code /fill} and {@code /setblock}, like {@code /clone ... masked}:
 * air in the copy is skipped, so what is already there stays. Only where the player may run both
 * commands (op, or cheats on in singleplayer), checked from the command tree the server sent. In
 * singleplayer the commands run
 * straight on the integrated server, wrapped in one Ctrl-Z step when that mod is loaded, so one
 * Ctrl+Z undoes the whole stamp. On a server they are sent as chat commands, a few per tick.
 */
public final class Stamp {
    /** Vanilla's default {@code commandModificationBlockLimit}: the most blocks one /fill may change. */
    private static final int MAX_FILL_VOLUME = 32_768;
    /** Side of the cubes a clear is split into: 32³ is exactly {@link #MAX_FILL_VOLUME}. */
    private static final int CLEAR_CHUNK = 32;
    /** Commands sent to a remote server per client tick. Ops are not kicked for command spam. */
    private static final int COMMANDS_PER_TICK = 4;

    private static final ArrayDeque<String> queue = new ArrayDeque<>();

    private Stamp() {}

    /** Pastes {@code copy} with its minimum corner at {@code origin}. */
    public static void stamp(Minecraft mc, Clipboard copy, BlockPos origin) {
        if (!canRunCommands(mc)) {
            Compat.showActionBar(mc.player, Component.translatable("creativeblueprint.stamp.no_permission"));
            return;
        }
        List<String> commands = commands(mc.player.level(), copy, origin);
        if (commands.isEmpty()) {
            Compat.showActionBar(mc.player, Component.translatable("creativeblueprint.stamp.nothing"));
            return;
        }
        send(mc, commands);
        Compat.showActionBar(mc.player, Component.translatable("creativeblueprint.stamp.done",
            copy.sizeX, copy.sizeY, copy.sizeZ, commands.size()));
    }

    /**
     * Clears the box between two corners (inclusive) to air, the deconstruction tool's job. Top
     * layer first, so sand and gravel don't fall into what is being cleared; parts the client
     * already sees as empty are left out. One Ctrl-Z step in singleplayer, like a stamp.
     */
    public static void clear(Minecraft mc, BlockPos a, BlockPos b) {
        if (!canRunCommands(mc)) {
            Compat.showActionBar(mc.player, Component.translatable("creativeblueprint.stamp.no_permission"));
            return;
        }
        BlockPos min = new BlockPos(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()));
        BlockPos max = new BlockPos(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
        List<String> commands = new ArrayList<>();
        // Cubes of 32 blocks a side stay within one /fill's limit
        for (int y = max.getY(); y >= min.getY(); y -= CLEAR_CHUNK) {
            int y1 = Math.max(min.getY(), y - CLEAR_CHUNK + 1);
            for (int z = min.getZ(); z <= max.getZ(); z += CLEAR_CHUNK) {
                int z2 = Math.min(max.getZ(), z + CLEAR_CHUNK - 1);
                for (int x = min.getX(); x <= max.getX(); x += CLEAR_CHUNK) {
                    int x2 = Math.min(max.getX(), x + CLEAR_CHUNK - 1);
                    if (isEmpty(mc.player.level(), x, y1, z, x2, y, z2)) continue;
                    commands.add("fill " + x + " " + y1 + " " + z + " " + x2 + " " + y + " " + z2 + " air");
                }
            }
        }
        if (commands.isEmpty()) {
            Compat.showActionBar(mc.player, Component.translatable("creativeblueprint.clear.nothing"));
            return;
        }
        send(mc, commands);
        Compat.showActionBar(mc.player, Component.translatable("creativeblueprint.clear.done",
            max.getX() - min.getX() + 1, max.getY() - min.getY() + 1, max.getZ() - min.getZ() + 1, commands.size()));
    }

    private static boolean isEmpty(Level level, int x1, int y1, int z1, int x2, int y2, int z2) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = y1; y <= y2; y++)
            for (int z = z1; z <= z2; z++)
                for (int x = x1; x <= x2; x++) {
                    if (!level.getBlockState(pos.set(x, y, z)).isAir()) return false;
                }
        return true;
    }

    /** Runs {@code commands} on the integrated server, or queues them for a remote one. */
    private static void send(Minecraft mc, List<String> commands) {
        IntegratedServer server = mc.getSingleplayerServer();
        if (server != null) {
            UUID player = mc.player.getUUID();
            server.execute(() -> runOnServer(server, player, commands));
        } else {
            queue.addAll(commands);
        }
    }

    /** From the end of {@code Minecraft.tick}: sends queued commands to a remote server. */
    public static void tick(Minecraft mc) {
        if (queue.isEmpty()) return;
        if (mc.player == null) {
            queue.clear();
            return;
        }
        // Checked again before every batch: losing op drops the rest
        if (!canRunCommands(mc)) {
            queue.clear();
            Compat.showActionBar(mc.player, Component.translatable("creativeblueprint.stamp.stopped"));
            return;
        }
        ClientPacketListener connection = mc.getConnection();
        for (int i = 0; i < COMMANDS_PER_TICK && !queue.isEmpty(); i++) connection.sendCommand(queue.poll());
    }

    /** True when the server lets this player run /fill and /setblock (it only sends commands they may use). */
    static boolean canRunCommands(Minecraft mc) {
        ClientPacketListener connection = mc.getConnection();
        if (connection == null) return false;
        RootCommandNode<?> root = connection.getCommands().getRoot();
        return root.getChild("fill") != null && root.getChild("setblock") != null;
    }

    private static void runOnServer(IntegratedServer server, UUID playerId, List<String> commands) {
        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        if (player == null) return;
        CommandSourceStack source = player.createCommandSourceStack().withSuppressedOutput();
        CtrlZ.begin();
        try {
            for (String command : commands) server.getCommands().performPrefixedCommand(source, command);
        } finally {
            CtrlZ.end();
        }
    }

    /**
     * The commands that turn the target box into the copy, bottom layer first so blocks have
     * something to stand on. Neighbouring blocks of one state are merged into boxes for /fill.
     * Air in the copy and blocks the client already sees as right are left out.
     */
    static List<String> commands(Level level, Clipboard copy, BlockPos origin) {
        int sx = copy.sizeX, sy = copy.sizeY, sz = copy.sizeZ;
        // Null where the copy has air: those cells are never touched
        BlockState[] cells = new BlockState[sx * sy * sz];
        for (Clipboard.Entry e : copy.entries) cells[index(copy, e.x(), e.y(), e.z())] = e.state();

        boolean[] done = new boolean[cells.length];
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = 0; y < sy; y++) {
            for (int z = 0; z < sz; z++) {
                for (int x = 0; x < sx; x++) {
                    int i = index(copy, x, y, z);
                    pos.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (cells[i] == null || level.getBlockState(pos) == cells[i]) done[i] = true;
                }
            }
        }

        List<String> commands = new ArrayList<>();
        for (int y = 0; y < sy; y++) {
            for (int z = 0; z < sz; z++) {
                for (int x = 0; x < sx; x++) {
                    if (done[index(copy, x, y, z)]) continue;
                    BlockState state = cells[index(copy, x, y, z)];
                    // Grow a box from here: along x, then z, then y, while it stays one state
                    int x2 = x, z2 = z, y2 = y;
                    while (x2 + 1 < sx && fits(copy, cells, done, state, x2 + 1, x2 + 1, y, y, z, z)) x2++;
                    while (z2 + 1 < sz && volume(x, x2, y, y2, z, z2 + 1) <= MAX_FILL_VOLUME
                        && fits(copy, cells, done, state, x, x2, y, y, z2 + 1, z2 + 1)) z2++;
                    while (y2 + 1 < sy && volume(x, x2, y, y2 + 1, z, z2) <= MAX_FILL_VOLUME
                        && fits(copy, cells, done, state, x, x2, y2 + 1, y2 + 1, z, z2)) y2++;
                    for (int by = y; by <= y2; by++)
                        for (int bz = z; bz <= z2; bz++)
                            for (int bx = x; bx <= x2; bx++) done[index(copy, bx, by, bz)] = true;
                    commands.add(command(origin, state, x, y, z, x2, y2, z2));
                }
            }
        }
        return commands;
    }

    private static String command(BlockPos origin, BlockState state, int x1, int y1, int z1, int x2, int y2, int z2) {
        String block = BlockStateParser.serialize(state);
        int ox = origin.getX(), oy = origin.getY(), oz = origin.getZ();
        if (x1 == x2 && y1 == y2 && z1 == z2) return "setblock " + (ox + x1) + " " + (oy + y1) + " " + (oz + z1) + " " + block;
        return "fill " + (ox + x1) + " " + (oy + y1) + " " + (oz + z1) + " " + (ox + x2) + " " + (oy + y2) + " " + (oz + z2) + " " + block;
    }

    /** True when every cell in the box is {@code state} and not placed yet. */
    private static boolean fits(Clipboard copy, BlockState[] cells, boolean[] done, BlockState state,
                                int x1, int x2, int y1, int y2, int z1, int z2) {
        for (int y = y1; y <= y2; y++)
            for (int z = z1; z <= z2; z++)
                for (int x = x1; x <= x2; x++) {
                    int i = index(copy, x, y, z);
                    if (done[i] || cells[i] != state) return false;
                }
        return true;
    }

    private static long volume(int x1, int x2, int y1, int y2, int z1, int z2) {
        return (long) (x2 - x1 + 1) * (y2 - y1 + 1) * (z2 - z1 + 1);
    }

    private static int index(Clipboard copy, int x, int y, int z) {
        return (y * copy.sizeZ + z) * copy.sizeX + x;
    }

    /** One line of state for the dev client's test hook. */
    static String describe() {
        return queue.isEmpty() ? "no stamp queued" : queue.size() + " commands queued";
    }

    /**
     * The Ctrl-Z mod (optional), reached by reflection so it is not needed to build or run. Its
     * history nests: a step opened here takes in the steps each command opens, so the whole stamp
     * is one undo. Only on the integrated server, where its history lives in this JVM.
     */
    private static final class CtrlZ {
        private static final Method BEGIN, END;
        private static final Object COMMAND_CAUSE;

        static {
            Method begin = null, end = null;
            Object cause = null;
            try {
                Class<?> history = Class.forName("com.choculaterie.history.BlockHistory");
                Class<?> causeClass = Class.forName("com.choculaterie.history.Cause");
                for (Object constant : causeClass.getEnumConstants()) {
                    if (((Enum<?>) constant).name().equals("COMMAND")) cause = constant;
                }
                begin = history.getMethod("begin", causeClass);
                end = history.getMethod("end");
            } catch (ReflectiveOperationException | LinkageError e) {
                begin = end = null;
            }
            BEGIN = cause != null ? begin : null;
            END = cause != null ? end : null;
            COMMAND_CAUSE = cause;
        }

        static void begin() {
            call(BEGIN, COMMAND_CAUSE);
        }

        static void end() {
            call(END);
        }

        private static void call(Method method, Object... args) {
            if (method == null) return;
            try {
                method.invoke(null, args);
            } catch (ReflectiveOperationException e) {
                CreativeBlueprint.LOGGER.warn("Ctrl-Z call {} failed", method.getName(), e);
            }
        }
    }
}
