# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project uses
semantic versioning.

## [0.1.2]

- Fixed the lingering cloud rendering as invisible (and potion liquids showing untinted). Particle and tint colors are now opaque, so the cloud is visible and cleanses anyone standing in it.

## [0.1.0]

First release: a Fabric and NeoForge mod for Minecraft 1.21.9 through 26.2.

**Potions**
- Three milk-brewed cleansing potions: **Cleansing** clears every effect, **Soothing** clears only the harmful ones, and **Purging** strips the beneficial ones off whoever it hits.
- Each comes as a drinkable, a splash, and a lingering version. Splash cleanses an area (or just the thrower), and lingering leaves a cloud that keeps cleansing over its lifetime.
- Identity is stored in a data component, not the item name, so renamed look-alikes cannot be faked.

**Brewing**
- Water bottle plus a milk bucket brews a Potion of Cleansing. Brew that with a honey bottle for Soothing, or a fermented spider eye for Purging.
- Gunpowder makes any of them a splash, and dragon's breath a lingering, the same as any vanilla potion.

**Configuration and commands**
- Area-of-effect radius, only-thrower mode, force-remove and force-keep effect lists, a throw cooldown, brewing and splash/lingering toggles, and a singleplayer-safe permission gate.
- Per-potion name, lore, and liquid color.
- In-game Cloth Config screen (Fabric via Mod Menu, NeoForge via the Mods list) alongside the JSON config.
- `/cleansingpotions` (alias `/cleanse`) with `give` and `reload`.

**Other**
- Anonymous usage metrics on 26.x (via FastStats), toggleable in the config.
