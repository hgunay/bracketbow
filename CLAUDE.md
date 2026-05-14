# Bracketbow

IntelliJ Platform tabanlı IDE'ler için açık kaynaklı bir parantez renklendirme plugin'i. Rainbow Brackets'a ücretsiz alternatif olarak yazıldı.

## Project status

- **Version:** 0.1.0 (initial release, henüz marketplace'te yayınlanmadı)
- **Working:** Java, Kotlin, XML, HTML, JSON için parantez renklendirme; 7 seviye renk; Color Scheme entegrasyonu
- **In progress:** Plugin settings UI (PersistentStateComponent + Configurable)
- **Not started:** Diğer diller, matching bracket highlight, indent guides, scope highlighting, HTML/XML tag coloring, unit tests

`CHANGELOG.md`'deki "Planlanan" listesi sürekli güncellenmeli.

## Architecture

Sorumluluğa göre paketlere ayrılmış:

```
src/main/kotlin/com/bracketbow/
├── psi/           — Pure logic, only depends on com.intellij.psi.PsiElement
│   ├── BracketDetector.kt    — "Is this element a bracket?"
│   └── DepthCalculator.kt    — "How deep is this bracket nested?"
├── colors/        — Color management
│   ├── BracketbowColors.kt              — TextAttributesKey definitions
│   └── BracketbowColorSettingsPage.kt   — Color Scheme settings UI
└── annotator/     — IDE integration
    └── BracketbowAnnotator.kt           — Thin coordinator
```

**Key principle:** Annotator is intentionally tiny — all real logic lives in `psi/` and `colors/`. This keeps the IDE integration boundary thin and makes the logic unit-testable without an IntelliJ instance.

**Depth calculation:** `DepthCalculator.depthOf()` walks PSI tree upward, counting ancestors whose first child is an opening bracket and last child is a matching closing bracket. Starts at -1 to not count the bracket's own wrapper.

## Build & Run

JDK 17+ required (project uses toolchain 17). IntelliJ Platform Gradle Plugin 2.1.0.

```bash
./gradlew runIde          # Sandbox IDE with plugin loaded — for manual testing
./gradlew buildPlugin     # Produces build/distributions/bracketbow-0.1.0.zip
./gradlew verifyPlugin    # JetBrains plugin verifier
./gradlew clean           # If Gradle gets stuck, this fixes most things
```

Target platform: IntelliJ IDEA Community 2024.2.4 (sinceBuild=242, untilBuild=252.*). Update `build.gradle.kts` to bump versions.

## Conventions

- **Package:** `com.bracketbow.{annotator,colors,psi}` — no `.plugin` subpackage
- **Class names:** `Bracketbow*` for plugin-specific classes (`BracketbowAnnotator`, `BracketbowColors`). Pure utilities get descriptive names (`BracketDetector`, `DepthCalculator`).
- **TextAttributesKey external names:** `BRACKETBOW_LEVEL_N` — these are user-visible in the Color Scheme settings and persist in user config files. **Do not rename across versions** without a migration.
- **Resource files:** kebab-case for plugin config (`bracketbow-java.xml`), PascalCase for color schemes (`BracketbowDefault.xml`)
- **Comments:** Turkish in source comments is fine — the project author prefers it. English for public-facing strings (description, change notes).
- **Kotlin style:** standard idiomatic Kotlin. `object` for stateless utilities. No mutable global state.

## Technical context

- **IntelliJ Platform Plugin SDK** docs: https://plugins.jetbrains.com/docs/intellij/welcome.html
- **PSI (Program Structure Interface):** IntelliJ's parsed representation of source code. Each language has its own PSI element types.
- **Annotator vs HighlightVisitor:** We use `Annotator` because it's per-language and integrates cleanly with the highlighting pipeline. Don't switch to `HighlightVisitor` without a clear reason.
- **Language registration:** Annotators must be registered per-language in `plugin.xml`. There's no wildcard `language=""` that works. Optional plugin dependencies (Java, Kotlin) live in separate config files referenced via `<depends optional="true" config-file="...">`.
- **Color defaults:** Provided via `<additionalTextAttributes>` extension point referencing XML files in `src/main/resources/colorSchemes/`. The XML format is sensitive — copy from existing `BracketbowDefault.xml` when adding new attribute keys.

## Common tasks

### Add support for a new language

For languages always present (e.g., a new always-bundled file type):

```xml
<!-- in src/main/resources/META-INF/plugin.xml, extensions block -->
<annotator language="Python"
           implementationClass="com.bracketbow.annotator.BracketbowAnnotator"/>
```

For languages requiring optional plugins:

1. Create `src/main/resources/META-INF/bracketbow-python.xml` with just the annotator registration
2. Add `<depends optional="true" config-file="bracketbow-python.xml">com.intellij.modules.python</depends>` to main `plugin.xml`
3. Add `bundledPlugin("com.intellij.modules.python")` to `build.gradle.kts` dependencies block (for `runIde` to work)

### Add a new color level

1. Add `level(N)` call to `LEVELS` array in `BracketbowColors.kt`
2. Add `<option name="BRACKETBOW_LEVEL_N">` entry to both XML color scheme files
3. The color settings page auto-picks up new levels via `mapIndexed`

### Add a new setting

1. Create/extend `BracketbowSettings` (PersistentStateComponent) in a new `settings/` package
2. Create `BracketbowConfigurable` using Kotlin UI DSL
3. Register both as services in `plugin.xml`
4. Have `BracketbowAnnotator` read settings via `service<BracketbowSettings>()` before annotating

## Gotchas

- **Annotator runs per PSI element** — keep `annotate()` fast. Avoid expensive PSI traversals; cache where possible.
- **PSI structure differs across languages** — depth calculation depends on parsers producing parent elements with bracket-wrapped first/last children. This is true for most languages but if you see wrong depths, check the PSI tree with **PsiViewer** plugin.
- **`firstChild == lastChild` edge case:** A single-leaf bracket pair shouldn't increment depth. `BracketDetector.isBracketGroup()` guards against this.
- **Don't reproduce Rainbow Brackets' logic verbatim** — that plugin is commercial and likely has copyright protections on its specific code. We re-implement the concept independently.
- **`runIde` first run is slow** — downloads ~500MB IntelliJ sandbox. Subsequent runs are seconds.
- **Color scheme XML files do not validate strictly** — typos in attribute key names silently fail (no colors applied). Always double-check the `name=` attribute matches the Kotlin code exactly.

## Out of scope

- Code formatting / linting / refactoring help — we only highlight existing brackets
- Bracket completion (typing `(` auto-inserting `)`) — IntelliJ has this natively
- Replacing Java's built-in matching bracket highlight (the one that highlights the bracket under cursor) — that's a separate platform feature, we may add a complementary version later

## When user asks

- **"How do I test this?"** → Run `./gradlew runIde`, open a `.kt`/`.java`/`.json` file in the sandbox, look for colored brackets. See README's "Test et" section.
- **"How do I package?"** → `./gradlew buildPlugin`, the zip is in `build/distributions/`.
- **"How do I install in my real IDE?"** → Settings → Plugins → ⚙ → Install Plugin from Disk, point to the built zip.
- **"It doesn't work"** → Check sandbox IDE's Event Log first. Most issues are Gradle sync or plugin.xml typos.

## Links

- [README.md](README.md) — user-facing setup guide
- [CHANGELOG.md](CHANGELOG.md) — version history
- [JetBrains Platform Docs](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [IntelliJ Platform Gradle Plugin 2.x](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html)
