package io.github.liangforstudy.creativeblueprint.loader;

//? if fabric {
import io.github.liangforstudy.creativeblueprint.LoaderAccess;
import net.fabricmc.loader.api.FabricLoader;

public final class FabricLoaderAccess implements LoaderAccess {
    @Override
    public String name() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }
}
//?}
