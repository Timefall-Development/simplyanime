# Simply Anime

An addon for [Simply Swords](https://github.com/Sweenus/SimplySwords) that adds unique weapons from anime.
Forge 1.20.1 and NeoForge 1.21.1.

## Weapons

**Inverted Spear of Heaven** (sai)
- Right click: dash forward and cut twice, the first hit knocks the target into the air.
- Sneak + right click: throw the blade out on its chain and whirl it around you, then fling it into the marked target.

**Ea, The Sword Of Rupture** (claymore)
- Right click, Enuma Elish: raise the sword and charge it (you can't be hurt while charging), then thrust
  to fire a huge beam. It deals one heavy hit when it fires and keeps damaging anything inside it for the rest
  of the beam. It goes through terrain and never breaks blocks.

**Divine Axe Rhitta** (greataxe)
- Sunshine: the axe hits harder the higher the sun is, strongest at noon and weakest at midnight. Being
  under a roof or in the rain weakens it, and every hit sets the target on fire.
- Right click, Cruel Sun: raise the axe and a small sun forms over you, burning anything close to it. Then
  it is thrown and goes off in a dome of fire. The sun and the blast are bigger toward noon.

All three are registered like any other Simply Swords unique: they show up in loot chests, take gems and
runic upgrades, and ship Better Combat weapon attributes. Ea can also be crafted (two nether stars, redstone blocks,
gold blocks and a netherite ingot).

Enuma Elish and Cruel Sun show the ability's name above the hotbar when they go off, that can be turned off.

Config files are in `config/simplyanime/`:
- `weapons.toml`: the spear and Ea. Damage, cooldowns, beam size, sounds, whether Ea is craftable or found in loot.
- `rhitta.toml`: the axe, how the sun affects it, Cruel Sun, and the block damage options (all off by default).
- `rhitta_client.toml` and `client.toml`: per player. Camera, HUD, particles, glow strength with shaders.

`/SimplyAnime cooldown reset [players]` clears ability cooldowns (needs op), handy for testing.

## Shaders

Works with Oculus on Forge and Iris on NeoForge. The effects use each pack's own glow and bloom and light the ground around
them. Tested with BSL, Complementary Reimagined and Unbound, MakeUp Ultra Fast, Rethinking Voxels, Solas,
Photon and Bliss. If a pack washes the effects out, lower `shaderGlow` in `client.toml`.

## Requirements

Forge 1.20.1:
- Forge 47.4 or newer
- Simply Swords 1.70.2 or newer (and its own dependencies)
- Fzzy Config 0.7.6 or newer

NeoForge 1.21.1 (also loads on 1.21):
- NeoForge 21.1 or newer
- Simply Swords 1.70.2 or newer (and its own dependencies)
- Fzzy Config 0.7.6 or newer

## Building

Run Gradle from the version folder you want. It has to run on **Java 21** (the mod for 1.20.1 still
targets Java 17). If your default Java isn't 21, point Gradle at one, for example in
`~/.gradle/gradle.properties`:

```
org.gradle.java.home=/path/to/jdk-21
```

Then, inside `forge-1.20.1` or `neoforge-1.21.1`:

```
./gradlew build        # jar ends up in build/libs
./gradlew runClient    # dev client with Simply Swords and its dependencies
./gradlew runServer    # dev server, on Forge add -Pplain to leave the shader mods out
```

The dependencies come from the Modrinth maven by version id (`gradle.properties`), because the builds
for different loaders share the same version number.

## Credits

- Simply Swords by Sweenus / Timefall Development

## License

Source available, all rights reserved. See [LICENSE](LICENSE).
