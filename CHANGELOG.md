# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project uses
semantic versioning.

## [0.3.0]

- Added support for Minecraft 26.3 (Fabric). NeoForge 26.3 follows once it leaves beta.
- Dropped Minecraft 1.21.9 through 26.1.2. Those versions stay served by 0.2.0.
- Each potion mode is now its own item, so there are nine in total (Cleansing, Soothing, and Purging, each as regular, splash, and lingering). 26.3 brewing recipes match on item id and cannot read a data component, which is what made brewing a Soothing or Purging splash potion possible again. Recipe viewers can now show all nine.
- Brewing on 26.3 is data driven: the recipes ship as JSON in `data/cleansingpotions/recipe/brewing` and can be overridden or removed by a data pack. The 26.3 build carries no mixins at all as a result. On 26.2 brewing is unchanged.
- Potion names and lore now update on potions already in your inventory when you edit the config, instead of only on newly brewed ones.
- Potion colour is no longer a config option. Colours are fixed and come from the resource pack, because the tint is chosen by the item model and a resource pack cannot read runtime config. The `color` field in each appearance block is ignored and can be deleted.
- Soothing and Purging potions brewed before 0.3.0 keep working, though they show the Cleansing tint until re-brewed.
- Added anonymous gameplay metrics and error reporting on Minecraft 26.x (via FastStats), behind the existing metrics toggle. The brewing counters do not report on 26.3, where the mixin that fed them no longer exists.
- Fixed drinking a cleansing potion making no sound. Drinking now also counts toward advancements that track consumed items, the same as a vanilla potion.

## [0.2.0]

- Fixed a crash on Minecraft 26.2 when a thrown lingering potion lands or when brewing. The 26.x line now ships native per-version builds (26.1.2 and 26.2), so each runs against its own Minecraft.
- Cleansing now gives feedback: sparkle particles and a chime when an entity's effects are cleared.
- Splash and lingering potions shatter on impact, with the burst scaled to the cleanse radius.
- A throw cooldown now tells you the time remaining instead of failing silently.
- An optional update notifier shows a toast on join when a newer version is out, and flags patches that fix a tagged bug. Toggle with checkForUpdates.
- New config toggles for the cleanse feedback, impact particles, and update check.

## [0.1.3]

- Fixed a crash on Minecraft 26.2 when a thrown lingering potion lands, and when brewing (a vanilla potion field changed type between 26.1.2 and 26.2).

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
