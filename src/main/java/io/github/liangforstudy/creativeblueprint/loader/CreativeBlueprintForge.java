package io.github.liangforstudy.creativeblueprint.loader;

//? if forge {
/*import io.github.liangforstudy.creativeblueprint.CreativeBlueprint;
import io.github.liangforstudy.creativeblueprint.Keys;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(CreativeBlueprint.MOD_ID)
public class CreativeBlueprintForge {
    public CreativeBlueprintForge() {
        // Forge 1.20.1 has no client-only @Mod, so a dedicated server just skips it
        if (!FMLEnvironment.dist.isClient()) return;
        CreativeBlueprint.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener((RegisterKeyMappingsEvent event) -> Keys.ALL.forEach(event::register));
        // The "Config" button in the mods list opens the key binds
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> Keys.settingsScreen(parent)));
    }
}
*///?}
