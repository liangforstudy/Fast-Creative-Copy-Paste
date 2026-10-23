package io.github.liangforstudy.creativeblueprint;

import net.minecraft.client.Minecraft;

/**
 * Test hook for the dev client: chat messages starting with {@code #cbtest} press and release a
 * virtual Ctrl and left mouse, so tools that can't hold real keys (MCPFabric) can run the gesture.
 * Each message logs the selection state. Only on when the JVM runs with
 * {@code -Dcreativeblueprint.devtools=true}, which only the dev run configs set; the message never
 * reaches the server.
 * <pre>
 * #cbtest press       Ctrl + left mouse down (starts a drag)
 * #cbtest release     both up (finishes the drag)
 * #cbtest ctrlup      Ctrl up, mouse still down (cancels)
 * #cbtest mouseup     left mouse up
 * #cbtest scroll [n]  scroll wheel, n steps (default 1, negative pulls nearer)
 * #cbtest status      log the state only
 * </pre>
 */
public final class DevTools {
    public static final boolean ENABLED = Boolean.getBoolean("creativeblueprint.devtools");
    private static final String PREFIX = "#cbtest";

    static boolean ctrlDown;
    static boolean attackDown;

    private DevTools() {}

    /** From the start of {@code ClientPacketListener.sendChat}. True when the message was ours. */
    public static boolean onChat(Minecraft mc, String message) {
        if (!ENABLED || !message.startsWith(PREFIX)) return false;
        String[] args = message.trim().split("\\s+");
        String action = args.length > 1 ? args[1] : "status";
        switch (action) {
            case "press" -> {
                ctrlDown = true;
                attackDown = true;
                SelectionInput.onAttackPressed(mc);
            }
            case "release" -> {
                ctrlDown = false;
                attackDown = false;
            }
            case "ctrlup" -> ctrlDown = false;
            case "mouseup" -> attackDown = false;
            case "scroll" -> {
                int steps = args.length > 2 ? Integer.parseInt(args[2]) : 1;
                for (int i = 0; i < Math.abs(steps); i++) SelectionInput.onScroll(mc, Math.signum(steps));
            }
            case "status" -> {}
            default -> CreativeBlueprint.LOGGER.warn("[cbtest] unknown action '{}'", action);
        }
        CreativeBlueprint.LOGGER.info("[cbtest] {}: {}", action, SelectionInput.describe());
        return true;
    }
}
