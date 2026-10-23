package io.github.liangforstudy.creativeblueprint;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;

/**
 * The mod's key mappings, rebindable under Controls → Key Binds. Each loader registers
 * {@link #ALL} its own way. Two defaults share a key with vanilla on purpose: Select with Sprint
 * (Left Ctrl) and Lock with Pick Block (middle click). 1.21.11+ and (Neo)Forge let several
 * mappings share a key; on older Fabric {@code KeyMappingMixin} makes that work too.
 */
public final class Keys {
    //? if >=1.21.11 {
    private static final net.minecraft.resources.Identifier CATEGORY_ID =
        net.minecraft.resources.Identifier.fromNamespaceAndPath(CreativeBlueprint.MOD_ID, "main");
    //? if neoforge {
    /*/^* NeoForge registers it with {@code RegisterKeyMappingsEvent.registerCategory}. ^/
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(CATEGORY_ID);
    *///?} else {
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(CATEGORY_ID);
    //?}
    //?} else {
    /*public static final String CATEGORY = "key.categories." + CreativeBlueprint.MOD_ID;
    *///?}

    /** Held for the whole selection drag (with the attack key). */
    public static final KeyMapping SELECT = key("select", InputConstants.KEY_LCONTROL);
    public static final KeyMapping ROTATE = key("rotate", InputConstants.KEY_R);
    public static final KeyMapping LOCK = new KeyMapping(name("lock"), InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_MIDDLE, CATEGORY);
    /** A second lock key, for mice and trackpads without a middle button. */
    public static final KeyMapping LOCK_ALT = key("lock_alt", InputConstants.KEY_H);
    public static final KeyMapping NUDGE_FORWARD = key("nudge_forward", InputConstants.KEY_UP);
    public static final KeyMapping NUDGE_BACK = key("nudge_back", InputConstants.KEY_DOWN);
    public static final KeyMapping NUDGE_LEFT = key("nudge_left", InputConstants.KEY_LEFT);
    public static final KeyMapping NUDGE_RIGHT = key("nudge_right", InputConstants.KEY_RIGHT);
    public static final KeyMapping NUDGE_UP = key("nudge_up", InputConstants.KEY_PAGEUP);
    public static final KeyMapping NUDGE_DOWN = key("nudge_down", InputConstants.KEY_PAGEDOWN);
    /** Second up / down keys, for keyboards without Page Up / Page Down. */
    public static final KeyMapping NUDGE_UP_ALT = key("nudge_up_alt", InputConstants.KEY_RBRACKET);
    public static final KeyMapping NUDGE_DOWN_ALT = key("nudge_down_alt", InputConstants.KEY_LBRACKET);

    public static final List<KeyMapping> ALL = List.of(SELECT, ROTATE, LOCK, LOCK_ALT,
        NUDGE_FORWARD, NUDGE_BACK, NUDGE_LEFT, NUDGE_RIGHT, NUDGE_UP, NUDGE_DOWN, NUDGE_UP_ALT, NUDGE_DOWN_ALT);

    private Keys() {}

    private static KeyMapping key(String id, int key) {
        return new KeyMapping(name(id), key, CATEGORY);
    }

    private static String name(String id) {
        return "key." + CreativeBlueprint.MOD_ID + "." + id;
    }

    /** Presses of {@code key} since the last call; 0 when it wasn't pressed. */
    public static int presses(KeyMapping key) {
        int n = 0;
        while (key.consumeClick()) n++;
        return n;
    }

    /** True when both mappings are bound to the same key. */
    public static boolean sameKey(KeyMapping a, KeyMapping b) {
        return a.saveString().equals(b.saveString());
    }

    /** The mod's settings screen (Mod Menu, the (Neo)Forge mods list). */
    public static Screen settingsScreen(Screen parent) {
        return new SettingsScreen(parent);
    }

    /** Vanilla's key binds screen, where the mod's keys are under their own heading. */
    public static Screen keyBindsScreen(Screen parent) {
        //? if >=1.21 {
        return new net.minecraft.client.gui.screens.options.controls.KeyBindsScreen(parent, Minecraft.getInstance().options);
        //?} else {
        /*return new net.minecraft.client.gui.screens.controls.KeyBindsScreen(parent, Minecraft.getInstance().options);
        *///?}
    }
}
