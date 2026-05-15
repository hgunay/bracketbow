package com.bracketbow.settings

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.psi.PsiManager
import com.intellij.ui.dsl.builder.*
import javax.swing.JComponent

/**
 * Settings → Tools → Bracketbow page.
 * Built with Kotlin UI DSL v2; apply/reset/isModified are handled by panel delegation.
 */
class BracketbowConfigurable(private val project: Project) : Configurable {

    private var panel: DialogPanel? = null

    override fun getDisplayName(): String = "Bracketbow"

    override fun createComponent(): JComponent {
        val state = BracketbowSettings.getInstance(project).state

        panel = panel {
            group("General") {
                row {
                    checkBox("Enable Bracketbow")
                        .bindSelected(state::enabled)
                }
                row("Color level count") {
                    spinner(3..10, 1)
                        .bindIntValue(state::colorLevels)
                }
                row {
                    comment("Colors cycle back from the beginning after this many levels (modulo)")
                }
            }

            group("Bracket types") {
                row {
                    checkBox("Round brackets ( )")
                        .bindSelected(state::colorRoundBrackets)
                }
                row {
                    checkBox("Square brackets [ ]")
                        .bindSelected(state::colorSquareBrackets)
                }
                row {
                    checkBox("Curly brackets { }")
                        .bindSelected(state::colorCurlyBrackets)
                }
            }

            group("Languages") {
                row {
                    checkBox("Java").bindSelected(state::enableJava)
                    checkBox("Kotlin").bindSelected(state::enableKotlin)
                }
                row {
                    checkBox("XML").bindSelected(state::enableXml)
                    checkBox("HTML").bindSelected(state::enableHtml)
                }
                row {
                    checkBox("JSON").bindSelected(state::enableJson)
                }
            }

            group("Colors") {
                row {
                    comment("To customize each level's color, open the Color Scheme settings.")
                }
                row {
                    button("Edit colors") {
                        ShowSettingsUtil.getInstance().showSettingsDialog(project, "Bracketbow")
                    }
                }
            }

            group("Coming soon") {
                row {
                    comment("These features are not yet available. Vote by checking what matters to you.")
                }
                row { checkBox("Highlight matching bracket pair").enabled(false) }
                row { checkBox("Colorize indent guides").enabled(false) }
                row { checkBox("Colorize HTML/XML tag pairs").enabled(false) }
                row { checkBox("Scope highlighting (cursor inside a bracket group)").enabled(false) }
            }
        }
        return panel!!
    }

    override fun isModified(): Boolean = panel?.isModified() ?: false

    override fun apply() {
        panel?.apply()
        val analyzer = DaemonCodeAnalyzer.getInstance(project)
        val psiManager = PsiManager.getInstance(project)
        FileEditorManager.getInstance(project).openFiles.forEach { virtualFile ->
            psiManager.findFile(virtualFile)?.let { analyzer.restart(it) }
        }
    }

    override fun reset() {
        panel?.reset()
    }
}
