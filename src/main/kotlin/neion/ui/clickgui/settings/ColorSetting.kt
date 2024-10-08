package neion.ui.clickgui.settings

import neion.features.ClickGui
import neion.features.RandomStuff
import net.minecraft.util.MathHelper
import java.awt.Color

class ColorSetting(
    name: String,
    val default: Color,
    var allowAlpha: Boolean = true,
    hidden: Boolean = false,
    description: String? = null,
) : Setting(name, hidden, description){

    var value: Color = default
    var hsbvals: FloatArray = Color.RGBtoHSB(default.red, default.green, default.blue, null)

    var red: Int
        get() = value.red
        set(input) {
            value = Color(MathHelper.clamp_int(input,0,255), green, blue, alpha)
        }
    var green: Int
        get() = value.green
        set(input) {
            value = Color(red, MathHelper.clamp_int(input,0,255), blue, alpha)
        }
    var blue: Int
        get() = value.blue
        set(input) {
            value = Color(red, green, MathHelper.clamp_int(input,0,255), alpha)
        }
    val rgb: Int
        get() = value.rgb
    var hue: Float
        get() {
            updateHSB()
            return hsbvals[0]
        }
        set(input) {
            hsbvals[0] = input
            updateColor()
        }
    var saturation: Float
        get() {
            updateHSB()
            return hsbvals[1]
        }
        set(input) {
            hsbvals[1] = input
            updateColor()
        }
    var brightness: Float
        get() {
            updateHSB()
            return hsbvals[2]
        }
        set(input) {
            hsbvals[2] = input
            updateColor()
        }
    var alpha: Int
        get() = value.alpha
        set(input) {
            // prevents changing the alpha if not allowed
            if (!allowAlpha) return
            value = Color(red, green, blue, MathHelper.clamp_int(input,0,255))
        }

    /**
     * Updates the color stored in value from the hsb values stored in hsbvals
     */
    private fun updateColor() {
        val tempColor =  Color(Color.HSBtoRGB(hsbvals[0], hsbvals[1], hsbvals[2]))
        value = Color(tempColor.red, tempColor.green, tempColor.blue, alpha)
    }

    /**
     * Updates the values in the hsbvals field.
     * Use this instead Color.RGBtoHSB(red, green, blue, hsbvals), to avoid setting the hue to 0 when eitehr saturation
     * or brightness are 0.
     */
    private fun updateHSB() {
        val newHSB = Color.RGBtoHSB(red, green, blue, null)
        hsbvals[2] = newHSB[2]
        if (newHSB[2] > 0) {
            hsbvals[1] = newHSB[1]
            if (newHSB[1] > 0) hsbvals[0] = newHSB[0]
        }
    }

    /**
     * Gets the value for the given color.
     */
    fun getNumber(colorNumber: ColorComponent) = when (colorNumber) {
        ColorComponent.HUE -> hue.toDouble()
        ColorComponent.SATURATION -> saturation.toDouble()
        ColorComponent.BRIGHTNESS -> brightness.toDouble()
        ColorComponent.ALPHA -> alpha.toDouble()
    }

    /**
     * Sets the value for the specified color.
     */
    fun setNumber(colorNumber: ColorComponent, number: Double) {
        when (colorNumber) {
            ColorComponent.HUE -> hue = number.toFloat()
            ColorComponent.SATURATION -> saturation = number.toFloat()
            ColorComponent.BRIGHTNESS -> brightness = number.toFloat()
            ColorComponent.ALPHA -> alpha = number.toInt()
        }
    }

    /**
     * Returns an array of the availiable settings. Those are either red, green and blue or red, green, blue and alpha.
     */
    fun colors(): Array<ColorComponent> {
        val tempArr = arrayOf(ColorComponent.HUE, ColorComponent.SATURATION, ColorComponent.BRIGHTNESS)
        return if (allowAlpha) tempArr.plus(ColorComponent.ALPHA) else tempArr
    }

    /**
     * Enum to allow for loop through the values.
     * This is the best solution i could come up with on the spot to circumvent that i cannot pass a reference to the
     * int values.
     */
    enum class ColorComponent {
        HUE, SATURATION, BRIGHTNESS, ALPHA;

        fun getName() = this.toString()

        fun maxValue() = when (this) {
            ALPHA -> 255
            HUE, SATURATION, BRIGHTNESS -> 1
        }
    }

    override fun reset() {
        value = default
        super.reset()
    }
}