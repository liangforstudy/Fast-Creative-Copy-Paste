# Fast Creative Copy Paste: plan

A client-side mod for fast, Factorio-style copy and paste in creative mode.

## The gesture

1. **Ctrl + drag** (left mouse) in creative: draw a box. Corner 2 follows the crosshair with a
   long raycast (about 128 blocks). The vanilla block break is cancelled while Ctrl is held.
   Letting go of Ctrl before the mouse cancels the drag; the held left click then does nothing
   until it is released.
2. **Release**: the box is copied at once. A ghost of it sticks to the crosshair.
3. **Click**: place a **blueprint** there. The ghost stays, so you can keep stamping.
4. **R** rotates, **Esc** / right click drops the ghost.

Works with anything in hand; Ctrl is the switch. The modifier is a rebindable keybind.

## What happens to a placed blueprint

- **Op (permission level 2+ and `fill`/`setblock` in the command tree):** "construction bots".
  The blueprint is built with `/fill` and `/setblock`, throttled.
- **Not op:** it stays as a hologram you build by hand.
  - Missing blocks: translucent ghost. Right block placed: ghost disappears. Wrong block: red.
  - Middle-click a ghost: that block goes into your hand.
  - **Ignore:** press **I** on a wrong block (or Ctrl+drag in ignore mode) to mark it "ignore for
    now". Toggle to show ignored blocks dimmed. Saved with the blueprint.
  - Blueprints are saved locally, per server and world.
  - If you are made op later, "send the bots" to finish a blueprint placed earlier.

## Saving schematics

**Ctrl+S** while holding a copy saves it as a `.litematic` in Litematica's `schematics` folder.
Written by this mod, so Litematica is not needed (and NeoForge builds can export too). Vanilla
`.nbt` and `.schem` may come later.

## Block data (chests, signs)

- Signs, banners and heads: the client already has them.
- Chest contents:
  - **Op:** asked from the server with the vanilla block data query (the one F3+I uses).
  - **Not op:** taken from Chest_Tracker (optional dependency) when it is installed.
- Pasting data as op: `/setblock ... {data}`, split into extra commands when a command would go
  over 256 characters.

## Hard rule: no packets to servers where you are not op

Nothing is sent to the server (commands, data queries) unless you have permission level 2+ **and**
the needed commands are in the command tree. Checked again before every batch; a de-op drops the
rest of the queue. Copying, ghosts, holograms and schematic export only read what the client
already has.

## Dependencies

- **Chest_Tracker** (`entitytracker`): optional. Declared under `suggests`, compiled against but
  not shipped, used from one class only after `isModLoaded`. Fabric 1.21.11 only for now.
  Chest_Tracker needs a small public lookup method for this.
- A shared library, if one is ever made, is bundled inside the jar (jar-in-jar), never a separate
  install.

## Versions

| Minecraft | Fabric | NeoForge / Forge | Java |
|-----------|--------|------------------|------|
| 1.20.1    | yes    | **Forge** (NeoForge barely existed then) | 17 |
| 1.21.1    | yes    | NeoForge | 21 |
| 1.21.11   | yes    | NeoForge | 21 |
| 26.2      | yes    | NeoForge | 25 |
| 26.3      | yes    | NeoForge | 25 |

Mojang mappings everywhere. Built with Stonecutter 0.9 from the official multi-loader template.

## Phases

0. **Scaffold:** every version and loader builds and launches an empty client mod.
1. **Input:** Ctrl + left mouse caught, block breaking cancelled, press / drag / release tracked.
2. **Selection:** long raycast for corner 2, scroll to push it nearer or further, box outline and
   size on the HUD.
3. **Copy and ghost:** read the blocks into a copy on release, render the ghost at the crosshair,
   rotate.
4. **Blueprints:** place, hologram rendering (missing / wrong / done), ignore marks, pick block,
   saving per server.
5. **Bots:** op detection, throttled `/fill` and `/setblock` queue.
6. **Schematics:** `.litematic` export.
7. **Block data:** server query as op, Chest_Tracker lookup, data in paste commands.

## Open questions (for later)

- Tool vs bare hands: currently anything in hand, with Ctrl as the switch.
- Mod id: `creativeblueprint` is a leftover from the first working name; changing it later means renaming the package and resource folders too.
