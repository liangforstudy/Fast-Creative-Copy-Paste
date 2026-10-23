package io.github.liangforstudy.creativeblueprint.loader;

//? if fabric {
import io.github.liangforstudy.creativeblueprint.CreativeBlueprint;
import net.fabricmc.api.ClientModInitializer;

public class CreativeBlueprintFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CreativeBlueprint.init();
    }
}
//?}
