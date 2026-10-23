package io.github.liangforstudy.creativeblueprint;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The mod's settings (Mod Menu, the (Neo)Forge mods list): the two tool items, typed as item ids,
 * and a button to the key binds. Only built from widgets, so it needs no drawing code of its own.
 */
public final class SettingsScreen extends Screen {
    private static final int WIDTH = 220;
    /** ARGB. */
    private static final int TEXT_COLOR = 0xFFE0E0E0;
    private static final int BAD_COLOR = 0xFFFF5555;

    private final Screen parent;
    private EditBox blueprint;
    private EditBox deconstruct;
    private Button done;

    public SettingsScreen(Screen parent) {
        super(Component.translatable("creativeblueprint.settings.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = width / 2 - WIDTH / 2;
        int y = height / 4;
        int titleWidth = font.width(title);
        addRenderableWidget(new StringWidget(width / 2 - titleWidth / 2, y - 24, titleWidth, 12, title, font));

        addRenderableWidget(new StringWidget(x, y, WIDTH, 12, Component.translatable("creativeblueprint.settings.blueprint_item"), font));
        blueprint = itemBox(x, y + 14, Config.blueprintItem());
        addRenderableWidget(new StringWidget(x, y + 44, WIDTH, 12, Component.translatable("creativeblueprint.settings.deconstruct_item"), font));
        deconstruct = itemBox(x, y + 58, Config.deconstructItem());
        addRenderableWidget(new StringWidget(x, y + 82, WIDTH, 12, Component.translatable("creativeblueprint.settings.item_hint"), font));

        addRenderableWidget(Button.builder(Component.translatable("creativeblueprint.settings.key_binds"),
            b -> Compat.setScreen(minecraft, Keys.keyBindsScreen(this))).bounds(x, y + 104, WIDTH, 20).build());
        done = addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
            .bounds(x, y + 130, WIDTH, 20).build());
        validate();
    }

    private EditBox itemBox(int x, int y, String value) {
        EditBox box = new EditBox(font, x, y, WIDTH, 20, Component.empty());
        box.setMaxLength(128);
        box.setValue(value);
        box.setResponder(text -> validate());
        return addRenderableWidget(box);
    }

    /** Colours a box red when no item has that id; Done only works when both are fine (or empty). */
    private void validate() {
        if (done == null) return;
        boolean ok = true;
        for (EditBox box : new EditBox[] {blueprint, deconstruct}) {
            boolean valid = box.getValue().isBlank() || Config.normalizeItem(box.getValue()) != null;
            box.setTextColor(valid ? TEXT_COLOR : BAD_COLOR);
            ok &= valid;
        }
        done.active = ok;
    }

    @Override
    public void onClose() {
        if (done.active) Config.setItems(itemOrEmpty(blueprint), itemOrEmpty(deconstruct));
        Compat.setScreen(minecraft, parent);
    }

    private static String itemOrEmpty(EditBox box) {
        return box.getValue().isBlank() ? "" : Config.normalizeItem(box.getValue());
    }
}
