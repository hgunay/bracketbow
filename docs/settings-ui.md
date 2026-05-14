# Settings UI Design Spec

This document specifies the plugin settings screen. Claude Code (or any developer) should follow this when implementing the Configurable. The visual reference is in [settings-ui-mockup.html](settings-ui-mockup.html).

## Location in IDE

**`Settings → Tools → Bracketbow`**

Register in `plugin.xml`:

```xml
<projectConfigurable
    parentId="tools"
    instance="com.bracketbow.settings.BracketbowConfigurable"
    id="com.bracketbow.settings"
    displayName="Bracketbow"
    nonDefaultProject="false"/>
```

Use `applicationConfigurable` if we decide settings are global rather than per-project. Default: **project-level** (most users want per-project preferences).

## Implementation outline

Three classes in a new `com.bracketbow.settings` package:

1. **`BracketbowSettings`** — `PersistentStateComponent<BracketbowSettings.State>` service. Holds the state and serializes to `bracketbow.xml` in the project's `.idea/` folder.
2. **`BracketbowConfigurable`** — `Configurable` that builds the UI using Kotlin UI DSL v2 (`com.intellij.ui.dsl.builder.panel`).
3. **`BracketbowSettings.State`** — data class with `@Property` annotations for serialization.

Register `BracketbowSettings` as a project service in `plugin.xml`:

```xml
<projectService serviceImplementation="com.bracketbow.settings.BracketbowSettings"/>
```

## State shape

```kotlin
data class State(
    var enabled: Boolean = true,
    var colorLevels: Int = 7,
    var colorRoundBrackets: Boolean = true,   // ( )
    var colorSquareBrackets: Boolean = true,  // [ ]
    var colorCurlyBrackets: Boolean = true,   // { }
    var enableJava: Boolean = true,
    var enableKotlin: Boolean = true,
    var enableXml: Boolean = true,
    var enableHtml: Boolean = true,
    var enableJson: Boolean = true
)
```

## Sections (top to bottom)

### 1. General

- **Checkbox:** "Enable Bracketbow"
  - Binds: `state.enabled`
  - When unchecked, annotator returns early without highlighting (master switch)

- **Slider with label:** "Color level count" — range 3 to 10, default 7
  - Binds: `state.colorLevels`
  - Hint text below: "Colors cycle back from the beginning after this many levels (modulo)"
  - Use `intTextField()` with `JBSlider` or just a `spinner` if DSL doesn't have native slider

### 2. Bracket types

Three checkboxes (all default true):

- "Round brackets `( )`" → `state.colorRoundBrackets`
- "Square brackets `[ ]`" → `state.colorSquareBrackets`
- "Curly brackets `{ }`" → `state.colorCurlyBrackets`

### 3. Languages

Group with five checkboxes in a 2-column grid layout (all default true):

- "Java" → `state.enableJava`
- "Kotlin" → `state.enableKotlin`
- "XML" → `state.enableXml`
- "HTML" → `state.enableHtml`
- "JSON" → `state.enableJson`

### 4. Colors

- Hint: "To customize each level's color, open the Color Scheme settings."
- Button: "Edit colors"
  - On click: navigate to Color Scheme settings page
  - Use: `ShowSettingsUtil.getInstance().showSettingsDialog(project, "Bracketbow")`
  - Or: `Settings → Editor → Color Scheme → Bracketbow`

### 5. Coming soon

Disabled section with checkboxes that don't bind to state — purely informational. These represent the roadmap.

- "Highlight matching bracket pair"
- "Colorize indent guides"
- "Colorize HTML/XML tag pairs"
- "Scope highlighting (cursor inside a bracket group)"

Hint above: "These features are not yet available. Vote by checking what matters to you."

When a feature ships, move its checkbox to the appropriate section above and remove from this list.

## Annotator integration

`BracketbowAnnotator.annotate()` must consult settings before applying:

```kotlin
override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    val settings = element.project.service<BracketbowSettings>().state
    if (!settings.enabled) return

    if (!BracketDetector.isBracketLeaf(element)) return

    // Check bracket type
    val ch = element.text[0]
    val typeEnabled = when (ch) {
        '(', ')' -> settings.colorRoundBrackets
        '[', ']' -> settings.colorSquareBrackets
        '{', '}' -> settings.colorCurlyBrackets
        else -> false
    }
    if (!typeEnabled) return

    val depth = DepthCalculator.depthOf(element)
    val colorKey = BracketbowColors.forDepth(depth, settings.colorLevels)
    // ... rest same as before
}
```

Update `BracketbowColors.forDepth()` to accept a `levels` parameter:

```kotlin
fun forDepth(depth: Int, levels: Int = LEVELS.size): TextAttributesKey {
    val effectiveLevels = levels.coerceIn(3, LEVELS.size)
    val safeDepth = if (depth < 0) 0 else depth
    return LEVELS[safeDepth % effectiveLevels]
}
```

**Note on language toggles:** Language enable/disable is trickier because annotators are registered per-language in `plugin.xml` and we can't unregister at runtime. The pragmatic approach: still register all annotators, but have `annotate()` check the file's language against settings and return early. Get the language from `element.containingFile.language.id` (e.g. "JAVA", "kotlin", "JSON").

## Triggering re-annotation on settings change

After `apply()` in the Configurable, re-highlight open files:

```kotlin
override fun apply() {
    // ... save state ...
    DaemonCodeAnalyzer.getInstance(project).restart()
}
```

Without this, changes don't reflect until the user types or reopens the file.

## Acceptance criteria

- [x] Settings page appears at `Settings → Tools → Bracketbow`
- [x] All controls visible and labeled in English
- [x] Changing the master toggle disables/enables coloring across all open files
- [x] Color level spinner affects how many colors cycle (verifiable by setting to 3 and seeing repetition)
- [x] Disabling a bracket type leaves those brackets uncolored while others remain colored
- [x] Disabling a language leaves that language's files uncolored
- [x] "Edit colors" button opens the Color Scheme page
- [x] State persists across IDE restart
- [x] "Apply" applies immediately (no need to reopen files)
- [x] "Reset" reverts unsaved changes

## Open questions

These are decisions deferred until implementation; resolve with the project owner:

1. **Project-level vs application-level settings?** Current spec says project-level. If user prefers global, switch to `applicationConfigurable` and `applicationService`.
2. **Where to put master toggle?** Currently in "General" section. Could also live as a status bar widget for quick toggling.
3. **Should disabled languages still appear in the list?** Some users may have only IntelliJ IDEA without certain plugins (e.g., no Python plugin). The current spec lists all known languages regardless.
