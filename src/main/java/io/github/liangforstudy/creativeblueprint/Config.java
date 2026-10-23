package io.github.liangforstudy.creativeblueprint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The mod's settings, in {@code config/creativeblueprint.json}: which items work as the two tools,
 * like Factorio's blueprint and deconstruction planner. Holding the blueprint tool, a left drag
 * copies (no Ctrl needed); holding the deconstruction tool, it clears the box. Edited in
 * {@link SettingsScreen}.
 */
public final class Config {
    public static final String DEFAULT_BLUEPRINT_ITEM = "minecraft:stick";
    public static final String DEFAULT_DECONSTRUCT_ITEM = "minecraft:totem_of_undying";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Item ids, like {@code minecraft:stick}; empty turns that tool off. */
    private static String blueprintItem = DEFAULT_BLUEPRINT_ITEM;
    private static String deconstructItem = DEFAULT_DECONSTRUCT_ITEM;
    private static boolean loaded;

    private Config() {}

    public static String blueprintItem() {
        load();
        return blueprintItem;
    }

    public static String deconstructItem() {
        load();
        return deconstructItem;
    }

    /** Sets both tools (item ids, empty for none) and saves. */
    public static void setItems(String blueprint, String deconstruct) {
        load();
        blueprintItem = blueprint;
        deconstructItem = deconstruct;
        save();
    }

    private static Path file() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(CreativeBlueprint.MOD_ID + ".json");
    }

    /** Read on first use, once the game directory is known. */
    private static void load() {
        if (loaded) return;
        loaded = true;
        Path file = file();
        if (!Files.exists(file)) {
            save();
            return;
        }
        try {
            JsonObject json = GSON.fromJson(Files.readString(file), JsonObject.class);
            if (json.has("blueprintItem")) blueprintItem = json.get("blueprintItem").getAsString();
            if (json.has("deconstructItem")) deconstructItem = json.get("deconstructItem").getAsString();
        } catch (IOException | RuntimeException e) {
            CreativeBlueprint.LOGGER.warn("Could not read {}, using defaults", file, e);
        }
    }

    private static void save() {
        JsonObject json = new JsonObject();
        json.addProperty("blueprintItem", blueprintItem);
        json.addProperty("deconstructItem", deconstructItem);
        try {
            Files.createDirectories(file().getParent());
            Files.writeString(file(), GSON.toJson(json));
        } catch (IOException e) {
            CreativeBlueprint.LOGGER.warn("Could not write {}", file(), e);
        }
    }

    /** {@code id} with {@code minecraft:} added when it has no namespace, or null when no item has it. */
    public static String normalizeItem(String id) {
        String full = id.trim().contains(":") ? id.trim() : "minecraft:" + id.trim();
        for (var key : BuiltInRegistries.ITEM.keySet()) {
            if (key.toString().equals(full)) return full;
        }
        return null;
    }

    public static boolean isBlueprintTool(ItemStack stack) {
        return isItem(stack, blueprintItem());
    }

    public static boolean isDeconstructTool(ItemStack stack) {
        return isItem(stack, deconstructItem());
    }

    private static boolean isItem(ItemStack stack, String id) {
        return !id.isEmpty() && !stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(id);
    }
}
