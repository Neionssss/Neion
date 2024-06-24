package neion.ui.clickgui.settings

class BooleanSetting (
    name: String,
    var enabled: Boolean = false,
    hidden: Boolean = false,
    description: String? = null,
): Setting(name, hidden, description) {

    fun toggle() {
        enabled = !enabled
    }
}