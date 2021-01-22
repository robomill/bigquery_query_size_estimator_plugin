package net.chakmidlot.jetbrains.bigquery.plugin
import com.intellij.openapi.options.Configurable
import javax.swing.*
import javax.swing.JPanel

import com.intellij.ui.components.JBLabel

import com.intellij.util.ui.FormBuilder

import com.intellij.ui.components.JBTextField

class Settings: Configurable {

    private var form: JPanel? = null
    private val keyPath = JBTextField()

    override fun createComponent(): JComponent {
        val settings = SettingsState.getInstance()
        keyPath.text = settings.state?.keyPath

        form = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("GCP key path:"), keyPath, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        return form!!
    }

    override fun isModified(): Boolean {
        val settings = SettingsState.getInstance()
        return settings.state?.keyPath != this.keyPath.text
    }

    override fun apply() {
        val settings = SettingsState.getInstance()
        settings.state?.keyPath = keyPath.text
    }

    override fun getDisplayName(): String {
        return "BigQuery GCP key"
    }
}