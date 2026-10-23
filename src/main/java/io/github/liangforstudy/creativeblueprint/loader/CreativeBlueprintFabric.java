package io.github.liangforstudy.creativeblueprint.loader;

//? if fabric {
import io.github.liangforstudy.creativeblueprint.CreativeBlueprint;
import io.github.liangforstudy.creativeblueprint.Keys;
import net.fabricmc.api.ClientModInitializer;

public class CreativeBlueprintFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CreativeBlueprint.init();
        //? if >=26.1 {
        Keys.ALL.forEach(net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper::registerKeyMapping);
        //?} else {
        /*Keys.ALL.forEach(net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper::registerKeyBinding);
        *///?}
    }
}
//?}
