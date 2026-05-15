package com.bracketbow.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

/**
 * Plugin ayarlarını proje başına saklar (.idea/bracketbow.xml).
 * PersistentStateComponent olduğu için IDE yeniden başlatmaya karşı kalıcıdır.
 */
@Service(Service.Level.PROJECT)
@State(
    name = "BracketbowSettings",
    storages = [Storage("bracketbow.xml")]
)
class BracketbowSettings(private val project: Project) : PersistentStateComponent<BracketbowSettings.State> {

    data class State(
        var enabled: Boolean = true,
        var colorLevels: Int = 7,
        var colorRoundBrackets: Boolean = true,
        var colorSquareBrackets: Boolean = true,
        var colorCurlyBrackets: Boolean = true,
        var enableJava: Boolean = true,
        var enableKotlin: Boolean = true,
        var enableXml: Boolean = true,
        var enableHtml: Boolean = true,
        var enableJson: Boolean = true
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    companion object {
        fun getInstance(project: Project): BracketbowSettings =
            project.getService(BracketbowSettings::class.java)
                ?: error("BracketbowSettings service not registered")
    }
}