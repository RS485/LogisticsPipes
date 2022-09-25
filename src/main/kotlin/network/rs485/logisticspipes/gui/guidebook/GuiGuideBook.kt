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

import logisticspipes.LPItems
import logisticspipes.LogisticsPipes
import logisticspipes.modplugins.jei.JEIPluginLoader
import logisticspipes.utils.Color
import logisticspipes.utils.MinecraftColor
import logisticspipes.utils.gui.SimpleGraphics
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiConfirmOpenLink
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.ItemStack
import network.rs485.logisticspipes.gui.HorizontalAlignment
import network.rs485.logisticspipes.gui.LPGuiDrawer
import network.rs485.logisticspipes.gui.VerticalAlignment
import network.rs485.logisticspipes.gui.widget.Tooltipped
import network.rs485.logisticspipes.guidebook.BookContents
import network.rs485.logisticspipes.guidebook.BookContents.MAIN_MENU_FILE
import network.rs485.logisticspipes.guidebook.DebugPage
import network.rs485.logisticspipes.guidebook.ItemGuideBook
import network.rs485.logisticspipes.util.cycleMinecraftColorId
import network.rs485.logisticspipes.util.math.Rectangle
import network.rs485.markdown.TextFormat
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.net.URI
import java.net.URISyntaxException
import java.util.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


object GuideBookConstants {
    // Z Levels
    const val Z_TOOLTIP = 500.0f // Tooltip z

    // Debug constant
    const val DRAW_BODY_WIREFRAME = false
}

class GuiGuideBook(private val state: ItemGuideBook.GuideBookState) : GuiScreen() {

    /*
    TODO after first deployment:
    - Page history with back and forwards functionality.
    - Crafting recipes?
    - Use translatable names or block/item identifiers as text?
    - DrawableListParagraph
    - Add configurability to images
    - Create tooltip object and render it independently of the Drawable it's attached to
    - Create a variety of tooltips with configurable style and information.
    - Improve font renderer to allow for more fonts.
    - Add config screen for the book to change font/font size.
    - Add option to enable or disable text justification.
     */

    // Gui Frame Constants
    private val guiBorderThickness = 16
    private val guiShadowThickness = 6
    private val guiSeparatorThickness = 6

    // Slider
    private val guiSliderWidth = 12

    // Tabs
    private val guiTabWidth = 24
    private val maxTabs = 100

    // Gui constrains
    private val innerGui = Rectangle()
    private val outerGui = Rectangle()
    private val sliderSeparator = Rectangle()
    private val visibleArea = Rectangle()

    // Drawing vars
    private var guiSliderX = 0
    private var guiSliderY0 = 0
    private var guiSliderY1 = 0

    // Buttons
    private lateinit var slider: SliderButton
    private lateinit var home: HomeButton

    private lateinit var addOrRemoveTabButton: BookmarkManagingButton

    // initialize tabs from the stack NBT
    private val tabButtons = state.bookmarks.map(::createGuiTabButton).toMutableList()
    private var freeColor: Int = state.bookmarks.maxOfOrNull { it.color ?: 0 } ?: 0

    // initialize cached pages with initial open page
    private val cachedPages = state.bookmarks.plus(state.currentPage).associateBy { it.page }.toMutableMap()

    private val actionListener = ActionListener()

    private var currentProgress: Float = state.currentPage.progress

    inner class ActionListener {
        fun onMenuButtonClick(newPage: String) = changePage(newPage)
        fun onPageLinkClick(newPage: String) = changePage(newPage)
        fun onWebLinkClick(webLink: String) {
            try {
                this@GuiGuideBook.clickedLinkURI = URI(webLink)
                mc.displayGuiScreen(GuiConfirmOpenLink(this@GuiGuideBook, webLink, 31102009, false))
            } catch (error: URISyntaxException) {
                LogisticsPipes.log.warn("Could not parse link $webLink in GuiGuideBook", error)
            }
        }

        fun onItemLinkClick(stack: ItemStack) {
            JEIPluginLoader.showRecipe(stack)
        }
    }

    init {
        if (LogisticsPipes.isDEBUG()) {
            val debugSavedPage = cachedPages.getOrPut(DebugPage.FILE) { Page(PageData(DebugPage.FILE)) }
            debugSavedPage.color = 0
            tabButtons.add(createGuiTabButton(debugSavedPage))
        }
    }

    private fun changePage(path: String) {
        val newPage = cachedPages.getOrPut(path) { Page(PageData(path)) }
        state.currentPage = newPage
        currentProgress = state.currentPage.progress
        newPage.setDrawablesPosition(visibleArea)
        updateButtonVisibility()
    }

    // (Re)calculates gui element sizes and positions, this is run on gui init
    private fun calculateGuiConstraints() {
        val marginRatio = 1.0 / 8.0
        val sizeRatio = 6.0 / 8.0
        outerGui.setPos(floor(marginRatio * width).toInt(), floor(marginRatio * height).toInt())
            .setSize((sizeRatio * width).toInt(), (sizeRatio * height).toInt())
        innerGui.setPos(outerGui.x0 + guiBorderThickness, outerGui.y0 + guiBorderThickness)
            .setSize(outerGui.roundedWidth - 2 * guiBorderThickness, outerGui.roundedHeight - 2 * guiBorderThickness)
        sliderSeparator.setPos(innerGui.x1 - guiSliderWidth - guiSeparatorThickness - guiShadowThickness, innerGui.y0 - 1)
            .setSize(2 * guiShadowThickness + guiSeparatorThickness, innerGui.roundedHeight + 2)
        guiSliderX = innerGui.roundedRight - guiSliderWidth
        guiSliderY0 = innerGui.roundedTop
        guiSliderY1 = innerGui.roundedBottom
        visibleArea.setPos(innerGui.x0 + guiShadowThickness, innerGui.y0)
            .setSize(innerGui.roundedWidth - sliderSeparator.roundedWidth - guiSliderWidth, innerGui.roundedHeight)
        state.currentPage.setDrawablesPosition(visibleArea)
        updateButtonVisibility()
    }

    // Checks each button for visibility and updates tab positions.
    private fun updateButtonVisibility() {
        if (this::home.isInitialized) home.visible = state.currentPage.page != MAIN_MENU_FILE
        if (this::slider.isInitialized) {
            slider.updateSlider(state.currentPage.getExtraHeight(visibleArea), state.currentPage.progress)
        }
        var xOffset = 0
        for (button: TabButton in tabButtons) {
            button.setPos(outerGui.roundedRight - 2 - 2 * guiTabWidth - xOffset, outerGui.roundedTop)
            xOffset += guiTabWidth
        }
        if (this::addOrRemoveTabButton.isInitialized) {
            addOrRemoveTabButton.updateState()
            addOrRemoveTabButton.setX(outerGui.roundedRight - 20 - guiTabWidth - xOffset)
        }
    }

    private fun isTabAbsent(page: Page): Boolean = state.bookmarks.none { it.pageEquals(page) }

    override fun initGui() {
        calculateGuiConstraints()
        slider = addButton(
            SliderButton(
                x = innerGui.roundedRight - guiSliderWidth,
                y = innerGui.roundedTop,
                railHeight = innerGui.roundedHeight,
                width = guiSliderWidth,
                progress = state.currentPage.progress,
                setProgressCallback = { progress -> state.currentPage.progress = progress }
            )
        )
        home = addButton(
            HomeButton(
                x = outerGui.roundedRight,
                y = outerGui.roundedTop
            ) { mouseButton ->
                if (mouseButton == 0) {
                    changePage(MAIN_MENU_FILE)
                }
                return@HomeButton true
            }
        )
        addOrRemoveTabButton = addButton(
            BookmarkManagingButton(
                x = outerGui.roundedRight - 18 - guiTabWidth + 4,
                y = outerGui.roundedTop - 2,
                onClickAction = { buttonState ->
                    when (buttonState) {
                        BookmarkManagingButton.ButtonState.ADD -> addBookmark().let { true }
                        BookmarkManagingButton.ButtonState.REMOVE -> removeBookmark(state.currentPage)
                        BookmarkManagingButton.ButtonState.DISABLED -> false
                    }
                },
                additionStateUpdater = {
                    when {
                        state.currentPage.isBookmarkable() && isTabAbsent(state.currentPage) -> BookmarkManagingButton.ButtonState.ADD
                        !isTabAbsent(state.currentPage) -> BookmarkManagingButton.ButtonState.REMOVE
                        else -> BookmarkManagingButton.ButtonState.DISABLED
                    }
                }
            )
        )
        updateButtonVisibility()
    }

    override fun onGuiClosed() {
        LPItems.itemGuideBook.saveState(state)
        if (LogisticsPipes.isDEBUG()) {
            BookContents.clear()
        }
        super.onGuiClosed()
    }

    override fun onResize(mcIn: Minecraft, w: Int, h: Int) {
        state.currentPage.progress = 0.0f
        super.onResize(mcIn, w, h)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        GlStateManager.enableDepth()
        GlStateManager.depthFunc(GL11.GL_ALWAYS)
        SimpleGraphics.drawGradientRect(0, 0, width, height, Color.BLANK, Color.BLANK, 450.0)

        LPGuiDrawer.drawGuideBookBackground(outerGui)

        GlStateManager.depthFunc(GL11.GL_LEQUAL)
        state.currentPage.run {
            updateScrollPosition(visibleArea, currentProgress)
            draw(visibleArea, mouseX.toFloat(), mouseY.toFloat(), partialTicks)
        }
        GlStateManager.depthFunc(GL11.GL_ALWAYS)
        tabButtons.forEach { it.drawButton(mc, mouseX, mouseY, partialTicks) }
        LPGuiDrawer.drawGuideBookFrame(outerGui, sliderSeparator)
        buttonList.forEach { it.drawButton(mc, mouseX, mouseY, partialTicks) }

        (buttonList + tabButtons).reversed().forEach { it.drawButtonForegroundLayer(mouseX, mouseY) }
        GlStateManager.depthFunc(GL11.GL_LEQUAL)

        if (visibleArea.contains(mouseX, mouseY)) {
            val hovered = state.currentPage.getHovered(mouseX.toFloat(), mouseY.toFloat())
            hovered?.also {
                if (it is Tooltipped) {
                    LPGuiDrawer.drawTextTooltip(
                        text = it.getTooltipText(),
                        x = mouseX,
                        y = min(mouseY - 5f, visibleArea.bottom).roundToInt(),
                        z = GuideBookConstants.Z_TOOLTIP,
                        horizontalAlign = HorizontalAlignment.CENTER,
                        verticalAlign = VerticalAlignment.BOTTOM,
                    )
                }
            }
        }
        drawTitle()
    }

    override fun doesGuiPauseGame() = false

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val allButtons = (buttonList + tabButtons).sortedBy { it.zLevel }.filter { it.visible && it.enabled }
        for (button in allButtons) {
            if (button.mousePressed(mc, mouseX, mouseY)) {
                selectedButton = button
                when (mouseButton) {
                    0 -> {
                        actionPerformed(button)
                        return
                    }

                    1 -> {
                        rightClick(button)
                        return
                    }
                }
            }
        }
        if (visibleArea.contains(mouseX, mouseY)) {
            state.currentPage.mouseClicked(mouseX.toFloat(), mouseY.toFloat(), mouseButton, visibleArea, actionListener)
        }
    }

    override fun updateScreen() {
        if (currentProgress == state.currentPage.progress) {
            return
        }
        val progressDiff = currentProgress - state.currentPage.progress
        val speedModifier = 0.5f
        currentProgress = when {
            progressDiff < 0.0025f && progressDiff > -0.0025f -> {
                state.currentPage.progress
            }

            progressDiff < 0.0025f -> {
                min(currentProgress - (progressDiff * speedModifier), state.currentPage.progress)
            }

            progressDiff > -0.0025f -> {
                max(currentProgress - (progressDiff * speedModifier), state.currentPage.progress)
            }

            else -> currentProgress
        }
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        if (state.currentPage.getExtraHeight(visibleArea) > 0) {
            val mouseDWheel = Mouse.getDWheel() / -120
            if (mouseDWheel != 0) {
                slider.changeProgress(mouseDWheel * LPGuiDrawer.lpFontRenderer.getFontHeight(1.0f))
            }
        }
    }

    override fun actionPerformed(button: GuiButton) {
        when (button) {
            home -> if (home.click(0)) {
                button.playPressSound(mc.soundHandler)
            }

            addOrRemoveTabButton -> if (addOrRemoveTabButton.click(0)) {
                button.playPressSound(mc.soundHandler)
            }

            is TabButton -> if (button.onLeftClick()) {
                button.playPressSound(mc.soundHandler)
            }
        }
        updateButtonVisibility()
    }

    private fun rightClick(button: GuiButton) {
        when (button) {
            is TabButton -> {
                if (button.onRightClick(shiftClick = isShiftKeyDown(), ctrlClick = isCtrlKeyDown())) {
                    button.playPressSound(mc.soundHandler)
                }
            }
        }
        updateButtonVisibility()
    }

    private fun addBookmark() = state.currentPage.takeIf { isTabAbsent(it) && tabButtons.size < maxTabs }
        ?.also { state.bookmarks.add(it); tabButtons.add(createGuiTabButton(it)) }

    private fun createGuiTabButton(tabPage: Page): TabButton =
        TabButton(tabPage, outerGui.roundedRight - 2 - 2 * guiTabWidth, outerGui.roundedTop, object : TabButtonReturn {
            override fun onLeftClick(): Boolean {
                if (!isPageActive()) {
                    changePage(tabPage.page)
                    return true
                }
                return false
            }

            override fun onRightClick(shiftClick: Boolean, ctrlClick: Boolean): Boolean {
                if (!isPageActive()) return false
                if (ctrlClick && shiftClick) {
                    removeBookmark(tabPage)
                } else {
                    tabPage.cycleColor(inverted = shiftClick)
                }
                return true
            }

            override fun getColor(): Int =
                tabPage.color ?: cycleMinecraftColorId(freeColor).also { freeColor = it; tabPage.color = it }

            override fun isPageActive(): Boolean = tabPage.pageEquals(state.currentPage)
        })

    private fun removeBookmark(page: Page): Boolean {
        val removedFromState = state.bookmarks.removeIf { it.pageEquals(page) }
        val removedFromButtons = tabButtons.removeIf { it.tabPage.pageEquals(page) }
        return removedFromState || removedFromButtons
    }

    private fun drawTitle() {
        LPGuiDrawer.lpFontRenderer.drawCenteredString(
            state.currentPage.title,
            floor(width / 2.0f),
            outerGui.y0 + (innerGui.y0 - outerGui.y0 - LPGuiDrawer.lpFontRenderer.getFontHeight()) / 2.0f,
            MinecraftColor.WHITE.colorCode,
            EnumSet.of(TextFormat.Shadow),
            1.0f
        )
    }

}
