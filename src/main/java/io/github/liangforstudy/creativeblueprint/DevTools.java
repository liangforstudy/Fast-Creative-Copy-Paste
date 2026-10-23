package io.github.liangforstudy.creativeblueprint;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;

/**
 * Test hook for the dev client: chat messages starting with {@code #cbtest} press and release a
 * virtual Ctrl and left mouse, so tools that can't hold real keys (MCPFabric) can run the gesture.
 * Each message logs the selection state. Only on when the JVM runs with
 * {@code -Dcreativeblueprint.devtools=true}, which only the dev run configs set; the message never
 * reaches the server.
 * <pre>
 * #cbtest press       Ctrl + left mouse down (starts a drag)
 * #cbtest mousedown   left mouse down without Ctrl (a drag with a tool item in hand)
 * #cbtest release     both up (finishes the drag)
 * #cbtest ctrlup      Ctrl up, mouse still down (cancels)
 * #cbtest mouseup     left mouse up
 * #cbtest scroll [n]  scroll wheel, n steps (default 1, negative pulls nearer)
 * #cbtest rotate      R: turn the held copy a quarter clockwise
 * #cbtest rclick      right click: cancel the drag or the held copy
 * #cbtest lclick      left click without Ctrl: stamp the held copy
 * #cbtest lock        middle click: lock or unlock the held copy
 * #cbtest nudge d [n] move a locked copy n blocks (default 1); d is forward, back, left, right, up or down
 * #cbtest undo        Ctrl+Z key press (for the Ctrl-Z mod; 26.3 only)
 * #cbtest redo        Ctrl+Y key press (same)
 * #cbtest key k       press and release keyboard key k, by its name after "key.keyboard." (r, up,
 *                     page.up, left.control...), through the key mappings; keydown / keyup for one half
 * #cbtest settings    open the mod's settings screen (what Mod Menu's button opens)
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
            case "mousedown" -> {
                attackDown = true;
                SelectionInput.onAttackPressed(mc);
            }
            case "ctrlup" -> ctrlDown = false;
            case "mouseup" -> attackDown = false;
            case "scroll" -> {
                int steps = args.length > 2 ? Integer.parseInt(args[2]) : 1;
                for (int i = 0; i < Math.abs(steps); i++) SelectionInput.onScroll(mc, Math.signum(steps));
            }
            case "rotate" -> Ghost.rotate(mc, true);
            case "rclick" -> SelectionInput.onUsePressed(mc);
            case "lclick" -> SelectionInput.onAttackPressed(mc);
            case "lock" -> Ghost.toggleLock(mc);
            case "nudge" -> {
                String dir = args.length > 2 ? args[2] : "forward";
                int n = args.length > 3 ? Integer.parseInt(args[3]) : 1;
                for (int i = 0; i < n; i++) {
                    switch (dir) {
                        case "forward" -> Ghost.nudge(mc, 1, 0, 0);
                        case "back" -> Ghost.nudge(mc, -1, 0, 0);
                        case "right" -> Ghost.nudge(mc, 0, 1, 0);
                        case "left" -> Ghost.nudge(mc, 0, -1, 0);
                        case "up" -> Ghost.nudge(mc, 0, 0, 1);
                        case "down" -> Ghost.nudge(mc, 0, 0, -1);
                        default -> CreativeBlueprint.LOGGER.warn("[cbtest] unknown nudge direction '{}'", dir);
                    }
                }
            }
            case "undo" -> pressKey(mc, InputConstants.KEY_Z, InputConstants.MOD_CONTROL, true, true);
            case "redo" -> pressKey(mc, InputConstants.KEY_Y, InputConstants.MOD_CONTROL, true, true);
            case "key", "keydown", "keyup" -> {
                int key = InputConstants.getKey("key.keyboard." + args[2]).getValue();
                pressKey(mc, key, 0, !action.equals("keyup"), !action.equals("keydown"));
            }
            case "settings" -> mc.execute(() -> Compat.setScreen(mc, Keys.settingsScreen(null)));
            case "status" -> {}
            default -> CreativeBlueprint.LOGGER.warn("[cbtest] unknown action '{}'", action);
        }
        CreativeBlueprint.LOGGER.info("[cbtest] {}: {}; {}; {}", action, SelectionInput.describe(), Ghost.describe(mc), Stamp.describe());
        return true;
    }

    /** {@code key} pressed and/or released through the keyboard handler, as a real key press is. */
    private static void pressKey(Minecraft mc, int key, int modifiers, boolean press, boolean release) {
        //? if >=26.3 {
        var event = new net.minecraft.client.input.KeyEvent(key, 0, modifiers);
        if (press) mc.keyboardHandler.keyPress(mc.getWindow().handle(), InputConstants.PRESS, event);
        if (release) mc.keyboardHandler.keyPress(mc.getWindow().handle(), InputConstants.RELEASE, event);
        //?} else {
        /*CreativeBlueprint.LOGGER.warn("[cbtest] key presses are only wired up on 26.3");
        *///?}
    }
}
