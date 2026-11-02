# FastStats Data Sources: cleansingpotions

## Conventions

- **type**: `number`, `string`, or `boolean`.
- **shape**
  - `Scalar`: one value per report (a counter, gauge, or flag).
  - `Array`: an ordered list of values.
  - `Map`: key → value pairs; validation columns describe the **values**.
- **validation**: `allowNegative` / `allowDecimals` / `min` / `max`. For `boolean` and `string` sources
  these are not applicable (`n/a`). A `min` or `max` of `-` means unbounded.

Built-in environment metrics (`plugin_version`, `game_version`, `platform_version`, `server_type`,
`player_count`, `online_mode`, `client`, Java version and vendor, OS name, version and architecture, and
core count) are collected by the SDK itself and need no data source here. Two SDK changes matter for
dashboards: `minecraft_version` was renamed to `game_version` in SDK 0.28.1, so jars built on an older
SDK still send the old key, and SDK 0.29.4 added a boolean `client` that separates client installs from
servers.

A context submits 30 seconds after it is ready, then every 30 minutes, and once more at shutdown. The
shutdown send is one metrics request, plus one error request when reports are pending. Each request times
out after 3 seconds when the connection or the response headers stall, and that timeout does not cover a
stalled name lookup or a stalled response body. The SDK runs the shutdown send itself on Fabric
(`CLIENT_STOPPING`, `SERVER_STOPPING`) and on a NeoForge dedicated server (`ServerStoppingEvent`), where it
blocks the thread stopping the game until the send ends. A NeoForge client gets it from
`NeoForgeFastStats`, at `GameShuttingDownEvent`, and so does a dedicated server whose run loop crashed,
because a crash skips `ServerStoppingEvent`. There the send runs on a daemon thread, and the thread stopping
the game waits for it for at most 6 seconds, the length of two requests at their timeout. A send still
running after that continues until it finishes or JVM exit abandons it, so its batch can be lost.
Gradle run tasks set `faststats.enabled=false`, so dev runs send nothing unless `-Pfaststats` is passed.

**Error reports are not automatic.** FastStats captures an error on its own only when one kills a
thread and has a stack frame in this mod's package, and the Fabric and NeoForge platform modules register
nothing else (the Paper `ServerExceptionEvent` path is Bukkit only). Anything this mod catches and
recovers from reaches the dashboard only by calling `ErrorReporter.report`. Current report sites:

| source | raised when |
| --- | --- |
| `config:load` | the config file could not be read or parsed, so the previous values stayed in effect (the defaults on the first load) |
| `config:save` | the config could not be written, so an owner's edit was lost |
| `permissions:fabric-permissions-api` | the Fabric Permissions mod is installed but its API does not match, so every permission check returns its default |

Deliberately not reported: the update check failing (offline players and timeouts are not defects),
the toast accessor being absent, and enum parse fallbacks. Noise there buries the reports worth
reading. `/cleanse testerror` sends a report with `intentional=true` and `handled=false`.

The first config load runs before the metrics toggle has been read, so it happens before the FastStats
context exists. `ErrorReporter` holds a report raised then, at most one per source, and flushes it when
the context starts, which is why a malformed config still reports on the boot that found it. That holds
only while the file's text has `"metrics": true`: a first load that fails turns metrics off otherwise,
so no context starts and that boot sends nothing.

**First run and consent.** Clients and servers follow the same rule. The run that creates
`config/faststats/config.properties` submits nothing, and submission starts at the next game start, which
gives an owner the chance to opt out in that file first. The file is shared by every FastStats mod on the
install. A client has one exception, the SDK's consent screen. SDK 0.31.0 opens it by itself only when a
mod asks for it with `requireConsent()`, and this mod does not, but the game's telemetry options screen
always has a FastStats button that opens it. Closing that screen applies its choices at once to every
FastStats context on that client, this one included, so on a first run it can start submission. Its boxes
start ticked on a first run, so closing it unchanged does that. Our own
`metrics` config option decides whether a context is created at all, so a boot with it off keeps this mod
out of that screen.

**Reporting semantics.** A counter reports its total since the last accepted metrics submission. Reading
one (`Counter.peek`) only records the value it returned, because FastStats also runs the suppliers when it
assembles an error report, so a read must not clear anything. `CleansingMetrics.commitAll`, registered
through `Metrics.Factory#onFlush`, runs only after a metrics submission is accepted, and `Counter.commit`
subtracts what the latest read returned. An increment that lands between the read and the flush therefore
stays for the next submission, and a rejected submission keeps all of its counts. A map sends only the
keys whose count is above zero.

---

## A. Action counters (core)

Increment-by-1 (or by N) counters. All are non-negative whole numbers.

| name | reference_id | type | shape | allowNegative | allowDecimals | min | max | emit point |
|---|---|---|---|---|---|---|---|---|
| Potions Consumed | `potions_consumed` | number | Scalar | false | false | 0 | - | `CleansingPotionItem.finishUsingItem` |
| Potions Thrown | `potions_thrown` | number | Scalar | false | false | 0 | - | `CleansingThrowItem.use` (success). A dispensed potion does not count |
| Cleanse Operations | `cleanse_operations` | number | Scalar | false | false | 0 | - | `EffectCleanser.cleanse` (per entity with removed > 0) |
| Effects Removed | `effects_removed` | number | Scalar | false | false | 0 | - | `EffectCleanser.cleanse` (per effect removed) |
| Lingering Clouds Spawned | `lingering_clouds_spawned` | number | Scalar | false | false | 0 | - | `CleansingPotionEntity.spawnCloud`, dispensed potions included |
| Potions Brewed | `potions_brewed` | number | Scalar | false | false | 0 | - | `PotionBrewingMixin.mix` (non-empty result). **26.2 only** |
| Cooldown Blocks | `cooldown_blocks` | number | Scalar | false | false | 0 | - | `CleansingThrowItem.use` (cooldown refusal). Counts each retry while the use button is held |
| Permission Denials | `permission_denials` | number | Scalar | false | false | 0 | - | `CleansingPotionItem.use`, `CleansingThrowItem.use` (`PermissionGate.allowsUse` false). Counts each retry while the use button is held |
| Commands Executed | `commands_executed` | number | Scalar | false | false | 0 | - | `CleansingCommand.give`, `reload`, and `testError` (when a report was queued) |
| Config Reloads | `config_reloads` | number | Scalar | false | false | 0 | - | `CleansingPotions.reload` (successful load only) |

---

## B. Breakdowns (Maps)

Per-category counts. Keep these as Maps rather than one source per category so new keys
(e.g. modded effects) don't require new data sources. Values are non-negative whole numbers.

| name | reference_id | type | shape | allowNegative | allowDecimals | min | max | keys |
|---|---|---|---|---|---|---|---|---|
| Cleanses by Mode | `cleanses_by_mode` | number | Map | false | false | 0 | - | `all` (Cleansing), `negative_only` (Soothing), `positive_only` (Purging) |
| Potions Used by Variant | `potions_used_by_variant` | number | Map | false | false | 0 | - | `regular`, `splash`, `lingering`. Player drinks and throws only |
| Effects Removed by Disposition | `effects_removed_by_disposition` | number | Map | false | false | 0 | - | `beneficial`, `harmful`, `neutral` |
| Effects Removed by Type | `effects_removed_by_type` | number | Map | false | false | 0 | - | effect registry ids, e.g. `minecraft:poison` *(high cardinality, includes modded effects)* |
| Brews by Recipe | `brews_by_recipe` | number | Map | false | false | 0 | - | `cleansing`, `soothing`, `purging`, `splash`, `lingering`. **26.2 only** |

> **Brewing counters stop at 26.2.** On 26.3 brewing is a data pack recipe type and `PotionBrewing` does
> not exist, so the mixin that is the only caller of `CleansingMetrics.potionBrewed` does not ship in the
> 26.3 jars. `potions_brewed` and `brews_by_recipe` therefore report zero from 26.3 installs. Read a
> decline in those two as version mix, not as players brewing less. Feeding them on 26.3 needs a hook into
> the brewing stand's recipe path, which means a mixin in the 26.3 jars.

---

## C. Distribution gauges

Each value is the mean of its samples since the last accepted metrics submission, and `0` when there are
none. Decimals are allowed because a mean is fractional even though each sample is a whole count.

| name | reference_id | type | shape | allowNegative | allowDecimals | min | max | emit point |
|---|---|---|---|---|---|---|---|---|
| Effects Removed per Cleanse | `effects_removed_per_cleanse` | number | Scalar | false | true | 0 | - | `EffectCleanser.cleanse` (sample `removed`, when above zero) |
| Entities Affected per Throw | `entities_affected_per_throw` | number | Scalar | false | true | 0 | - | `CleansingPotionEntity.applySplash` (count cleansed). Splash impacts only, dispensed potions included. A lingering cloud adds no sample |

---

## D. Configuration snapshot

Sent with every submission, read from the live `CleansingConfig` at that moment, so you can segment usage
by how the mod is configured. Booleans use no numeric validation. Loading or saving the config keeps
`cleanse_radius` within 0.5 to 32 and `cooldown_seconds` within 0 to 3600.

| name | reference_id | type | shape | allowNegative | allowDecimals | min | max |
|---|---|---|---|---|---|---|---|
| Cleanse Radius | `cleanse_radius` | number | Scalar | false | true | 0 | 32 |
| Cooldown Enabled | `cooldown_enabled` | boolean | Scalar | n/a | n/a | n/a | n/a |
| Cooldown Seconds | `cooldown_seconds` | number | Scalar | false | false | 0 | 3600 |
| Only Thrower | `only_thrower` | boolean | Scalar | n/a | n/a | n/a | n/a |
| Brewing Enabled | `brewing_enabled` | boolean | Scalar | n/a | n/a | n/a | n/a |
| Splash Enabled | `splash_enabled` | boolean | Scalar | n/a | n/a | n/a | n/a |
| Lingering Enabled | `lingering_enabled` | boolean | Scalar | n/a | n/a | n/a | n/a |
| Permissions Enabled | `permissions_enabled` | boolean | Scalar | n/a | n/a | n/a | n/a |
| Cleanse Feedback | `cleanse_feedback` | boolean | Scalar | n/a | n/a | n/a | n/a |
| Impact Particles | `impact_particles` | boolean | Scalar | n/a | n/a | n/a | n/a |
| Update Checks Enabled | `check_for_updates` | boolean | Scalar | n/a | n/a | n/a | n/a |
| Force-Remove List | `force_remove_effects` | string | Array | n/a | n/a | n/a | n/a |
| Force-Keep List | `force_keep_effects` | string | Array | n/a | n/a | n/a | n/a |

`brewing_enabled`, `splash_enabled`, and `lingering_enabled` mean the same on both 26.2 and 26.3. On 26.2
the brewing mixin reads them live. On 26.3 they gate the JSON recipes through the
`cleansingpotions:brewing` load condition, so the recipes follow at the next data load while these values
already show the new setting. `splash_enabled` and `lingering_enabled` also decide on both versions whether
a potion of that type can be thrown and has any effect.

---

## Suggested rollout order

1. **A + B**: the action counters and breakdowns are the headline charts (usage, mode mix,
   which effects players actually cleanse). Start here.
2. **C**: distribution gauges for balance insight (how many effects a cleanse clears, AoE
   reach in practice).
3. **D**: config snapshot, useful once you have traffic and want to segment it.

The emit code (`CleansingMetrics` + `FastStatsBridge`) registers all of the above; once the sources exist
in FS with these `reference_id`s, data starts landing on the next submission.

**Turning metrics off.** `CleansingConfig.get().metrics` decides at game start whether a context is
created. Turning it off later, through `/cleanse reload` or the config screen, calls `FastStatsBridge.stop`,
which ends all traffic at once: no final batch goes out, queued error reports are dropped, and uncaught
exception tracking is detached. There are two exceptions. Another mod nesting a newer FastStats that lacks
the `SimpleContext.submissionActive` field can let a final batch go out, and the log says so. A request the
`GameShuttingDownEvent` send already has in flight finishes, because `stop` does not wait for that send, and
the send makes no further request. Turning `metrics` back on takes effect at the next game start, because a
stopped context cannot restart. A reload that fails to read the file neither stops nor starts FastStats.

---

## Error Tracking

Separate FS feature from metrics; **not** a data source you define.

- **Platform:** you must turn on **Error Tracking** in the FS project **Features** settings. Captured
  errors are retained for **30 days**.
- **Automatic capture:** `FastStatsBridge.start` attaches `ErrorTracker.contextAware()` through
  `.errorTrackerService(...)` on both loaders. It sees every throwable that kills a thread. The SDK's own
  check only compares class loaders, and every mod and Minecraft share one loader on both platforms, so
  `start` adds an exclusion rule: a throwable is dropped unless it or one of its causes has a stack frame
  in `so.alaz.cleansingpotions`. The rule applies to automatic capture only, so `ErrorReporter.report` and
  `/cleanse testerror` are unaffected.
- **Handled failures:** report them through `ErrorReporter.report(source, error)`, which is safe when the
  nested FastStats mod is absent. It sends at most one report per source per process, because the SDK stores
  each report that carries attributes as its own entry, so a dashboard entry means "this failed at least once
  in that session", not a count of failures. `/cleanse testerror` skips that limit and adds one report per
  command.
- **When reports send:** with the metrics schedule above. A failed send keeps the reports and retries on the
  next cycle, and the send at shutdown is the last attempt. `FastStatsBridge` queues a report only while
  `errorsWillSend` is true. That needs a context, which does not exist when `metrics` was off at game start,
  when FastStats could not start (it is not loaded on this game version, for example), or after
  `FastStatsBridge.stop`. It needs error tracking on, which `enabled=false` or `submitErrors=false` in
  `config/faststats/config.properties` turns off, as does a choice on the FastStats consent screen. It also
  needs submission active, which is false during the run that creates that file until the consent screen
  is closed with metrics or errors ticked, and after the shutdown send.
  With `metrics` off in the config, `/cleanse testerror` answers
  `command.cleansingpotions.testerror_disabled`. With it on while `errorsWillSend` is false, the command
  answers `command.cleansingpotions.testerror_inactive`, which names the first run, the opt-out in that
  file, a stop until the next game start, and FastStats not being loaded on this game version. Neither
  answer queues a report.
- **Context:** each error report also carries the current metric values, unless the owner set
  `submitAdditionalMetrics=false`.
