# Cleansing Potions

We already know Milk cures all when you drink it. But while you're in a fight, you can use a Splash Bottle of Purging on an enemy to remove all of their good effects, like regeneration or strength. Want to remove only good effects? Drink a Potion of Soothing. And for both, you can simply use a Potion of Cleansing which removes everything. This gives you the ability midfight to handle status effects and get back in.


[![Total downloads](https://img.shields.io/endpoint?url=https%3A%2F%2Ffaststats.dev%2Fapi%2Fshields%2Fcleansingpotions-mod%3Fmetric%3Ddownloads%26icon%3D1&style=for-the-badge)](https://faststats.dev/project/cleansingpotions-mod)
[![Support Discord](https://img.shields.io/discord/1510890328943628350?style=for-the-badge&logo=discord&logoColor=white&label=discord&color=5865F2)](https://discord.gg/m3AKQfrMS5)
[![Build](https://img.shields.io/github/actions/workflow/status/alazso/cleansingpotions-mod/ci.yml?branch=main&style=for-the-badge&label=build)](https://github.com/alazso/cleansingpotions-mod/actions)
[![Source](https://img.shields.io/badge/Source-0f0f0f?style=for-the-badge&logo=github&logoColor=white)](https://github.com/alazso/cleansingpotions-mod)
[![License](https://img.shields.io/badge/license-MIT-2b2d31?style=for-the-badge)](https://github.com/alazso/cleansingpotions-mod/blob/main/LICENSE)

## The potions

There are three different potions. They all come in Regular Bottle, Splash, and Lingering variants.

All Potions:
<div class="spoiler">![image](https://cdn.alaz.so/out_all.gif)</div>

**Potion of Cleansing** clears every effect at once, good and bad, exactly like drinking milk: <div class="spoiler">This shows the Potion of Cleansing removing all of the potions effects from myself: ![image](https://cdn.alaz.so/out_cleansing.gif)</div>
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
  "soothing":  { "name": "Potion of Soothing",  "color": "#E8A33B", "lore": ["Clears harmful effects."] },
  "purging":   { "name": "Potion of Purging",   "color": "#7E57A8", "lore": ["Clears beneficial effects."] },
  "onlyThrower": false,
  "radius": 4.0,
  "forceRemove": ["minecraft:bad_omen"],
  "forceKeep": ["minecraft:conduit_power"],
  "cooldownEnabled": false,
  "cooldownSeconds": 5
}
```

## Requirements

Fabric 1.21.9 - 26.2: 
* Mod Menu (Optional)
* Cloth Config (Optional)

NeoForge 1.21.9 - 26.2:
* Cloth Config (Optional)

## License

Released under the [MIT License](LICENSE).
