# ENTEL CLIENT

**Minecraft Java Edition 1.21.11 • Fabric • Java 21**

Entel Client is a custom client-side cheat and utility mod featuring a categorized purple ClickGUI, configurable GUI keybind and persistent module settings.

## Download and installation

1. Go to the [Releases page](https://github.com/r7981435-star/EntelClient/releases).
2. Download the latest Entel Client mod JAR from the release assets.
3. Install Fabric Loader and Fabric API for Minecraft 1.21.11.
4. Place the Entel Client and Fabric API JARs in your Minecraft `mods` folder.
5. Launch Minecraft using the Fabric profile.
6. Press **Right Shift** to open the ClickGUI.

You can change the GUI keybind in **Options → Controls → Key Binds**.

## Modules (18)

| Category | Modules |
| --- | --- |
| Combat | AutoAttack, AutoTotem |
| Movement | Sprint, AutoJump, SafeWalk, AutoSneak, NoJumpDelay, AutoSwim |
| Render | Fullbright, Coordinates, SpeedHUD, DirectionHUD, HealthHUD |
| Player | AutoRespawn, AutoUse, AntiAFK |
| World | BaseFinder, Scaffold |

### Highlights

- **Scaffold:** Attempts to place blocks beneath you while moving over air, using blocks from your hotbar or offhand and an adjacent supporting block.
- **BaseFinder:** Scans the current chunk for suspicious blocks and reports their coordinates in chat.
- **Coordinates:** Updates your position every client tick.
- **ClickGUI:** Categorized panels with visual module toggle indicators and ENTEL CLIENT branding.
- **Persistent settings:** Saves module states locally.

## Build from source

Requires Java 21 and Gradle. Run `gradle build`. Output is located in `build/libs/`.

## Development status

Entel Client is in early development. The listed modules have source implementations, but compilation and in-game functionality have not all been verified. More modules, advanced BaseFinder filtering and GUI improvements are planned.

## Disclaimer

Use only in singleplayer or on servers that permit these modifications. Using cheats on other servers may result in bans. No anticheat bypass is guaranteed.
