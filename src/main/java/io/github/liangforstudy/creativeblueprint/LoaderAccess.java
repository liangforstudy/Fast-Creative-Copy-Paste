package io.github.liangforstudy.creativeblueprint;

import io.github.liangforstudy.creativeblueprint.loader.*;

/** The few things that differ between mod loaders. */
public interface LoaderAccess {
    LoaderAccess INSTANCE = create();

    String name();

    boolean isModLoaded(String id);

    private static LoaderAccess create() {
        //? if fabric {
        return new FabricLoaderAccess();
        //?} elif neoforge {
        /*return new NeoForgeLoaderAccess();
        *///?} else {
        /*return new ForgeLoaderAccess();
        *///?}
    }
}
