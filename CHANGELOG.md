# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project uses
semantic versioning.

## [0.3.0]

**Minecraft versions**
- Added Minecraft 26.3 on both Fabric and NeoForge. 26.2 stays supported on both.
- Dropped Minecraft 1.21.9 through 26.1.2. Those versions stay served by 0.2.0.
- A Fabric jar loads on its own Minecraft drop, patch releases included, and not on the next drop's prereleases. A NeoForge jar loads only on its exact Minecraft version.

**Potions**
- Each potion mode is now its own item, so there are nine in total: Cleansing, Soothing, and Purging, each as regular, splash, and lingering. Brewing a Soothing or Purging splash or lingering potion works on 26.3 as a result, because each recipe there names the item it takes.
- Potions from earlier versions turn into their new items automatically once they are in a player's inventory, and a name given on an anvil is kept. A stack still in a chest or other container keeps its mode until then, with one limit on 26.3: recipes there do not read the mode stored on an older stack, so an older Soothing or Purging potion that reaches a brewing stand without passing through a player's inventory (left in a bottle slot, or fed by a hopper) brews as a Cleansing potion of that variant.
- Names and tooltips come from language files, so a resource pack can change them. Colours are fixed tints that a resource pack can also change. The `cleansing`, `soothing`, and `purging` blocks in an older config file are ignored, including their `name`, `lore`, and `color` fields, and they drop out the next time the config is saved.
- Dispensers now throw splash and lingering potions.
- A lingering cloud keeps its mode across a save and load. A cloud saved by an earlier version loads as Cleansing.
- A lingering cloud lasts its full duration at every radius.
- A cleanse reaches an entity when its hitbox is within the radius.
- Fixed drinking a cleansing potion making no sound. Drinking now also counts toward advancements that track consumed items, the same as a vanilla potion.
- A use the server refuses (no permission, a disabled potion type, a throw cooldown) shows its reason in the action bar and the potion stays in your inventory, on the client as well. Throwing a disabled potion type is refused, so the potion is kept. The cooldown message reads correctly at one second.
- The thrown potion and the cloud have proper names, and their entity settings match the vanilla splash potion and area effect cloud.
- `/cleanse give` feedback names the potion that was given, including its variant.
- A `/cleanse give` potion that does not fit drops for the target only, with no pickup delay, as with vanilla `/give`.

**Brewing**
- On 26.3 the recipes are JSON in `data/cleansingpotions/recipe/brewing`, which a data pack can override or remove. The 26.3 builds carry no mixin as a result.
- `brewingEnabled`, `splashEnabled`, and `lingeringEnabled` control the 26.3 recipes through load conditions, and apply on the next world load, `/reload`, or `/cleanse reload`. They work on 26.2 as well, where a change applies at once.
- `/cleanse reload` rereads the config file, and on 26.3 also reloads data packs so the recipe toggles apply.
- On 26.2 the regular and splash potions go into brewing stand bottle slots, by hand and by hopper, when a brew switched on in the config takes them. Lingering potions are refused, because no recipe takes them.
- On 26.2 milk buckets and honey bottles are no longer accepted as brewing ingredients while `brewingEnabled` is off.

**Configuration**
- Throw cooldowns follow the current `cooldownSeconds`, so a lowered value applies to a player already cooling, and they reset when the server or world stops.
- A `forceRemove` or `forceKeep` entry with no namespace, such as `night_vision`, means `minecraft:`.
- Config values are sanitized on load and on save: `radius` stays within 0.5 to 32, `cooldownSeconds` within 0 to 3600, a radius that is not a finite number becomes the default, and null entries leave the force lists.
- Saves write a temporary file and move it into place, so a failed write leaves the existing file intact.
- A config file that cannot be read keeps the previous settings, and `/cleanse reload` reports that failure in place of claiming success. Plain `/reload` does not reread the file.
- The config screen has reset buttons and limits the radius to 0.5 to 32, and its text is translatable. Without Cloth Config, Mod Menu shows no config button.
- The config screen rereads the file each time it opens, so a save keeps hand edits. Saving over a file that cannot be parsed first copies it to `config/cleansingpotions.json.bak`.
- The permission gate now works on NeoForge through the node `cleansingpotions.use`, which needs a mod that supplies a permission handler. On Fabric it needs the Fabric Permissions API. Either way, a server logs a warning at startup when the gate is on without what it needs.
- The update check asks only for versions matching your loader and game version, and ignores anything that is not a release. On a Fabric patch release such as 26.3.1 it also matches versions for 26.3, because those jars load there.

**Metrics**
- Added pseudonymous gameplay metrics and error reporting via FastStats (SDK 0.31.0), behind the `metrics` toggle. FastStats ships nested inside the jar as its own mod, and the mod keeps working without metrics if FastStats cannot start.
- Clients get a FastStats consent screen once at the title screen, including installs updating from an older version, and this mod sends nothing from a client until it is answered. Its boxes start ticked, the answer covers every FastStats mod on the install, and the FastStats button in the game's telemetry options reopens it. That button is greyed out while no FastStats mod is running.
- A dedicated server has no screen and sends nothing during the run that creates `config/faststats/config.properties`. That file is shared by every FastStats mod on the install and has its own opt-outs.
- Turning `metrics` off through the config screen or `/cleanse reload` stops FastStats at once, and nothing further is sent. Turning it back on takes effect at the next game start.
- If the config file cannot be parsed at game start, metrics stay off for that session unless the file's text holds `"metrics": true`.
- Failures the mod recovers from are reported too, once per source per game session: an unreadable or unwritable config, and an installed Fabric Permissions mod whose API does not match.
- An error that kills a thread is reported automatically only when its stack trace includes this mod's code, so another mod's failure is not reported.
- `/cleanse testerror` queues an intentional test error for operators checking error reporting, and queues nothing when metrics are off or FastStats is not sending error reports.
- The brewing counters stay at zero on 26.3, where the mixin that feeds them does not ship.

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
- Pseudonymous usage metrics on 26.x (via FastStats), toggleable in the config.
