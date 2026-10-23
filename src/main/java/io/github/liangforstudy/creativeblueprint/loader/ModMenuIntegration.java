package io.github.liangforstudy.creativeblueprint.loader;

//? if fabric {
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.liangforstudy.creativeblueprint.Keys;

/** Mod Menu's settings button (only loaded when Mod Menu is installed): opens the key binds. */
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return Keys::settingsScreen;
    }
}
//?}
