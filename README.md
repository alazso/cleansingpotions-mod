# Cleansing Potions

We already know Milk cures all when you drink it. But while you're in a fight, you can use a Splash Potion of Purging on an enemy to remove all of their good effects, like regeneration or strength. Want to remove only bad effects? Drink a Potion of Soothing. And for both, you can simply use a Potion of Cleansing which removes everything. This gives you the ability midfight to handle status effects and get back in.


[![Total downloads](https://img.shields.io/endpoint?url=https%3A%2F%2Ffaststats.dev%2Fapi%2Fshields%2Fcleansingpotions-mod%3Fmetric%3Ddownloads%26icon%3D1&style=for-the-badge)](https://faststats.dev/project/cleansingpotions-mod)
[![Support Discord](https://img.shields.io/discord/1510890328943628350?style=for-the-badge&logo=discord&logoColor=white&label=discord&color=5865F2)](https://discord.gg/m3AKQfrMS5)
[![Build](https://img.shields.io/github/actions/workflow/status/alazso/cleansingpotions-mod/ci.yml?branch=main&style=for-the-badge&label=build)](https://github.com/alazso/cleansingpotions-mod/actions)
[![Source](https://img.shields.io/badge/Source-0f0f0f?style=for-the-badge&logo=github&logoColor=white)](https://github.com/alazso/cleansingpotions-mod)
[![License](https://img.shields.io/badge/license-MIT-2b2d31?style=for-the-badge)](https://github.com/alazso/cleansingpotions-mod/blob/main/LICENSE)

## The potions

There are three different potions. They all come in Regular Bottle, Splash, and Lingering variants.

All Potions:
<div class="spoiler">![image](https://cdn.alaz.so/out_all.gif)</div>

**Potion of Cleansing** clears every effect at once, good and bad, much like drinking milk (anything on the `forceKeep` list stays): <div class="spoiler">This shows the Potion of Cleansing removing all of the potions effects from myself: ![image](https://cdn.alaz.so/out_cleansing.gif)</div>
**Potion of Soothing** clears only the harmful effects and leaves your buffs alone: <div class="spoiler">This shows the Potion of Soothing solely removing the negative effects from myself: ![image](https://cdn.alaz.so/out_soothing.gif)</div>
**Potion of Purging** clears only the beneficial effects, which makes a well-aimed splash a real
  tool in a fight: <div class="spoiler">This shows the Potion of Purging solely removing the good effects from myself: ![image](https://cdn.alaz.so/out_purge.gif)</div>

## Brewing

Everything is made at a brewing stand, the way you would expect:

| Bottle | Ingredient | Result |
| --- | --- | --- |
| Water Bottle | Milk Bucket | Potion of Cleansing |
| Potion of Cleansing | Honey Bottle | Potion of Soothing |
| Potion of Cleansing | Fermented Spider Eye | Potion of Purging |
| Any of the three | Gunpowder | Splash version |
| A splash version | Dragon's Breath | Lingering version |

Lingering potions are the end of the chain, so the bottle slots do not accept them.

On Minecraft 26.3 these are ordinary data pack recipes, living in
`data/cleansingpotions/recipe/brewing`, so you can override or remove any of them with a data pack.
On 26.2 they are built in.

The config can switch them off on both versions: `brewingEnabled` covers all of them, `splashEnabled`
the gunpowder step, and `lingeringEnabled` the dragon's breath step. On 26.2 a change applies at once.
On 26.3 a change applies on the next world load, `/reload`, or `/cleanse reload`, because the recipes
are only rebuilt when data loads. A hand edit to the config file is read at game start, by
`/cleanse reload`, or when the config screen opens, on either version. Closing the screen without saving
does not undo that read.

## Using them

Drink one to clean up your own effects. Throw a splash to catch everyone in range, or set it to affect
only the thrower. Throw a lingering to leave a cloud that keeps cleansing anyone who stands in it.

Dispensers throw the splash and lingering potions too. A dispensed potion has no thrower, so with
`onlyThrower` on, a dispensed splash cleanses nobody.

With `splashEnabled` or `lingeringEnabled` off, throwing that potion is refused and it stays in your
hand. A dispenser still fires it, and it shatters with no effect.

The `/cleanse` command (or the full `/cleansingpotions`) is limited to operators:

* `/cleanse give <cleansing|soothing|purging> [regular|splash|lingering] [players]` hands out any potion.
* `/cleanse reload` rereads the config file, and on 26.3 also reloads data packs so the recipe toggles
  apply. If the file cannot be read it says so, and the previous settings stay.
* `/cleanse testerror` queues an intentional test error with FastStats, for operators checking that error
  reporting works. If FastStats is not sending error reports, it says so and queues nothing.

## Configuration

Settings live in `config/cleansingpotions.json`, written on first launch. With Cloth Config installed,
most of them can also be changed in-game (through Mod Menu on Fabric, or the Mods list on NeoForge).
The two force lists are edited in the file.

```json
{
  "splashEnabled": true,
  "lingeringEnabled": true,
  "onlyThrower": false,
  "radius": 4.0,
  "forceRemove": [
    "minecraft:bad_omen"
  ],
  "forceKeep": [
    "minecraft:conduit_power"
  ],
  "brewingEnabled": true,
  "cooldownEnabled": false,
  "cooldownSeconds": 5,
  "permissionsEnabled": false,
  "cleanseFeedback": true,
  "impactParticles": true,
  "checkForUpdates": true,
  "metrics": true
}
```

| Setting | What it does |
| --- | --- |
| `brewingEnabled` | Allows brewing the potions at all. |
| `splashEnabled`, `lingeringEnabled` | Allow brewing and throwing that type. |
| `onlyThrower` | A splash cleanses only whoever threw it. |
| `radius` | Area of effect in blocks, from 0.5 to 32. |
| `forceRemove`, `forceKeep` | Effects every potion always clears, or never clears. An effect on both lists is kept. An entry with no namespace means `minecraft:`. |
| `cooldownEnabled`, `cooldownSeconds` | A cooldown between throws, from 0 to 3600 seconds. A change applies at once, and cooldowns reset when the server or world stops. |
| `permissionsEnabled` | Gates drinking and throwing behind a permission, see below. |
| `cleanseFeedback` | Sparkle particles and a chime when an entity is cleansed. |
| `impactParticles` | Shatter particles where a potion lands. |
| `checkForUpdates`, `metrics` | See [Metrics and the update check](#metrics-and-the-update-check). |

A value outside its range is pulled back into range when the file loads. A file that cannot be parsed
is left as it is, and the previous settings stay in effect (the defaults, at game start). The config
screen rereads the file each time it opens. If the file still cannot be parsed, saving from the screen
first copies it to `config/cleansingpotions.json.bak`.

Potion names and tooltips come from language files, so a resource pack can rename or translate them,
and a language file also translates the config screen. Potion item colours are tints in
`assets/cleansingpotions/items`, so a resource pack changes those too. The colours of clouds and impact
particles are fixed.

### Permissions

With `permissionsEnabled` on, drinking and throwing check the node `cleansingpotions.use` on multiplayer
servers. The node is allowed by default, so you deny it in your permissions setup. Singleplayer is never
gated, and neither are guests on a LAN world.

* Fabric needs a permissions mod that works with the Fabric Permissions API. Without that API the gate
  has no effect, and the game logs a warning at startup.
* NeoForge reads the node through its own permission API, so it needs a mod that supplies a permission
  handler. Without one, everyone stays allowed.

## Metrics and the update check

**Metrics.** The mod reports pseudonymous usage statistics and errors through
[FastStats](https://faststats.dev): counts such as potions used and effects removed, the mod's settings,
error reports with stack traces for failures in this mod's code, and environment details (mod, game, and
loader versions, player count, online mode, Java version, operating system, and CPU core count). An install
is identified by a random ID in `config/faststats/config.properties`. The
[full list of metrics](https://github.com/alazso/cleansingpotions-mod/blob/main/docs/faststats-data-sources.md)
is in the repository.

There are two ways to turn it off:

* `metrics` in `config/cleansingpotions.json` or the config screen covers this mod. Turning it off through
  the screen or `/cleanse reload` stops FastStats at once, and nothing further is sent. Turning it back on
  takes effect at the next game start.
* `enabled=false` in `config/faststats/config.properties` covers every FastStats mod on that install, and
  is read at game start. That file also has `submitMetrics`, `submitAdditionalMetrics`, and `submitErrors`
  for finer control.

FastStats sends nothing during the run that creates that file, so you can opt out before anything is sent.
The one exception is a player closing the FastStats consent screen during that run, which applies that
screen's choices at once. Its boxes start ticked, so closing it unchanged starts sending. The game's
telemetry options offer that screen, and another FastStats mod can open it at the title screen.
If `config/cleansingpotions.json` cannot be parsed at game start, metrics stay off for that session unless
the file's text holds `"metrics": true`.

**Update check.** On the first world join of a session, the client asks the Modrinth API whether a newer
release exists for your game version and loader, and shows a toast if there is one. On a Fabric patch
release such as 26.3.1 it also asks about 26.3, because a Fabric jar for 26.3 loads there. The request
carries the game version, the loader, and the mod version (in its user agent). It runs only on clients,
never on dedicated servers. Set `checkForUpdates` to `false` to turn it off.

## Requirements

Minecraft 26.2 or 26.3, with a separate jar for each version and loader.

Fabric:
* Fabric API
* Mod Menu (Optional)
* Cloth Config (Optional)
* Fabric Permissions API (Optional)

NeoForge:
* Cloth Config (Optional)

Minecraft 1.21.9 to 26.1.2 are supported by version 0.2.0, which stays available for download.

## License

Released under the [MIT License](LICENSE).
