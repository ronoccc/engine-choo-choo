# Quarantined fork cards (post-upstream merge)

These card definitions were authored on the pre-merge fork against an older Argentum DSL
(`Triggers.EntersBattlefield`, `MayEffect`, `PlayerChoiceEffect`, `Costs.UntapPermanents`,
`Keyword.ENCORE` / `DEMONSTRATE`, etc.).

After merging `wingedsheep/argentum-engine` (Oct 2026), the SDK trigger/effect DSL changed.
These files do not compile against current `mtg-sdk` and were moved here so `:game-server`
and set modules stay green.

**Do not delete** — re-port to the current DSL (`Triggers.self.enters()`, etc.) and move
back under the matching `mtg-sets/<era>/src/.../cards/` path.

See `MOVED.txt` for the original paths.
