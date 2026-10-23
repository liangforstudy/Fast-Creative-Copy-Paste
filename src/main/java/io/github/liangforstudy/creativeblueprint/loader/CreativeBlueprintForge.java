package io.github.liangforstudy.creativeblueprint.loader;

//? if forge {
/*import io.github.liangforstudy.creativeblueprint.CreativeBlueprint;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(CreativeBlueprint.MOD_ID)
public class CreativeBlueprintForge {
    public CreativeBlueprintForge() {
        // Forge 1.20.1 has no client-only @Mod, so a dedicated server just skips it
        if (FMLEnvironment.dist.isClient()) CreativeBlueprint.init();
    }
}
*///?}
