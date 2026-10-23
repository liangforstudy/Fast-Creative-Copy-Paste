package io.github.liangforstudy.creativeblueprint;

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

    public static void setScreen(Minecraft mc, net.minecraft.client.gui.screens.Screen screen) {
        //? if >=26.2 {
        mc.gui.setScreen(screen);
        //?} else {
        /*mc.setScreen(screen);
        *///?}
    }

    /** The Select key (Left Ctrl by default), or the dev client's virtual Ctrl. */
    public static boolean isSelectDown(Minecraft mc) {
        return Keys.SELECT.isDown() || DevTools.ctrlDown;
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
