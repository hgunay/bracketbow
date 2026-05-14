# Bracketbow

A free, open-source alternative to the Rainbow Brackets plugin for IntelliJ-based IDEs.
Colors `( )`, `[ ]`, `{ }` brackets by nesting depth.

→ [Türkçe README](README.tr.md)

## Requirements

- **JDK 17+** (JDK 21 recommended)
- **IntelliJ IDEA** Community (free) or Ultimate
- Internet connection for the first Gradle run (~500 MB download)

## Quick Start

```bash
# Open the project in IntelliJ (File → Open → this folder)
# Wait for Gradle sync, then:

./gradlew runIde      # Launches a sandbox IDE with the plugin loaded
./gradlew buildPlugin # Produces a distributable .zip
```

Install the built `build/distributions/bracketbow-0.1.0.zip` in your real IDE via:
`Settings → Plugins → ⚙ → Install Plugin from Disk...`

> On Windows use `gradlew.bat runIde`

## Settings

`Settings → Tools → Bracketbow`

- **Enable/disable** the plugin globally
- **Color level count** — how many levels before colors cycle (3–10)
- **Bracket types** — toggle round `( )`, square `[ ]`, and curly `{ }` independently
- **Languages** — enable/disable per language (Java, Kotlin, XML, HTML, JSON)
- **Edit colors** — opens `Settings → Editor → Color Scheme → Bracketbow`

## Architecture

Code is split into packages by responsibility:

```
src/main/kotlin/com/bracketbow/
├── psi/
│   ├── BracketDetector.kt    ← Is this element a bracket? (pure logic)
│   └── DepthCalculator.kt    ← How deep is this bracket nested? (pure logic)
├── colors/
│   ├── BracketbowColors.kt              ← Color level definitions
│   └── BracketbowColorSettingsPage.kt   ← Color Scheme settings page
├── settings/
│   ├── BracketbowSettings.kt            ← PersistentStateComponent
│   └── BracketbowConfigurable.kt        ← Settings UI (Kotlin UI DSL v2)
└── annotator/
    └── BracketbowAnnotator.kt           ← Thin coordinator
```

Each file does exactly one thing. Classes in `psi/` have no IntelliJ API dependency beyond `PsiElement` and are easily unit-testable. The annotator just wires the pieces together.

### Data flow

```
IntelliJ              BracketbowAnnotator     psi/                  colors/
  │                          │                   │                      │
  │── annotate(element) ────►│                   │                      │
  │                          │── isBracketLeaf() ►│                      │
  │                          │◄── true/false ─────│                      │
  │                          │── depthOf(element) ►│                      │
  │                          │◄── int ─────────────│                      │
  │                          │── forDepth(depth) ───────────────────────►│
  │                          │◄── TextAttributesKey ────────────────────│
  │◄── annotation ───────────│                   │                      │
```

### How depth is calculated

For the innermost `(` in `((()))`:

1. `BracketDetector.isBracketLeaf` → `true`
2. `DepthCalculator.depthOf`:
   - parent #1: innermost paren expression → `isBracketGroup` true → depth 0
   - parent #2: middle paren expression → true → depth 1
   - parent #3: outermost paren expression → true → depth 2
   - parent #4: file/statement → false → stop
3. `BracketbowColors.forDepth(2)` → `BRACKETBOW_LEVEL_2`
4. IntelliJ applies the corresponding color

## Development

```bash
./gradlew runIde       # Live test in sandbox IDE
./gradlew buildPlugin  # Build distributable zip
./gradlew verifyPlugin # JetBrains compatibility check
./gradlew clean        # Fix stuck Gradle builds
```

> Requires JDK 17. If your active JDK is newer and Gradle fails, prefix with:
> `JAVA_HOME=/path/to/jdk17 ./gradlew ...`

## License

MIT — see [LICENSE](LICENSE).
