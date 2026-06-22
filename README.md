# Cleansing Potions

We already know Milk cures all when you drink it. But while you're in a fight, you can use a Splash Bottle of Purging on an enemy to remove all of their good effects, like regeneration or strength. Want to remove only good effects? Drink a Potion of Soothing. And for both, you can simply use a Potion of Cleansing which removes everything. This gives you the ability midfight to handle status effects and get back in.

[![Build](https://img.shields.io/github/actions/workflow/status/alazso/cleansingpotions-mod/ci.yml?branch=main&style=for-the-badge&label=build)](https://github.com/alazso/cleansingpotions-mod/actions)
[![Total downloads](https://img.shields.io/endpoint?url=https%3A%2F%2Ffaststats.dev%2Fapi%2Fshields%2Fcleansingpotions-mod%3Fmetric%3Ddownloads&style=for-the-badge)](https://faststats.dev/project/cleansingpotions-mod)
[![License](https://img.shields.io/badge/license-MIT-2b2d31?style=for-the-badge)](https://github.com/alazso/cleansingpotions-mod/blob/main/LICENSE)

[![Source](https://img.shields.io/badge/Source-0f0f0f?style=for-the-badge&logo=github&logoColor=white)](https://github.com/alazso/cleansingpotions-mod)
[![Support Discord](https://img.shields.io/discord/1510890328943628350?style=for-the-badge&logo=discord&logoColor=white&label=discord&color=5865F2)](https://discord.gg/m3AKQfrMS5)

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.9%20–%2026.2-blue)
![Loaders](https://img.shields.io/badge/loader-Fabric%20%7C%20NeoForge-green)
[![Total downloads](https://img.shields.io/endpoint?url=https%3A%2F%2Ffaststats.dev%2Fapi%2Fshields%2Fcleansingpotions-mod%3Fmetric%3Ddownloads%26icon%3D1&style=for-the-badge)](https://faststats.dev/project/cleansingpotions-mod)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

## The potions

There are three, and they differ only in what they strip away:

- **Potion of Cleansing** clears every effect at once, good and bad, exactly like drinking milk.
- **Potion of Soothing** clears only the harmful effects and leaves your buffs alone.
- **Potion of Purging** clears only the beneficial effects, which makes a well-aimed splash a real
  tool in a fight.

## Brewing

Everything is made at a brewing stand, the way you would expect:

| Bottle | Ingredient | Result |
| --- | --- | --- |
| Water Bottle | Milk Bucket | Potion of Cleansing |
| Potion of Cleansing | Honey Bottle | Potion of Soothing |
| Potion of Cleansing | Fermented Spider Eye | Potion of Purging |
| Any of the three | Gunpowder | Splash version |
| A splash version | Dragon's Breath | Lingering version |

The splash and lingering steps can each be turned off in the config.

## Using them

Drink one to clean up your own effects. Throw a splash to catch everyone in range, or set it to affect
only the thrower. Throw a lingering to leave a cloud that keeps cleansing anyone who stands in it.

The `/cleanse` command (or the full `/cleansingpotions`) hands out any potion and reloads the config.
It is limited to operators.

## Configuration

Settings live in `config/cleansingpotions.json`, written on first launch, and the common ones can also
be changed in-game through the Cloth Config screen. You can adjust the area-of-effect radius, force
specific effects to always or never be cleared, add a throw cooldown, rename and recolour each potion,
and gate use behind a permission on multiplayer servers. Permission gating never affects singleplayer.

```json
{
  "cleansing": { "name": "Potion of Cleansing", "color": "#FFFFFF", "lore": ["Clears every potion effect."] },
  "onlyThrower": false,
  "radius": 4.0,
  "forceRemove": ["minecraft:bad_omen"],
  "forceKeep": ["minecraft:conduit_power"],
  "cooldownEnabled": false,
  "cooldownSeconds": 5
}
```

## Requirements

Minecraft 1.21.9 through 26.2, on Fabric (with Fabric API) or NeoForge. Cloth Config is optional and
only adds the in-game settings screen. Because the mod adds items and recipes, it goes on the server,
or your singleplayer world, and on every client that connects.

## License

Released under the [MIT License](LICENSE).
