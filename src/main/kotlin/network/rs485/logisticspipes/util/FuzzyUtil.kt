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

import logisticspipes.utils.item.ItemIdentifier
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import network.rs485.logisticspipes.property.IBitSet
import java.util.*

enum class FuzzyFlag(val bit: Int, val nbtName: String) {
    IGNORE_DAMAGE(1, "ignore_dmg"),
    IGNORE_NBT(2, "ignore_nbt"),
    USE_ORE_DICT(0, "use_od"),
    USE_ORE_CATEGORY(3, "use_category")
}

typealias FuzzyFlagger = (flag: FuzzyFlag) -> Boolean

object FuzzyUtil {

    fun readFromNBT(fuzzyBitSet: IBitSet, tag: NBTTagCompound) =
        FuzzyFlag.values().forEach { fuzzyBitSet[it.bit] = tag.getBoolean(it.nbtName) }

//    fun writeToNBT(fuzzyBitSet: IBitSet, tag: NBTTagCompound) =
//        FuzzyFlag.values().forEach { tag.setBoolean(it.nbtName, fuzzyBitSet[it.bit]) }

    fun set(fuzzyBitSet: IBitSet, flag: FuzzyFlag, value: Boolean) = fuzzyBitSet.set(flag.bit, value)
    fun set(fuzzyBitSet: BitSet, flag: FuzzyFlag, value: Boolean) = fuzzyBitSet.set(flag.bit, value)
    fun get(fuzzyBitSet: IBitSet, flag: FuzzyFlag): Boolean = fuzzyBitSet[flag.bit]
    fun get(fuzzyBitSet: BitSet, flag: FuzzyFlag): Boolean = fuzzyBitSet[flag.bit]
    fun getter(fuzzyBitSet: IBitSet): FuzzyFlagger = { get(fuzzyBitSet, it) }
    fun getter(fuzzyBitSet: BitSet): FuzzyFlagger = { get(fuzzyBitSet, it) }

    fun fuzzyMatches(
        fuzzyFlagger: FuzzyFlagger,
        firstItem: ItemIdentifier,
        secondItem: ItemIdentifier,
    ): Boolean {
        val useOreCategory = fuzzyFlagger(FuzzyFlag.USE_ORE_CATEGORY)
        if (fuzzyFlagger(FuzzyFlag.USE_ORE_DICT) || useOreCategory) {
            val firstDictIdent = firstItem.dictIdentifiers
            val secondDictIdent = secondItem.dictIdentifiers
            if (firstDictIdent != null && secondDictIdent != null) {
                if (firstDictIdent.canMatch(secondDictIdent, true, useOreCategory)) {
                    return true
                }
            }
        }
        val firstStack: ItemStack = firstItem.makeNormalStack(1)
        val secondStack: ItemStack = secondItem.makeNormalStack(1)
        if (firstStack.item !== secondStack.item) {
            return false
        }
        if (firstStack.itemDamage != secondStack.itemDamage) {
            if (firstStack.hasSubtypes) {
                return false
            } else if (!fuzzyFlagger(FuzzyFlag.IGNORE_DAMAGE)) {
                return false
            }
        }
        if (fuzzyFlagger(FuzzyFlag.IGNORE_NBT)) {
            return true
        }
        if (firstStack.hasTagCompound() xor secondStack.hasTagCompound()) {
            return false
        }
        return if (!firstStack.hasTagCompound() && !secondStack.hasTagCompound()) {
            true
        } else ItemStack.areItemStackTagsEqual(firstStack, secondStack)
    }

}
