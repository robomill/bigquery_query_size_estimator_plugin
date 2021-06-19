package net.chakmidlot.jetbrains.bigquery.plugin

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "net.chakmidlot.jetbrains.bigquery.plugin.SettingsState",
    storages = [Storage("BigQuery_estimator.xml")])
class SettingsState : PersistentStateComponent<StateModel> {
    var data = StateModel()

    override fun getState(): StateModel {
        return this.data
    }

    override fun loadState(state: StateModel) {
        data = state
    }

    companion object {
        @JvmStatic
        fun getInstance(): PersistentStateComponent<StateModel> {
            return ServiceManager.getService(SettingsState::class.java)
        }
    }
}

data class StateModel (
    var applicationDefaultAuthentication: Boolean = false,
    var keyPath: String = "",
)
