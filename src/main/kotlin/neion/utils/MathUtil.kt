package neion.utils

object MathUtil {

    // Thanks UnBloomedClaim6
    fun fn(num: Int): String {
        return num.toString().replace("(\\d)(?=(\\d{3})+(?!\\d))".toRegex(), "$1,")
    }

    /**
     * This code was converted to Kotlin and taken under CC BY-SA 3.0 license
     * @link https://stackoverflow.com/a/9073310
     */
    fun String.romanToDecimal(): Int {
        var decimal = 0
        var lastNumber = 0
        val romanNumeral = this.uppercase()
        for (x in romanNumeral.length - 1 downTo 0) {
            when (romanNumeral[x]) {
                'M' -> {
                    decimal = processDecimal(1000, lastNumber, decimal)
                    lastNumber = 1000
                }
                'D' -> {
                    decimal = processDecimal(500, lastNumber, decimal)
                    lastNumber = 500
                }
                'C' -> {
                    decimal = processDecimal(100, lastNumber, decimal)
                    lastNumber = 100
                }
                'L' -> {
                    decimal = processDecimal(50, lastNumber, decimal)
                    lastNumber = 50
                }
                'X' -> {
                    decimal = processDecimal(10, lastNumber, decimal)
                    lastNumber = 10
                }
                'V' -> {
                    decimal = processDecimal(5, lastNumber, decimal)
                    lastNumber = 5
                }
                'I' -> {
                    decimal = processDecimal(1, lastNumber, decimal)
                    lastNumber = 1
                }
            }
        }
        return decimal
    }

    private fun processDecimal(decimal: Int, lastNumber: Int, lastDecimal: Int): Int {
        return if (lastNumber > decimal) lastDecimal - decimal else lastDecimal + decimal
    }
}