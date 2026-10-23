# Fast Creative Copy Paste: plan

A client-side mod for fast, Factorio-style copy and paste in creative mode.

## The gesture

1. **Ctrl + drag** (left mouse) in creative: draw a box. Corner A (blue) is where the drag
   started; corner B (red) follows the crosshair with a long raycast (about 128 blocks). Scrolling
   pushes corner B further or pulls it nearer. The size shows on the action bar. The vanilla block
   break is cancelled while Ctrl is held.
2. **Cancel a drag:** let go of Ctrl before the mouse, or right click. The held left click then
   does nothing until it is released.
3. **Release**: the box is copied at once (clipboard only, nothing is saved). A ghost of it,
   see-through cubes in each block's map colour with a light-blue outline, sits on the face the
   crosshair points at.
4. **Click**: stamp the copy there (see below). The ghost stays, so you can keep stamping.
5. **R** or **scroll** turns the ghost a quarter (scroll down clockwise, scroll up
   counterclockwise). **Right click** cancels it ("Copy cancelled").
6. **Middle click** (or **H**) locks the ghost in place, like Satisfactory's nudge mode: its
   outline turns orange and it stops following the crosshair. The **arrow keys** move it a block at
   a time, relative to the compass direction you face most; **Page Up / Page Down** (or **]** /
   **[**) move it up and down. Rotating turns it about its middle. A stamp keeps it locked, so you
   can nudge and stamp again next to it. Middle click again unlocks it and forgets the nudges.

Ctrl + drag works with anything in hand. Two tool items, like Factorio's planners, start a drag
without Ctrl:

- **Blueprint tool** (stick by default): a left drag copies, as Ctrl + drag does. While a copy is
  held, a plain click with it stamps; Ctrl + drag starts a new copy.
- **Deconstruction tool** (totem of undying by default): a left drag draws a red box, and letting
  go clears it to air (one Ctrl-Z step in singleplayer). This is how to carve: stamps never dig.

A drag started by a tool is cancelled when the tool leaves your hand, one started by Ctrl when
Ctrl is let go.

## Settings and keys

The mod's settings button (Mod Menu on Fabric, the mods list's Config button on NeoForge / Forge)
opens a small screen: the two tool items as item ids (empty turns a tool off), saved in
`config/creativeblueprint.json`, and a button to the key binds.

Every key above is a key mapping, rebindable under Options → Controls → Key Binds in the
"Fast Creative Copy Paste" section. Scrolling, left and right click are not mappings: they follow
vanilla's attack and use keys.

Two defaults share a key with vanilla on purpose: Select with Sprint (Left Ctrl) and Lock with
Pick Block (middle click; a held copy takes that click from vanilla). 1.21.11+ and (Neo)Forge let
several mappings share a key. On Fabric 1.20.1 and 1.21.1 vanilla sends each key to one mapping
only, so `KeyMappingMixin` passes presses on to every mapping bound to that key there.

## Stamping

- **Allowed when** the server lets you use `/fill` and `/setblock`, read from the command tree it
  sent (op, or cheats on in singleplayer). Otherwise the action bar says so and nothing is sent.
- **Commands** are built from the copy, bottom layer first: neighbouring blocks of one state are
  merged into `/fill` boxes (at most 32,768 blocks, vanilla's limit), single blocks use
  `/setblock`. Air in the copy is skipped (like `/clone ... masked`), so a stamp never digs into
  what is already there. Blocks the client already sees as right are skipped too.
- **Singleplayer:** the commands run straight on the integrated server, output suppressed.
- **On a server:** sent as chat commands, 4 per tick. Losing the permission drops the rest.
- **Clearing** (deconstruction tool) is `/fill ... air` in cubes of 32 blocks a side, top layer
  first so sand and gravel don't fall in; cubes the client sees as empty are skipped. Same
  permission check, sending and undo as a stamp.
- **Undo:** with Ctrl-Z in singleplayer, the whole stamp is wrapped in one Ctrl-Z step
  (`BlockHistory.begin` / `end` by reflection; Ctrl-Z nests steps). On a server with Ctrl-Z each
  command is its own step, unless Ctrl-Z gets a way for a client to group commands (ask upstream).
  Without Ctrl-Z there is no undo yet.

## Not op: holograms (later)

- A stamp you can't place stays as a hologram you build by hand.
  - Missing blocks: translucent ghost. Right block placed: ghost disappears. Wrong block: red.
  - Middle-click a ghost: that block goes into your hand.
  - **Ignore:** press **I** on a wrong block (or Ctrl+drag in ignore mode) to mark it "ignore for
    now". Toggle to show ignored blocks dimmed.
  - If you are made op later, "send the bots" to finish a hologram placed earlier.
- Saving holograms per server and world is on hold: for now the mod has a clipboard only.

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

Nothing is sent to the server (commands, data queries) unless the needed commands are in the
command tree. Checked again before every batch; a de-op drops the rest of the queue. Copying,
ghosts, holograms and schematic export only read what the client already has.

## Dependencies

- **Fabric API** (Fabric): required.
- **Mod Menu** (Fabric): optional. Compiled against, not shipped; its settings button opens the
  key binds.
- **Ctrl-Z** (`ctrl-z`, needs Vanilib): optional, and the recommended undo mod to pair with this
  one (README says so). It records block changes on the server, one undo step per player command;
  a stamp in singleplayer is one step.
- **Chest_Tracker** (`entitytracker`): optional, later (phase 6). Declared under `suggests`,
  compiled against but not shipped, used from one class only after `isModLoaded`. Fabric 1.21.11
  only for now. Chest_Tracker needs a small public lookup method for this.
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

0. **Scaffold** (done): every version and loader builds and launches an empty client mod.
1. **Input** (done): Ctrl + left mouse caught, block breaking cancelled, press / drag / release
   tracked.
2. **Selection** (done): long raycast for corner B, scroll to push it nearer or further, box
   outline and size on the HUD.
3. **Copy and ghost** (done): read the blocks into a copy on release, render the ghost at the
   crosshair, rotate (R or scroll), cancel.
4. **Stamp** (done): paste with merged `/fill` and `/setblock`, one Ctrl-Z step in singleplayer,
   lock and nudge, blueprint and deconstruction tool items, rebindable keys, a settings screen.
   Tested on 26.3 Fabric only; every version and loader compiles. Not yet tested on a server.
5. **Holograms** for players who can't stamp: missing / wrong / done rendering, pick block, ignore
   marks.
6. **Block data:** server query as op, Chest_Tracker lookup, data in stamp commands.
7. **Schematics:** `.litematic` export.

## Open questions (for later)

- Holding an arrow key nudges once; should it repeat?
- Mod id: `creativeblueprint` is a leftover from the first working name; changing it later means
  renaming the package and resource folders too.
