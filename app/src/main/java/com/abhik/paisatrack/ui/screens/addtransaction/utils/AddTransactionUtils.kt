package com.abhik.paisatrack.ui.screens.addtransaction.utils

object AddTransactionUtils {

    fun formatAmountWithCommas(raw: String): String {
        val clean = raw.replace(",", "")
        if (clean.isEmpty()) return ""
        val parts = clean.split(".")
        val integerPart = parts[0]

        val formattedInt = if (integerPart.length > 3) {
            val lastThree = integerPart.substring(integerPart.length - 3)
            val rest = integerPart.substring(0, integerPart.length - 3)
            val restFormatted = StringBuilder()
            var count = 0
            for (i in rest.length - 1 downTo 0) {
                if (count > 0 && count % 2 == 0) {
                    restFormatted.append(",")
                }
                restFormatted.append(rest[i])
                count++
            }
            restFormatted.reverse().toString() + "," + lastThree
        } else {
            integerPart
        }

        return if (parts.size > 1) {
            formattedInt + "." + parts[1]
        } else if (clean.contains(".")) {
            "$formattedInt."
        } else {
            formattedInt
        }
    }

    fun mapOffset(text: String, formattedText: String, offset: Int): Int {
        val prefix = text.take(offset)
        val digitsBeforeCursor = prefix.count { it != ',' }
        var formattedSelectionIndex = 0
        var digitsSeen = 0
        while (formattedSelectionIndex < formattedText.length && digitsSeen < digitsBeforeCursor) {
            if (formattedText[formattedSelectionIndex] != ',') {
                digitsSeen++
            }
            formattedSelectionIndex++
        }
        while (formattedSelectionIndex < formattedText.length && formattedText[formattedSelectionIndex] == ',') {
            formattedSelectionIndex++
        }
        return formattedSelectionIndex
    }
}
