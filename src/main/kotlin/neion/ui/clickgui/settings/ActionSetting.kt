package neion.ui.clickgui.settings

class ActionSetting(name: String, hidden: Boolean = false, description: String? = null) : Setting(name, hidden, description) { var action: () -> Unit = {} }