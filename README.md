# Fast Creative Copy Paste

A client-side mod for fast, Factorio-style copy and paste in creative mode: Ctrl+drag to copy,
then stamp the copy as a blueprint. See [PLAN.md](PLAN.md) for the design.

> Early development: this is the empty scaffold (phase 0).

## Versions

| Minecraft | Loaders          |
|-----------|------------------|
| 1.20.1    | Fabric, Forge    |
| 1.21.1    | Fabric, NeoForge |
| 1.21.11   | Fabric, NeoForge |
| 26.2      | Fabric, NeoForge |
| 26.3      | Fabric, NeoForge |

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

## Letting Claude drive the dev client

The 1.21.1–26.3 dev clients (`runClient`) also load [MCPFabric](https://modrinth.com/mod/mcpfabric),
which lets an MCP client like Claude Code look around and play the game. It is never shipped in
the mod's jar, and there is no MCPFabric for 1.20.1.

Its MCP server is built locally (both paths are git-ignored):

```
git clone --depth 1 --branch v0.8.0 https://github.com/denfry/mcpfabric.git .dev/mcpfabric
cd .dev/mcpfabric/mcp-server && npm ci --ignore-scripts && npm run build
```

Launch a dev client once so it writes `run/config/mcpfabric.config.json`, then point `.mcp.json`
at `.dev/mcpfabric/mcp-server/dist/index.js` with `MCPFABRIC_TOKEN` set to the `token` from that
file. MCPFabric can't hold Ctrl or the left mouse button, so the Ctrl+drag gesture itself still
needs a person (or a dev-only test hook).

## License

[MIT](LICENSE)
