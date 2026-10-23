# Fast Creative Copy Paste

A client-side mod for fast, Factorio-style copy and paste in creative mode: Ctrl+drag over some
blocks to copy them, then click to stamp the copy wherever you look. See
[docs/PLAN.md](docs/PLAN.md) for the design.

> Early development: copying, the ghost, stamping, lock and nudge work (phases 0–4), tested on
> 26.3 Fabric. Nothing is released yet.

## How to use

In creative, with anything in hand:

| Input | Does |
|-------|------|
| **Ctrl + drag** (left mouse), or drag holding a **stick** | Draw a box. Scroll to push the far corner further or nearer. |
| Let go of the mouse | Copy the box. A see-through ghost of it follows the crosshair. |
| Drag holding a **totem of undying** | Draw a red box; letting go clears it to air (to carve a hole, say). |
| Let go of Ctrl first, or **right click** | Cancel the drag. |
| **Click** | Stamp the copy at the ghost. The ghost stays, so you can keep stamping. |
| **R**, or **scroll** | Turn the copy a quarter (scroll down clockwise, up counterclockwise). |
| **Middle click** (or **H**) | Lock the ghost in place (orange outline), or unlock it. |
| **Arrow keys** (locked) | Move it one block, relative to where you face. |
| **Page Up / Page Down** (or **]** / **[**) (locked) | Move it up and down. |
| **Right click** | Cancel the copy. |

The stick and the totem are the two tools, like Factorio's blueprint and deconstruction planner:
holding one, a plain left drag works without Ctrl. Both items can be changed (or turned off) in
the settings.

Stamping and clearing place real blocks with `/fill` and `/setblock`, so they need permission to
run those commands: op on a server, or cheats on in singleplayer. Air in a copy is skipped, so a
stamp only adds blocks and never digs into what is already there; use the deconstruction tool to
dig.

**Settings:** the mod's settings button (in [Mod Menu](https://modrinth.com/mod/modmenu) on
Fabric, or the mods list's Config button on NeoForge / Forge) picks the two tool items (saved in
`config/creativeblueprint.json`) and leads to the key binds. All keys are rebindable under
**Options → Controls → Key Binds → Fast Creative Copy Paste**.

## Pairs well with: Ctrl-Z

[Ctrl-Z](https://modrinth.com/mod/ctrlz) ([source](https://github.com/Choculaterie/Ctrl-Z)) is the
recommended undo mod to use with this one. Ctrl+Z / Ctrl+Y undo and redo your block changes,
stamps and clears included; in singleplayer a whole stamp or clear undoes in one press (on a
server, one press per command it used). It needs [Vanilib](https://modrinth.com/mod/TpZlEkF6) and must also be on
the server (singleplayer counts). It is optional: this mod works without it.

## Versions

| Minecraft | Loaders          |
|-----------|------------------|
| 1.20.1    | Fabric, Forge    |
| 1.21.1    | Fabric, NeoForge |
| 1.21.11   | Fabric, NeoForge |
| 26.2      | Fabric, NeoForge |
| 26.3      | Fabric, NeoForge |

Fabric needs [Fabric API](https://modrinth.com/mod/fabric-api).

## Building

Gradle runs itself on Java 25 and downloads it if it's missing
(`gradle/gradle-daemon-jvm.properties`). Java 17 and 21 for the older versions are downloaded the
same way.

```
./gradlew build
```

Jars end up in `versions/<version>-<loader>/build/libs/`. `./gradlew buildAndCollect` copies them
all to `build/libs/<mod version>/`.

The project uses [Stonecutter](https://stonecutter.kikugie.dev/) with Mojang mappings. Source is
kept in the `26.3-fabric` state; switch the active version with the Stonecutter tasks before
editing version-specific code.

## Dev client

`./gradlew :<version>-<loader>:runClient` starts a dev client. They share `run/` (saves, options,
config). Fabric dev clients also load Mod Menu; the 26.3 Fabric one also loads Ctrl-Z and
Vanilib, to test stamping and undo together. None of these are shipped in the mod's jar.

### Letting Claude drive it

The 1.21.1–26.3 dev clients also load [MCPFabric](https://modrinth.com/mod/mcpfabric), which lets
an MCP client like Claude Code look around and play the game. There is no MCPFabric for 1.20.1,
and no MCP mod confirmed to work on Forge 1.20.1 either.

Its MCP server is built locally (both paths are git-ignored):

```
git clone --depth 1 --branch v0.8.0 https://github.com/denfry/mcpfabric.git .dev/mcpfabric
cd .dev/mcpfabric/mcp-server && npm ci --ignore-scripts && npm run build
```

Launch a dev client once so it writes `run/config/mcpfabric.config.json`, then point `.mcp.json`
at `.dev/mcpfabric/mcp-server/dist/index.js` with `MCPFABRIC_TOKEN` set to the `token` from that
file. Set `enableUiControl` to `true` there to let it click through menus, and turn off "Pause on
lost focus" in the dev client so it doesn't pause while you are in another window.

MCPFabric can't hold keys or mouse buttons, so dev clients have a test hook: chat messages
starting with `#cbtest` press virtual keys and log the mod's state (see `DevTools.java`).

## License

[MIT](LICENSE)
