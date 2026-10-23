package io.github.liangforstudy.creativeblueprint;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

/** Small vanilla calls whose shape differs between the Minecraft versions we build for. */
public final class Compat {
    private Compat() {}

    public static boolean isScreenOpen(Minecraft mc) {
        //? if >=26.2 {
        return mc.gui.screen() != null;
        //?} else {
        /*return mc.screen != null;
        *///?}
    }

    /** Either Ctrl key, read straight from the keyboard so a key mapping on Ctrl (sprint) doesn't hide it. */
    public static boolean isCtrlDown(Minecraft mc) {
        if (DevTools.ctrlDown) return true;
        //? if >=26.3 {
        return InputConstants.isKeyDown(InputConstants.KEY_LCONTROL) || InputConstants.isKeyDown(InputConstants.KEY_RCONTROL);
        //?} elif >=1.21.11 {
        /*var window = mc.getWindow();
        return InputConstants.isKeyDown(window, InputConstants.KEY_LCONTROL) || InputConstants.isKeyDown(window, InputConstants.KEY_RCONTROL);
        *///?} else {
        /*long window = mc.getWindow().getWindow();
        return InputConstants.isKeyDown(window, InputConstants.KEY_LCONTROL) || InputConstants.isKeyDown(window, InputConstants.KEY_RCONTROL);
        *///?}
    }

    /** The attack key (left mouse by default), or the dev client's virtual one. */
    public static boolean isAttackDown(Minecraft mc) {
        return mc.options.keyAttack.isDown() || DevTools.attackDown;
    }

    /** Text above the hotbar. */
    public static void showActionBar(LocalPlayer player, Component message) {
        //? if >=26.2 {
        player.sendOverlayMessage(message);
        //?} else {
        /*player.displayClientMessage(message, true);
        *///?}
    }
}
