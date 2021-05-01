/*
 * Copyright (c) 2021  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2021  RS485
 *
 * This MIT license was reworded to only match this file. If you use the regular
 * MIT license in your project, replace this copyright notice (this line and any
 * lines below and NOT the copyright line above) with the lines from the original
 * MIT license located here: http://opensource.org/licenses/MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this file and associated documentation files (the "Source Code"), to deal in
 * the Source Code without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Source Code, and to permit persons to whom the Source Code is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Source Code, which also can be
 * distributed under the MIT.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package network.rs485.logisticspipes.util

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.resources.I18n
import net.minecraft.item.ItemStack
import net.minecraft.util.text.TextFormatting
import java.text.NumberFormat
import java.util.*

object TextUtil {

    private const val holdShiftTooltip: String = "misc.holdshift"
    private val numberPrefixes: List<Pair<Double, String>> = listOf(1e0 to "", 1e3 to "k", 1e6 to "M", 1e9 to "G", 1e12 to "T", 1e15 to "P", 1e18 to "E")
    private val formattingState: EnumSet<TextFormatting> = EnumSet.noneOf(TextFormatting::class.java)
    private val baseFormattingState: EnumSet<TextFormatting> = EnumSet.noneOf(TextFormatting::class.java)
    private val regexPattern =
            TextFormatting.values().joinToString("|", prefix = "(\\$)(", postfix = ")") { it.friendlyName.toUpperCase() }
                    .toRegex()

    @JvmStatic
    fun translate(
            key: String,
            vararg args: String
    ): String =
            translate(
                    key = key,
                    baseFormatting = EnumSet.noneOf(TextFormatting::class.java),
                    args = args
            )

    @JvmStatic
    fun translate(
        key: String,
        baseFormatting: EnumSet<TextFormatting> = EnumSet.noneOf(TextFormatting::class.java),
        prepend: String = "",
        append: String = "",
        args: Array<out String>,
    ): String = transform(prepend + I18n.format(key, *args) + append, baseFormatting)

    @JvmStatic
    fun getTrimmedString(text: String, maxWidth: Int, fontRenderer: FontRenderer, postfix: CharSequence = "..."): String {
        if (fontRenderer.getStringWidth(text) < maxWidth) return text
        var result = ""
        text.takeWhile { char ->
            (fontRenderer.getStringWidth(result + postfix) < maxWidth).also { fitsInWidth ->
                if (fitsInWidth) {
                    result += char
                }
            }
        }
        return result.trim() + postfix
    }


    /**
     *  Turns the long value into a formatted number string following the metric prefixes.
     *  The result is limited to 3 digits and to achieve it when the number is above 100 in
     *  each scale it will be translated to the next higher scale as so: 0Px where P is the
     *  higher prefix and x is the value in the hundreds equivalent to the previous prefix.
     *  @param number to be formatted.
     *  @param forceDisplayNumber whether 1 should return an empty string or itself.
     *  @return 3 digit string, not constrained but should never exceed it.
     */
    @JvmStatic
    fun getThreeDigitFormattedNumber(number: Long, forceDisplayNumber: Boolean): String {
        return numberPrefixes.firstOrNull { prefix ->
            number == 0L || (number >= prefix.first * 0.1 && number < prefix.first * 100)
        }?.let { prefix ->
            when {
                number == 1L && !forceDisplayNumber -> ""
                number < 1000 -> number.toString() // Don't touch less than 3 digit values
                else -> (number / prefix.first).toInt().toString() + prefix.second + if (number > 10 * prefix.first) {
                    ""
                } else {
                    ((number % prefix.first) / (prefix.first / 10)).toInt().takeIf { decimal -> decimal > 0 }?: ""
                }
            }
        } ?: "NaN"
    }

    @JvmStatic
    fun addTooltipInformation(stack: ItemStack, tooltip: MutableList<String>, extended: Boolean){
        if(extended) {
            var tooltipLine = 1
            while(I18n.hasKey("${stack.unlocalizedName}.tip$tooltipLine")){
                tooltip += translate("${stack.unlocalizedName}.tip$tooltipLine")
                tooltipLine++
            }
        } else {
            if(I18n.hasKey("${stack.unlocalizedName}.tip1")){
                tooltip += translate(holdShiftTooltip)
            }
        }
    }

    /**
     * Logistics Pipes localization files accept special (more descriptive)
     * formatting tags this method is supposed to turn them into minecraft
     * font renderer compatible tags. baseFormatting will be preserved over $RESET tags.
     * @param text to be formatted
     * @param baseFormatting to be applied at the start of the string and will be preserved throughout.
     * @return formatted string ready to be rendered by Minecraft's font renderer.
     */
    fun transform(text: String, baseFormatting: EnumSet<TextFormatting>): String {
        baseFormattingState.clear()
        baseFormattingState.addAll(baseFormatting)
        formattingState.clear()
        var result = text.prependIndent(baseFormattingState.getColorTag() + baseFormattingState.getFormattingTags())
        while (regexPattern.containsMatchIn(result)) {
            result = regexPattern.replace(
                    input = result,
                    transform = { matchResult ->
                        getReplacementString(matchResult.getTextFormatting())
                    }
            )
        }
        return result
    }

    @JvmStatic
    fun formatNumberWithCommas(number: Long): String =
            NumberFormat.getNumberInstance(Minecraft.getMinecraft().languageManager.currentLanguage.javaLocale).format(number)

    private fun getReplacementString(formatting: TextFormatting): String {
        if (formatting == TextFormatting.RESET) {
            formattingState.clear()
            return formatting.toString() + baseFormattingState.getColorTag() + baseFormattingState.getFormattingTags()
        }
        if (formatting.isColor) {
            formattingState.removeAll { it.isColor }
        }
        formattingState.add(formatting)
        return formattingState.getColorTag() + formattingState.getFormattingTags()
    }

    private fun MatchResult.getTextFormatting(): TextFormatting = TextFormatting.getValueByName(value.toLowerCase())!!

    private fun EnumSet<TextFormatting>.getColorTag(): String =
            this.firstOrNull { it.isColor }?.toString() ?: baseFormattingState.firstOrNull { it.isColor }?.toString()
            ?: ""

    private fun EnumSet<TextFormatting>.getFormattingTags(): String =
            (this + baseFormattingState).filter { it.isFancyStyling }.joinToString(separator = "") { it.toString() }
}