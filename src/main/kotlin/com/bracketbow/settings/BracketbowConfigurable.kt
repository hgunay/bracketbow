package com.bracketbow.settings

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.*
import javax.swing.JComponent

/**
 * Settings → Tools → Bracketbow sayfasını oluşturur.
 * Kotlin UI DSL v2 ile panel inşa edilir; apply/reset/isModified
 * panel delegasyonuyla otomatik çalışır.
 */
class BracketbowConfigurable(private val project: Project) : Configurable {

    private var panel: DialogPanel? = null

    override fun getDisplayName(): String = "Bracketbow"

    override fun createComponent(): JComponent {
        val state = BracketbowSettings.getInstance(project).state

        panel = panel {
            group("Genel") {
                row {
                    checkBox("Bracketbow'u etkinleştir")
                        .bindSelected(state::enabled)
                }
                row("Renk seviyesi sayısı") {
                    spinner(3..10, 1)
                        .bindIntValue(state::colorLevels)
                }
                row {
                    comment("Bu sayıdan sonra renkler baştan başlar (modulo)")
                }
            }

            group("Parantez türleri") {
                row {
                    checkBox("Yuvarlak parantez ( )")
                        .bindSelected(state::colorRoundBrackets)
                }
                row {
                    checkBox("Köşeli parantez [ ]")
                        .bindSelected(state::colorSquareBrackets)
                }
                row {
                    checkBox("Süslü parantez { }")
                        .bindSelected(state::colorCurlyBrackets)
                }
            }

            group("Diller") {
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

            group("Renkler") {
                row {
                    comment("Her seviyenin rengini düzenlemek için renk şemasına git.")
                }
                row {
                    button("Renkleri düzenle") {
                        ShowSettingsUtil.getInstance().showSettingsDialog(project, "Bracketbow")
                    }
                }
            }

            group("Yakında geliyor") {
                row {
                    comment("Bu özellikler henüz eklenmedi; aşağıdaki listeden hangilerini öncelik vereceğimizi seçeceğiz.")
                }
                row { checkBox("Eşleşen parantez çiftini vurgula").enabled(false) }
                row { checkBox("Indent guide'ları renklendir").enabled(false) }
                row { checkBox("HTML/XML etiket çiftlerini renklendir").enabled(false) }
                row { checkBox("Scope vurgulama (imleç bir parantez grubunda)").enabled(false) }
            }
        }
        return panel!!
    }

    override fun isModified(): Boolean = panel?.isModified() ?: false

    override fun apply() {
        panel?.apply()
        DaemonCodeAnalyzer.getInstance(project).restart()
    }

    override fun reset() {
        panel?.reset()
    }
}