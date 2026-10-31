# FastStats Data Sources — cleansingpotions

## Conventions

- **type** — the value's primitive: `number`, `string`, or `boolean`.
- **shape**
  - `Scalar` — one value per report (a counter, gauge, or flag).
  - `Array` — an ordered list of values.
  - `Map` — key → value pairs; validation columns describe the **values**.
- **validation** — `allowNegative` / `allowDecimals` / `min` / `max`. For `boolean` and
  `string` sources these are not applicable (`—`). `min`/`max` left as `—` mean unbounded;
  bounds marked *(suggested)* are sane guardrails, not hard requirements.

Built-in environment metrics (mod version, game version, loader, player and server counts) are
collected by the SDK itself and need no data source here. Two SDK changes matter for dashboards:
`minecraft_version` was renamed to `game_version` in SDK 0.28.1, so jars built on an older SDK
still send the old key, and SDK 0.29.4 added a boolean `client` that separates client installs
from servers.

**Error reports are not automatic.** FastStats captures an error on its own only when one kills a
thread, and the Fabric and NeoForge platform modules register nothing else (the Paper
`ServerExceptionEvent` path is Bukkit only). Anything this mod catches and recovers from reaches the
dashboard only by calling `ErrorReporter.report`. Current report sites:

| source | raised when |
| --- | --- |
| `config:load` | the config file could not be read or parsed, so every setting fell back to its default |
| `config:save` | the config could not be written, so an owner's edit was lost |
| `permissions:fabric-permissions-api` | the Fabric Permissions mod is installed but its API no longer matches, so every permission check silently returns its default |

Deliberately not reported: the update check failing (offline players and timeouts are not defects),
the toast accessor being absent, and enum parse fallbacks. Noise there buries the reports worth
reading. `/cleanse testerror` sends a report with `intentional=true` and `handled=false`.

The first config load runs before the metrics toggle has been read, so it happens before the FastStats
context exists. `ErrorReporter` holds up to 8 such reports and flushes them when the context attaches,
which is why a malformed config still reports on the boot that found it.

**Consent.** On a client, SDK 0.31.0 and later show a consent screen on the title screen before
anything is submitted, and one screen covers every FastStats mod installed. Dedicated servers have no
screen and keep using `config/faststats/config.properties`. Our own `metrics` config option still
decides whether a context is created at all, so turning it off keeps this mod out of that screen.

**Reporting semantics.** Counters report the total since the last successful metrics submission.
The suppliers only read (`CleansingMetrics.peek*`), because FastStats also reads them when it
assembles an error report. They are cleared by `CleansingMetrics.resetAll`, registered through
`Metrics.Factory#onFlush`, which runs only after a submission is accepted.

---

## A. Action counters (core)

Increment-by-1 (or by N) counters. All are non-negative whole numbers.

| name | reference_id | type | shape | allowNegative | allowDecimals | min | max | emit point |
|---|---|---|---|---|---|---|---|---|
| Potions Consumed | `potions_consumed` | number | Scalar | false | false | 0 | — | `CleansingPotionItem.finishUsingItem` |
| Potions Thrown | `potions_thrown` | number | Scalar | false | false | 0 | — | `CleansingThrowItem.use` (success) |
| Cleanse Operations | `cleanse_operations` | number | Scalar | false | false | 0 | — | `EffectCleanser.cleanse` (per entity with removed > 0) |
| Effects Removed | `effects_removed` | number | Scalar | false | false | 0 | — | `EffectCleanser.cleanse` (add `removed`) |
| Lingering Clouds Spawned | `lingering_clouds_spawned` | number | Scalar | false | false | 0 | — | `CleansingPotionEntity.spawnCloud` |
| Potions Brewed | `potions_brewed` | number | Scalar | false | false | 0 | — | `PotionBrewingMixin.mix` (non-empty result). **26.2 only** |
| Cooldown Blocks | `cooldown_blocks` | number | Scalar | false | false | 0 | — | `CleansingThrowItem.use` (cooldown FAIL) |
| Permission Denials | `permission_denials` | number | Scalar | false | false | 0 | — | `PermissionGate.allows` == false branches |
| Commands Executed | `commands_executed` | number | Scalar | false | false | 0 | — | `CleansingCommand.give` / `reload` |
| Config Reloads | `config_reloads` | number | Scalar | false | false | 0 | — | `CleansingPotions.reload` |

---

## B. Breakdowns (Maps)

Per-category counts. Keep these as Maps rather than one source per category so new keys
(e.g. modded effects) don't require new data sources. Values are non-negative whole numbers.

| name | reference_id | type | shape | allowNegative | allowDecimals | min | max | keys |
|---|---|---|---|---|---|---|---|---|
| Cleanses by Mode | `cleanses_by_mode` | number | Map | false | false | 0 | — | `cleansing`, `soothing`, `purging` |
| Potions Used by Variant | `potions_used_by_variant` | number | Map | false | false | 0 | — | `regular`, `splash`, `lingering` |
| Effects Removed by Disposition | `effects_removed_by_disposition` | number | Map | false | false | 0 | — | `beneficial`, `harmful`, `neutral` |
| Effects Removed by Type | `effects_removed_by_type` | number | Map | false | false | 0 | — | effect registry ids, e.g. `minecraft:poison` *(high cardinality — includes modded effects)* |
| Brews by Recipe | `brews_by_recipe` | number | Map | false | false | 0 | — | `cleansing`, `soothing`, `purging`, `splash`, `lingering`. **26.2 only** |

> **Brewing counters stop at 26.2.** From 26.3 brewing is a data pack recipe type and `PotionBrewing`
> is gone, so the mixin that was the only caller of `CleansingMetrics.potionBrewed` no longer ships.
> `potions_brewed` and `brews_by_recipe` therefore report zero from 26.3 clients. Read a decline in
> those two as version mix, not as players brewing less. Restoring them needs a hook into the
> brewing stand's recipe path, which would mean putting a mixin back into the 26.3 jar.

---

## C. Distribution gauges

Per-event measurements FS aggregates (avg / p50 / max). Decimals allowed because averages
are fractional even though each sample is a whole count.

| name | reference_id | type | shape | allowNegative | allowDecimals | min | max | emit point |
|---|---|---|---|---|---|---|---|---|
| Effects Removed per Cleanse | `effects_removed_per_cleanse` | number | Scalar | false | true | 0 | — | `EffectCleanser.cleanse` (sample `removed`) |
| Entities Affected per Throw | `entities_affected_per_throw` | number | Scalar | false | true | 0 | — | `CleansingPotionEntity.applySplash` / cloud tick (count cleansed) |

---

## D. Configuration snapshot

Report once per session/server so you can segment usage by how the mod is configured. These
mirror `CleansingConfig` fields. Booleans use no numeric validation.

| name | reference_id | type | shape | allowNegative | allowDecimals | min | max |
|---|---|---|---|---|---|---|---|
| Cleanse Radius | `cleanse_radius` | number | Scalar | false | true | 0 | 64 *(suggested)* |
| Cooldown Enabled | `cooldown_enabled` | boolean | Scalar | — | — | — | — |
| Cooldown Seconds | `cooldown_seconds` | number | Scalar | false | false | 0 | — |
| Only Thrower | `only_thrower` | boolean | Scalar | — | — | — | — |
| Brewing Enabled | `brewing_enabled` | boolean | Scalar | — | — | — | — |
| Splash Enabled | `splash_enabled` | boolean | Scalar | — | — | — | — |
| Lingering Enabled | `lingering_enabled` | boolean | Scalar | — | — | — | — |
| Permissions Enabled | `permissions_enabled` | boolean | Scalar | — | — | — | — |
| Cleanse Feedback | `cleanse_feedback` | boolean | Scalar | — | — | — | — |
| Impact Particles | `impact_particles` | boolean | Scalar | — | — | — | — |
| Update Checks Enabled | `check_for_updates` | boolean | Scalar | — | — | — | — |
| Force-Remove List | `force_remove_effects` | string | Array | — | — | — | — |
| Force-Keep List | `force_keep_effects` | string | Array | — | — | — | — |

---

## Suggested rollout order

1. **A + B** — the action counters and breakdowns are the headline charts (usage, mode mix,
   which effects players actually cleanse). Start here.
2. **C** — distribution gauges for balance insight (how many effects a cleanse clears, AoE
   reach in practice).
3. **D** — config snapshot, useful once you have traffic and want to segment it.

The emit code (`CleansingMetrics` + `FastStatsBridge`) registers all of the above; once the
sources exist in FS with these `reference_id`s, data starts landing on the next flush. The
whole pipeline is gated on `CleansingConfig.get().metrics` (the same flag that builds the
context), so a player who disables metrics emits nothing.

---

## Error Tracking

Separate FS feature from metrics; **not** a data source you define. Status and setup:

- **Library support:** yes — `dev.faststats` ships `ErrorTracker` / `TrackedError`.
- **Platform:** you must turn on **Error Tracking** in the FS project **Features** settings.
  Captured errors are retained for **30 days**.
- **Code:** `FastStatsBridge.errorTracker()` returns `ErrorTracker.contextAware()` and is
  attached via `.errorTrackerService(...)` on both loaders' context factory. `contextAware()`
  auto-captures uncaught throwables originating from the mod's own classloader. For handled
  exceptions you can also report manually:

  ```java
  FastStatsBridge.errorTracker()
      .trackError(e)
      .handled(true)
      .attributes(dev.faststats.Attributes.empty().put("where", "brewing"));
  ```

  (Any such call must live inside a `//? if >=26.1.2` Stonecutter guard, since `dev.faststats`
  is absent on the 1.21.x nodes.)
- **Before this change** the factory only called `.metrics(...)` — no tracker was attached, so
  error tracking was *not* active in any form (neither automatic nor manual).
