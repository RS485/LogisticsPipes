/*
 * Copyright (c) 2020  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2020  RS485
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

package network.rs485.logisticspipes.gui.guidebook

import net.minecraft.nbt.NBTTagCompound
import network.rs485.logisticspipes.guidebook.BookContents
import network.rs485.logisticspipes.util.LPDataInput
import network.rs485.logisticspipes.util.LPDataOutput
import network.rs485.logisticspipes.util.LPSerializable
import network.rs485.logisticspipes.util.cycleMinecraftColorId
import network.rs485.logisticspipes.util.math.Rectangle


interface IPageData : LPSerializable {
    var page: String
    var color: Int?
    var progress: Float

    fun fromTag(tag: NBTTagCompound) {
        page = tag.getString("page")
        color = if (tag.hasKey("color")) tag.getInteger("color") else null
        progress = tag.getFloat("progress")
    }

    fun toTag(): NBTTagCompound {
        val nbt = NBTTagCompound()
        nbt.setString("page", page)
        color?.also {
            nbt.setInteger("color", it)
        }
        nbt.setFloat("progress", progress)
        return nbt
    }

    /**
     * Takes in an LPDataOutput buffer and turns a SavedPage object into bytes and puts them inside the buffer.
     * @param output data to send
     */
    override fun write(output: LPDataOutput) = output.writeNBTTagCompound(toTag())

    /**
     * Takes in an LPDataInput buffer and turns the buffered bytes object into a SavedPage object.
     * @param input the received data
     * @return SavedPage object created from the buffered data
     */
    override fun read(input: LPDataInput) {
        input.readNBTTagCompound()?.apply(::fromTag)
    }
}


class PageData : IPageData {

    override var page: String = ""
    override var color: Int? = null
    override var progress: Float = 0.0F

    constructor(input: LPDataInput) {
        read(input)
    }

    constructor(tag: NBTTagCompound) {
        fromTag(tag)
    }

    constructor(page: String) {
        this.page = page
    }

}


class Page(data: PageData) : IPageData by data {

    private val infoProvider = BookContents.get(page)
    private val drawable = BookContents.getDrawablePage(page)

    val title: String
        get() = infoProvider.metadata.title

    fun updateScrollPosition(visibleArea: Rectangle, currentProgress: Float) =
        drawable.updateScrollPosition(visibleArea, currentProgress)

    fun getExtraHeight(visibleArea: Rectangle): Int =
        if (visibleArea.roundedHeight < drawable.height) drawable.height - visibleArea.roundedHeight else 0

    fun mouseClicked(
        mouseX: Float,
        mouseY: Float,
        mouseButton: Int,
        visibleArea: Rectangle,
        guideActionListener: GuiGuideBook.ActionListener,
    ) {
        drawable.getVisibleParagraphs(visibleArea)
            .firstOrNull { it.absoluteBody.contains(mouseX, mouseY) }
            ?.mouseClicked(mouseX, mouseY, mouseButton, guideActionListener)
    }

    fun setDrawablesPosition(area: Rectangle) {
        drawable.setWidth(area.roundedWidth)
        drawable.setPos(area.roundedX, area.roundedY)
    }

    fun isBookmarkable() = infoProvider.bookmarkable

    fun pageEquals(other: Page): Boolean = this.page == other.page

    fun cycleColor(inverted: Boolean = false) =
        cycleMinecraftColorId((color ?: 0), inverted).also { color = it }

    fun draw(visibleArea: Rectangle, mouseX: Float, mouseY: Float, partialTicks: Float) {
        drawable.preRender(mouseX, mouseY, visibleArea)
        drawable.draw(mouseX, mouseY, partialTicks, visibleArea)
    }

}
