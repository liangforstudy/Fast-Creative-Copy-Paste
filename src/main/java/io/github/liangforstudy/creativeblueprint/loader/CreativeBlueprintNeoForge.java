package io.github.liangforstudy.creativeblueprint.loader;

//? if neoforge {
/*import io.github.liangforstudy.creativeblueprint.CreativeBlueprint;
import io.github.liangforstudy.creativeblueprint.Keys;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = CreativeBlueprint.MOD_ID, dist = Dist.CLIENT)
public class CreativeBlueprintNeoForge {
    public CreativeBlueprintNeoForge(IEventBus modBus, ModContainer container) {
        CreativeBlueprint.init();
        modBus.addListener(RegisterKeyMappingsEvent.class, event -> {
            //? if >=1.21.11 {
            event.registerCategory(Keys.CATEGORY);
            //?}
            Keys.ALL.forEach(event::register);
        });
        // The "Config" button in the mods list opens the key binds
        container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> Keys.settingsScreen(parent));
    }
}
*///?}
