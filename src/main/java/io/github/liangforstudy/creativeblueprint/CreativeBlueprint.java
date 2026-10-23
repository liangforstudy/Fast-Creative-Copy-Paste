package io.github.liangforstudy.creativeblueprint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Loader-independent entry point; each loader's entry class calls {@link #init()}. */
public final class CreativeBlueprint {
    public static final String MOD_ID = "creativeblueprint";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String VERSION = /*$ mod_version*/ "0.1.0";
    public static final String MINECRAFT = /*$ minecraft*/ "26.3";

    private CreativeBlueprint() {}

    public static void init() {
        LOGGER.info("Fast Creative Copy Paste {} for Minecraft {} on {}", VERSION, MINECRAFT, LoaderAccess.INSTANCE.name());
    }
}
