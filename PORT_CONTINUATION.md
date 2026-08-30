# BuildCraft 1.20.1 Port — Continuation Prompt

Paste the section below to another AI agent to continue this port.

---

You are continuing a **functional port of BuildCraft from Minecraft 1.12.2/Forge 14 to 1.20.1/Forge 47**.
The legacy 1.12.2 source (`common/`, `common_old_license/`, `sub_projects/`) is kept in the repo but is
**excluded from compilation**. All ported code lives under `src/main/java/buildcraft/` and uses modern
APIs (`DeferredRegister`, `EntityBlock`, `MenuType`, Forge Energy/Fluids, `BlockEntityRenderer`,
worldgen features + Forge biome modifiers).

## Environment / build
- JDK **17** is required (install Temurin 17; set `JAVA_HOME`). ForgeGradle 6, Gradle 8.1.1 wrapper.
- Compile: `./gradlew compileJava`  ·  Full jar: `./gradlew build` → `build/libs/buildcraft-8.0.0.jar`
- Unit tests: `./gradlew test` (port tests live in `src/porttest/java`, not the legacy `src/test`).
- Smoke test (headless): `./gradlew runServer` with `run/eula.txt=eula=true`; confirm it logs
  `BuildCraft (1.20.1 port) constructed.` and `Done (…)!` with no exceptions.

## Project conventions
- Registries are centralized in `buildcraft/registry/`: `BCItems`, `BCBlocks`, `BCBlockEntities`,
  `BCMenuTypes`, `BCEntities`, `BCFeatures`, `BCFluids`, `BCCreativeTabs`. Add new content there.
- Machines: a block entity implements `buildcraft.factory.tile.ITickingMachine`; energy machines use
  `buildcraft.factory.util.MachineEnergyStorage` (receive) or `buildcraft.energy.util.EngineEnergyStorage`
  (generate/extract). Generic blocks exist: `factory.block.BlockMachine`, `transport.block.BlockPipe`,
  `builders.block.BlockBuilderMachine`, `silicon.block.BlockSiliconTable`.
- Every new block/item needs: registry entry, creative-tab entry (`BCCreativeTabs`), blockstate + model
  + item model under `src/main/resources/assets/buildcraft/`, lang entry in `lang/en_us.json`, a crafting
  recipe and (for blocks) a loot table under `src/main/resources/data/buildcraft/`.
- GUIs are drawn with primitives (`GuiGraphics.fill`) — see `energy/client/EngineScreen`. Menus use
  `IForgeMenuType.create(...)` + `MenuScreens.register(...)` in `client/BuildCraftClient`.
- Commit per logical change; build + smoke test before each commit.

## Already ported (functional)
core (gears/wrench/shard), engines (redstone/stirling/combustion+liquid-fuel/creative), fluids (oil/fuel
+ buckets + oil worldgen), factory (tank, pump, mining well, refinery, distiller, floodgate, heat
exchanger, chute, auto workbench), builders (quarry, marker, filler, library, Template/Architect/Builder
blueprint system with full block-state capture), transport (item pipes wood/cobble/stone/gold/obsidian/void,
fluid pipes wood/cobble, kinesis pipes wood/cobble, a functional Gate), silicon (chipsets, assembly table,
integration + programming tables, laser with beam render), robotics (flying picker Robot entity + AI +
renderer, robot station, zone planner, boards), lib (NBT helpers, expression engine + JUnit tests),
rendering (in-pipe travelling items, laser beam), functional Wrench (rotates machines).

## What to do next (priority order)
1. **Iron & Diamond pipes** — iron = single wrench-set output direction (add a facing to the pipe tile,
   read it in `TilePipe.tryExit`); diamond = per-side item filters (needs a small GUI).
2. **Pipe wires/facades/plugs** — requires a pipe *side-attachment framework* the current simplified pipe
   port lacks; design that first (a per-side part registry on `TilePipe`).
3. **Paintbrush + pipe/machine coloring** (16 color variants; add a `DyeColor` blockstate to pipes).
4. **Engine overheat explosions** and heat-based piston-speed visuals (add a real engine `BlockEntityRenderer`
   that animates the trunk; note the current model draws a static trunk — hide it if you add a BER).
5. **More robot behaviours** (miner, farmer, lumberjack) as AI goal variants on `RobotEntity`.
6. **Config** (Forge config spec) for energy rates, worldgen frequency, feature toggles.
7. **Data generation** (`GatherDataEvent`) to replace the hand-written models/recipes/loot with providers.

Keep changes compiling and smoke-tested. Favour working mechanics over 1:1 fidelity where the original
depended on the huge `lib` render/expression framework.
