package neion.ui.clickgui.settings

open class Setting(
    val name: String,
    val hidden: Boolean = false,
    var description: String? = null) {
    open fun reset() {}
}