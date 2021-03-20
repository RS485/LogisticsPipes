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

import logisticspipes.LPConstants
import logisticspipes.LPItems
import logisticspipes.LogisticsPipes
import logisticspipes.utils.MinecraftColor
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiConfirmOpenLink
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import network.rs485.logisticspipes.gui.HorizontalAlignment
import network.rs485.logisticspipes.gui.LPFontRenderer
import network.rs485.logisticspipes.gui.VerticalAlignment
import network.rs485.logisticspipes.guidebook.BookContents
import network.rs485.logisticspipes.guidebook.BookContents.MAIN_MENU_FILE
import network.rs485.logisticspipes.guidebook.DebugPage
import network.rs485.logisticspipes.guidebook.ItemGuideBook
import network.rs485.logisticspipes.util.*
import network.rs485.logisticspipes.util.math.Rectangle
import network.rs485.markdown.TextFormat
import network.rs485.markdown.defaultDrawableState
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.net.URI
import java.net.URISyntaxException
import java.util.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min


object GuideBookConstants {
    val guiBookTexture = ResourceLocation(LPConstants.LP_MOD_ID, "textures/gui/guide_book.png")

    // Texture
    private const val ATLAS_WIDTH = 256.0
    private const val ATLAS_HEIGHT = 256.0
    const val ATLAS_WIDTH_SCALE = 1 / ATLAS_WIDTH
    const val ATLAS_HEIGHT_SCALE = 1 / ATLAS_HEIGHT

    // Z Levels
    const val Z_TOOLTIP = 32.0f // Tooltip z
    const val Z_TITLE_BUTTONS = 31.0f // Title and Buttons Z
    const val Z_FRAME = 30.0f // Frame Z
    const val Z_TEXT = 5.0f // Text/Information Z
    const val Z_BACKGROUND = 0.0f // Background Z

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
    - Scroll Wheel functionality
     */

    // Gui Frame Constants
    private val guiBorderThickness = 16
    private val guiShadowThickness = 6
    private val guiSeparatorThickness = 6
    private val guiBorderWithShadowThickness = guiBorderThickness + guiShadowThickness
    private val guiAtlasSize = 64
    private val innerFrameTexture = Rectangle(guiBorderWithShadowThickness, guiBorderWithShadowThickness, guiAtlasSize - (guiBorderWithShadowThickness * 2), guiAtlasSize - (guiBorderWithShadowThickness * 2))
    private val outerFrameTexture = Rectangle(0, 0, guiAtlasSize, guiAtlasSize)
    private val sliderSeparatorTexture = Rectangle(96, 65, 16, 30)
    private val backgroundFrameTexture = Rectangle(64, 0, 32, 32)

    // Slider
    private val guiSliderWidth = 12
    private val guiSliderHeight = 15

    // Tabs
    private val guiTabWidth = 24
    private val guiTabHeight = 24
    private val guiFullTabHeight = 32
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
        outerGui.setPos(floor(marginRatio * width).toInt(), floor(marginRatio * height).toInt()).setSize((sizeRatio * width).toInt(), (sizeRatio * height).toInt())
        innerGui.setPos(outerGui.x0 + guiBorderThickness, outerGui.y0 + guiBorderThickness).setSize(outerGui.roundedWidth - 2 * guiBorderThickness, outerGui.roundedHeight - 2 * guiBorderThickness)
        sliderSeparator.setPos(innerGui.x1 - guiSliderWidth - guiSeparatorThickness - guiShadowThickness, innerGui.y0).setSize(2 * guiShadowThickness + guiSeparatorThickness, innerGui.roundedHeight)
        guiSliderX = innerGui.roundedRight - guiSliderWidth
        guiSliderY0 = innerGui.roundedTop
        guiSliderY1 = innerGui.roundedBottom
        visibleArea.setPos(innerGui.x0 + guiShadowThickness, innerGui.y0).setSize(innerGui.roundedWidth - sliderSeparator.roundedWidth - guiSliderWidth, innerGui.roundedHeight)
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
        buttonList.forEach { it.drawButton(mc, mouseX, mouseY, partialTicks) }
        state.currentPage.updateScrollPosition(visibleArea, currentProgress)
        state.currentPage.draw(visibleArea, mouseX, mouseY, partialTicks)
        drawGui()
        if (tabButtons.isNotEmpty()) tabButtons.forEach { it.drawButton(mc, mouseX, mouseY, partialTicks) }
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
            state.currentPage.mouseClicked(mouseX, mouseY, visibleArea, actionListener)
        }
    }

    override fun updateScreen() {
        val progressDiff = currentProgress - state.currentPage.progress
        val speedModifier = 0.5f
        if (progressDiff < 0.05f) {
            currentProgress = min(currentProgress - (progressDiff * speedModifier), state.currentPage.progress)
        } else if (progressDiff > 0.05f) {
            currentProgress = max(currentProgress - (progressDiff * speedModifier), state.currentPage.progress)
        }
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        if (state.currentPage.getExtraHeight(visibleArea) > 0) {
            val mouseDWheel = Mouse.getDWheel() / -120
            if (mouseDWheel != 0) {
                slider.changeProgress(mouseDWheel * lpFontRenderer.getFontHeight(1.0f))
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

    private fun addBookmark() = state.currentPage.takeIf { isTabAbsent(it) && tabButtons.size < maxTabs }?.also { state.bookmarks.add(it); tabButtons.add(createGuiTabButton(it)) }

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

            override fun getColor(): Int = tabPage.color ?: cycleMinecraftColorId(freeColor).also { freeColor = it; tabPage.color = it }

            override fun isPageActive(): Boolean = tabPage.pageEquals(state.currentPage)
        })

    private fun removeBookmark(page: Page): Boolean {
        val removedFromState = state.bookmarks.removeIf { it.pageEquals(page) }
        val removedFromButtons = tabButtons.removeIf { it.tabPage.pageEquals(page) }
        return removedFromState || removedFromButtons
    }

    private fun drawTitle() {
        lpFontRenderer.zLevel = GuideBookConstants.Z_TITLE_BUTTONS
        lpFontRenderer.drawCenteredString(state.currentPage.title, floor(width / 2.0f), outerGui.y0 + (innerGui.y0 - outerGui.y0 - lpFontRenderer.getFontHeight()) / 2.0f, MinecraftColor.WHITE.colorCode, EnumSet.of(TextFormat.Shadow), 1.0f)
        lpFontRenderer.zLevel = GuideBookConstants.Z_TEXT
    }

    private fun drawGui() {
        Minecraft.getMinecraft().renderEngine.bindTexture(GuideBookConstants.guiBookTexture)
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        val tessellator = Tessellator.getInstance()
        val bufferBuilder = tessellator.buffer
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
        // Background
        putTexturedRectangle(
            bufferBuilder = bufferBuilder,
            x0 = innerGui.x0,
            y0 = innerGui.y0,
            x1 = innerGui.x1,
            y1 = innerGui.y1,
            z = GuideBookConstants.Z_BACKGROUND,
            u0 = backgroundFrameTexture.roundedLeft,
            v0 = backgroundFrameTexture.roundedTop,
            u1 = backgroundFrameTexture.roundedRight,
            v1 = backgroundFrameTexture.roundedBottom
        )
        // Corners: TopLeft, TopRight, BottomLeft & BottomRight
        putTexturedRectangle(
            bufferBuilder = bufferBuilder,
            x0 = outerGui.x0,
            y0 = outerGui.y0,
            x1 = innerGui.x0 + guiShadowThickness,
            y1 = innerGui.y0 + guiShadowThickness,
            z = GuideBookConstants.Z_FRAME,
            u0 = outerFrameTexture.roundedLeft,
            v0 = outerFrameTexture.roundedTop,
            u1 = innerFrameTexture.roundedLeft,
            v1 = innerFrameTexture.roundedTop
        )
        putTexturedRectangle(
            bufferBuilder = bufferBuilder,
            x0 = innerGui.x1 - guiShadowThickness,
            y0 = outerGui.y0,
            x1 = outerGui.x1,
            y1 = innerGui.y0 + guiShadowThickness,
            z = GuideBookConstants.Z_FRAME,
            u0 = innerFrameTexture.roundedRight,
            v0 = outerFrameTexture.roundedTop,
            u1 = outerFrameTexture.roundedRight,
            v1 = innerFrameTexture.roundedTop
        )
        putTexturedRectangle(
            bufferBuilder = bufferBuilder,
            x0 = outerGui.x0,
            y0 = innerGui.y1 - guiShadowThickness,
            x1 = innerGui.x0 + guiShadowThickness,
            y1 = outerGui.y1,
            z = GuideBookConstants.Z_FRAME,
            u0 = outerFrameTexture.roundedLeft,
            v0 = innerFrameTexture.roundedBottom,
            u1 = innerFrameTexture.roundedLeft,
            v1 = outerFrameTexture.roundedBottom
        )
        putTexturedRectangle(
            bufferBuilder = bufferBuilder,
            x0 = innerGui.x1 - guiShadowThickness,
            y0 = innerGui.y1 - guiShadowThickness,
            x1 = outerGui.x1,
            y1 = outerGui.y1,
            z = GuideBookConstants.Z_FRAME,
            u0 = innerFrameTexture.roundedRight,
            v0 = innerFrameTexture.roundedBottom,
            u1 = outerFrameTexture.roundedRight,
            v1 = outerFrameTexture.roundedBottom
        )
        // Edges: Top, Bottom, Left & Right
        putTexturedRectangle(
            bufferBuilder = bufferBuilder,
            x0 = innerGui.x0 + guiShadowThickness,
            y0 = outerGui.y0,
            x1 = innerGui.x1 - guiShadowThickness,
            y1 = innerGui.y0 + guiShadowThickness,
            z = GuideBookConstants.Z_FRAME,
            u0 = innerFrameTexture.roundedLeft,
            v0 = outerFrameTexture.roundedTop,
            u1 = innerFrameTexture.roundedRight,
            v1 = innerFrameTexture.roundedTop
        )
        putTexturedRectangle(
            bufferBuilder = bufferBuilder,
            x0 = innerGui.x0 + guiShadowThickness,
            y0 = innerGui.y1 - guiShadowThickness,
            x1 = innerGui.x1 - guiShadowThickness,
            y1 = outerGui.y1,
            z = GuideBookConstants.Z_FRAME,
            u0 = innerFrameTexture.roundedLeft,
            v0 = innerFrameTexture.roundedBottom,
            u1 = innerFrameTexture.roundedRight,
            v1 = outerFrameTexture.roundedBottom
        )
        putTexturedRectangle(
            bufferBuilder = bufferBuilder,
            x0 = outerGui.x0,
            y0 = innerGui.y0 + guiShadowThickness,
            x1 = innerGui.x0 + guiShadowThickness,
            y1 = innerGui.y1 - guiShadowThickness,
            z = GuideBookConstants.Z_FRAME,
            u0 = outerFrameTexture.roundedLeft,
            v0 = innerFrameTexture.roundedTop,
            u1 = innerFrameTexture.roundedLeft,
            v1 = innerFrameTexture.roundedBottom
        )
        putTexturedRectangle(
            bufferBuilder = bufferBuilder,
            x0 = innerGui.x1 - guiShadowThickness,
            y0 = innerGui.y0 + guiShadowThickness,
            x1 = outerGui.x1,
            y1 = innerGui.y1 - guiShadowThickness,
            z = GuideBookConstants.Z_FRAME,
            u0 = innerFrameTexture.roundedRight,
            v0 = innerFrameTexture.roundedTop,
            u1 = outerFrameTexture.roundedRight,
            v1 = innerFrameTexture.roundedBottom
        )
        // Slider Separator
        putTexturedRectangle(
            bufferBuilder = bufferBuilder,
            x0 = sliderSeparator.x0,
            y0 = sliderSeparator.y0 - 1,
            x1 = sliderSeparator.x1,
            y1 = sliderSeparator.y0,
            z = GuideBookConstants.Z_FRAME,
            u0 = sliderSeparatorTexture.roundedLeft,
            v0 = sliderSeparatorTexture.roundedTop - 1,
            u1 = sliderSeparatorTexture.roundedRight,
            v1 = sliderSeparatorTexture.roundedTop
        )
        putTexturedRectangle(
            bufferBuilder = bufferBuilder,
            x0 = sliderSeparator.x0,
            y0 = sliderSeparator.y0,
            x1 = sliderSeparator.x1,
            y1 = sliderSeparator.y1,
            z = GuideBookConstants.Z_FRAME,
            u0 = sliderSeparatorTexture.roundedLeft,
            v0 = sliderSeparatorTexture.roundedTop,
            u1 = sliderSeparatorTexture.roundedRight,
            v1 = sliderSeparatorTexture.roundedBottom
        )
        putTexturedRectangle(
            bufferBuilder = bufferBuilder,
            x0 = sliderSeparator.x0,
            y0 = sliderSeparator.y1,
            x1 = sliderSeparator.x1,
            y1 = sliderSeparator.y1 + 1,
            z = GuideBookConstants.Z_FRAME,
            u0 = sliderSeparatorTexture.roundedLeft,
            v0 = sliderSeparatorTexture.roundedBottom,
            u1 = sliderSeparatorTexture.roundedRight,
            v1 = sliderSeparatorTexture.roundedBottom + 1
        )
        tessellator.draw()
        GlStateManager.disableBlend()
    }

    companion object {
        val lpFontRenderer = LPFontRenderer.get("ter-u12n")

        fun drawSliderButton(body: Rectangle, texture: Rectangle) {
            val z = GuideBookConstants.Z_TITLE_BUTTONS
            val bufferBuilder = startBuffer()
            putTexturedRectangle(
                bufferBuilder,
                body.x0,
                body.y0,
                body.x1,
                body.y0 + 2,
                z,
                texture.roundedLeft,
                texture.roundedTop,
                texture.roundedRight,
                texture.roundedTop + 2
            )
            putTexturedRectangle(
                bufferBuilder,
                body.x0,
                body.y0 + 2,
                body.x1,
                body.y1 - 2,
                z,
                texture.roundedLeft,
                texture.roundedTop + 2,
                texture.roundedRight,
                texture.roundedBottom - 2,
                MinecraftColor.WHITE.colorCode
            )
            putTexturedRectangle(
                bufferBuilder,
                body.x0,
                body.y1 - 2,
                body.x1,
                body.y1,
                z,
                texture.roundedLeft,
                texture.roundedBottom - 2,
                texture.roundedRight,
                texture.roundedBottom
            )
            drawBuffer()
        }

        /**
         * Draws a rectangle in which the given texture will be stretched to the given sized. This method assumes the bound texture is 256x256 in size.
         * @param x0            left x position of desired rectangle.
         * @param y0            top y position of desired rectangle.
         * @param x1            right position of desired rectangle.
         * @param y1            bottom position of desired rectangle.
         * @param z             z position of desired rectangle.
         * @param x0            left correspondent texture position.
         * @param y0            top correspondent texture position.
         * @param x1            right correspondent texture position.
         * @param y1            bottom correspondent texture position.
         */
        fun drawStretchingRectangle(x0: Float, y0: Float, x1: Float, y1: Float, z: Float, u0: Int, v0: Int, u1: Int, v1: Int, blend: Boolean) {
            drawStretchingRectangle(x0, y0, x1, y1, z, u0, v0, u1, v1, blend, MinecraftColor.WHITE.colorCode)
        }

        fun drawStretchingRectangle(rectangle: Rectangle, z: Float, texture: Rectangle, blend: Boolean, color: Int) {
            drawStretchingRectangle(rectangle.x0, rectangle.y0, rectangle.x1, rectangle.y1, z, texture.roundedLeft, texture.roundedTop, texture.roundedRight, texture.roundedBottom, blend, color)
        }

        /**
         * Draws a rectangle in which the given texture will be repeated to the given size it. This method assumes the bound texture is 256x256 in size.
         * @param x0            left x position of desired rectangle.
         * @param y0            top y position of desired rectangle.
         * @param x1            right position of desired rectangle.
         * @param y1            bottom position of desired rectangle.
         * @param z             z position of desired rectangle.
         * @param x0            left correspondent texture position.
         * @param y0            top correspondent texture position.
         * @param x1            right correspondent texture position.
         * @param y1            bottom correspondent texture position.
         */
        private fun drawStretchingRectangle(x0: Float, y0: Float, x1: Float, y1: Float, z: Float, u0: Int, v0: Int, u1: Int, v1: Int, blend: Boolean, color: Int) {
            Minecraft.getMinecraft().renderEngine.bindTexture(GuideBookConstants.guiBookTexture)
            // Four vertices of square following order: TopLeft, TopRight, BottomLeft, BottomRight
            if (blend) GlStateManager.enableBlend()
            if (blend) GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            val tessellator = Tessellator.getInstance()
            val bufferBuilder = tessellator.buffer
            bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
            putTexturedRectangle(bufferBuilder, x0, y0, x1, y1, z, u0, v0, u1, v1, color)
            tessellator.draw()
            if (blend) GlStateManager.disableBlend()
        }

        private fun putTexturedImage(bufferBuilder: BufferBuilder, x0: Float, y0: Float, x1: Float, y1: Float, z: Float, uw: Int, vh: Int, u0: Int, v0: Int, u1: Int, v1: Int) {
            val atlasWidthScale = 1 / uw.toDouble()
            val atlasHeightScale = 1 / vh.toDouble()
            val u0S = u0 * atlasWidthScale
            val v0S = v0 * atlasHeightScale
            val u1S = u1 * atlasWidthScale
            val v1S = v1 * atlasHeightScale
            bufferBuilder.pos(x0, y1, z).tex(u0S, v1S).endVertex()
            bufferBuilder.pos(x1, y1, z).tex(u1S, v1S).endVertex()
            bufferBuilder.pos(x1, y0, z).tex(u1S, v0S).endVertex()
            bufferBuilder.pos(x0, y0, z).tex(u0S, v0S).endVertex()
        }

        /**
         * Adds a textured rectangle to the given buffer. This method assumes the bound texture is 256x256 in size.
         * @param bufferBuilder buffer that needs to be initialized before it is given to this method;
         * @param area          defines position and size of the desired rectangle;
         * @param textureArea   defines position and size of the desired rectangle's texture;
         * @param z             defines z level of the desired rectangle.
         */
        private fun putTexturedRectangle(bufferBuilder: BufferBuilder, area: Rectangle, textureArea: Rectangle, z: Float) {
            putTexturedRectangle(bufferBuilder, area.x0, area.y0, area.x1, area.y1, z, textureArea.roundedLeft, textureArea.roundedTop, textureArea.roundedRight, textureArea.roundedBottom)
        }

        private fun putTexturedRectangle(bufferBuilder: BufferBuilder, x0: Float, y0: Float, x1: Float, y1: Float, z: Float, u0: Int, v0: Int, u1: Int, v1: Int) {
            putTexturedRectangle(bufferBuilder, x0, y0, x1, y1, z, u0, v0, u1, v1, MinecraftColor.WHITE.colorCode)
        }

        /**
         * Adds a textured rectangle to the given buffer. This method assumes the bound texture is 256x256 in size.
         * @param bufferBuilder buffer that needs to be initialized before it is given to this method;
         * @param x0            left x position of desired rectangle;
         * @param y0            top y position of desired rectangle;
         * @param x1            right position of desired rectangle;
         * @param y1            bottom position of desired rectangle;
         * @param z             z position of desired rectangle;
         * @param x0            left correspondent texture position;
         * @param y0            top correspondent texture position;
         * @param x1            right correspondent texture position;
         * @param y1            bottom correspondent texture position.
         */
        private fun putTexturedRectangle(bufferBuilder: BufferBuilder, x0: Float, y0: Float, x1: Float, y1: Float, z: Float, u0: Int, v0: Int, u1: Int, v1: Int, color: Int) {
            val r = color.red()
            val g = color.green()
            val b = color.blue()
            val a = color.alpha()
            // Scaled
            val u0S = u0 * GuideBookConstants.ATLAS_WIDTH_SCALE
            val v0S = v0 * GuideBookConstants.ATLAS_HEIGHT_SCALE
            val u1S = u1 * GuideBookConstants.ATLAS_WIDTH_SCALE
            val v1S = v1 * GuideBookConstants.ATLAS_HEIGHT_SCALE
            // Four vertices of square following order: TopLeft, TopRight, BottomLeft, BottomRight
            bufferBuilder.pos(x0, y1, z).tex(u0S, v1S).color(r, g, b, a).endVertex()
            bufferBuilder.pos(x1, y1, z).tex(u1S, v1S).color(r, g, b, a).endVertex()
            bufferBuilder.pos(x1, y0, z).tex(u1S, v0S).color(r, g, b, a).endVertex()
            bufferBuilder.pos(x0, y0, z).tex(u0S, v0S).color(r, g, b, a).endVertex()
        }

        /**
         * Draws a Tile of size btn, with a specific border.
         * @param btn       defines the size and position of where to draw the tile;
         * @param z         defines the z height of the drawn tile;
         * @param isEnabled defines whether or not the tile is enabled, if it isn't it can't be hovered and the texture is darker;
         * @param isHovered defines whether or not the tile is being hovered, this will make the like have a blue tint;
         * @param color     color to apply to the whole tile.
         */
        fun drawRectangleTile(btn: Rectangle, visibleArea: Rectangle, z: Float, isEnabled: Boolean, isHovered: Boolean, color: Int) {
            // Tile drawing constants
            val btnBackgroundUv = Rectangle(64, 32, 32, 32)
            val btnBorderUv = Rectangle(0, 64, 16, 16)
            val btnBorderWidth = 2
            Minecraft.getMinecraft().renderEngine.bindTexture(GuideBookConstants.guiBookTexture)
            GlStateManager.color(color.redF(), color.greenF(), color.blueF(), 1.0f)
            val tessellator = Tessellator.getInstance()
            val bufferBuilder = tessellator.buffer
            bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
            val uvOffsetModifier = (if (isHovered) 1 else 0) * if (isEnabled) 1 else 2
            val vOffset = uvOffsetModifier * btnBorderUv.roundedHeight
            val uOffset = uvOffsetModifier * btnBackgroundUv.roundedWidth
            //
            val visibleBtn = visibleArea.translated(0, -2).grow(0, 4).overlap(btn)
            // Fill: Middle
            putTexturedRectangle(
                bufferBuilder = bufferBuilder,
                x0 = visibleBtn.x0 + btnBorderWidth,
                y0 = visibleBtn.y0 + btnBorderWidth,
                x1 = visibleBtn.x1 - btnBorderWidth,
                y1 = visibleBtn.y1 - btnBorderWidth,
                z = z,
                u0 = btnBackgroundUv.roundedLeft,
                v0 = btnBackgroundUv.roundedTop + uOffset,
                u1 = btnBackgroundUv.roundedRight,
                v1 = btnBackgroundUv.roundedBottom + uOffset,
                MinecraftColor.WHITE.colorCode
            )

            // Corners: TopLeft, TopRight, BottomLeft & BottomRight
            putTexturedRectangle(
                bufferBuilder = bufferBuilder,
                x0 = visibleBtn.x0,
                y0 = visibleBtn.y0,
                x1 = visibleBtn.x0 + btnBorderWidth,
                y1 = visibleBtn.y0 + btnBorderWidth,
                z = z,
                u0 = btnBorderUv.roundedLeft,
                v0 = btnBorderUv.roundedTop + vOffset,
                u1 = btnBorderUv.roundedLeft + btnBorderWidth,
                v1 = btnBorderUv.roundedTop + btnBorderWidth + vOffset
            )
            putTexturedRectangle(
                bufferBuilder = bufferBuilder,
                x0 = visibleBtn.x1 - btnBorderWidth,
                y0 = visibleBtn.y0,
                x1 = visibleBtn.x1,
                y1 = visibleBtn.y0 + btnBorderWidth,
                z = z,
                u0 = btnBorderUv.roundedRight - btnBorderWidth,
                v0 = btnBorderUv.roundedTop + vOffset,
                u1 = btnBorderUv.roundedRight,
                v1 = btnBorderUv.roundedTop + btnBorderWidth + vOffset
            )
            putTexturedRectangle(
                bufferBuilder = bufferBuilder,
                x0 = visibleBtn.x0,
                y0 = visibleBtn.y1 - btnBorderWidth,
                x1 = visibleBtn.x0 + btnBorderWidth,
                y1 = visibleBtn.y1,
                z = z,
                u0 = btnBorderUv.roundedLeft,
                v0 = btnBorderUv.roundedBottom - btnBorderWidth + vOffset,
                u1 = btnBorderUv.roundedLeft + btnBorderWidth,
                v1 = btnBorderUv.roundedBottom + vOffset
            )
            putTexturedRectangle(
                bufferBuilder = bufferBuilder,
                x0 = visibleBtn.x1 - btnBorderWidth,
                y0 = visibleBtn.y1 - btnBorderWidth,
                x1 = visibleBtn.x1,
                y1 = visibleBtn.y1,
                z = z,
                u0 = btnBorderUv.roundedRight - btnBorderWidth,
                v0 = btnBorderUv.roundedBottom - btnBorderWidth + vOffset,
                u1 = btnBorderUv.roundedRight,
                v1 = btnBorderUv.roundedBottom + vOffset
            )
            // Edges: Top, Bottom, Left & Right
            putTexturedRectangle(
                bufferBuilder = bufferBuilder,
                x0 = visibleBtn.x0 + btnBorderWidth,
                y0 = visibleBtn.y0,
                x1 = visibleBtn.x1 - btnBorderWidth,
                y1 = visibleBtn.y0 + btnBorderWidth,
                z = z,
                u0 = btnBorderUv.roundedLeft + btnBorderWidth,
                v0 = btnBorderUv.roundedTop + vOffset,
                u1 = btnBorderUv.roundedRight - btnBorderWidth,
                v1 = btnBorderUv.roundedTop + btnBorderWidth + vOffset
            )
            putTexturedRectangle(
                bufferBuilder = bufferBuilder,
                x0 = visibleBtn.x0 + btnBorderWidth,
                y0 = visibleBtn.y1 - btnBorderWidth,
                x1 = visibleBtn.x1 - btnBorderWidth,
                y1 = visibleBtn.y1,
                z = z,
                u0 = btnBorderUv.roundedLeft + btnBorderWidth,
                v0 = btnBorderUv.roundedBottom - btnBorderWidth + vOffset,
                u1 = btnBorderUv.roundedRight - btnBorderWidth,
                v1 = btnBorderUv.roundedBottom + vOffset
            )
            putTexturedRectangle(
                bufferBuilder = bufferBuilder,
                x0 = visibleBtn.x0,
                y0 = visibleBtn.y0 + btnBorderWidth,
                x1 = visibleBtn.x0 + btnBorderWidth,
                y1 = visibleBtn.y1 - btnBorderWidth,
                z = z,
                u0 = btnBorderUv.roundedLeft,
                v0 = btnBorderUv.roundedTop + btnBorderWidth + vOffset,
                u1 = btnBorderUv.roundedLeft + btnBorderWidth,
                v1 = btnBorderUv.roundedBottom - btnBorderWidth + vOffset
            )
            putTexturedRectangle(
                bufferBuilder = bufferBuilder,
                x0 = visibleBtn.x1 - btnBorderWidth,
                y0 = visibleBtn.y0 + btnBorderWidth,
                x1 = visibleBtn.x1,
                y1 = visibleBtn.y1 - btnBorderWidth,
                z = z,
                u0 = btnBorderUv.roundedRight - btnBorderWidth,
                v0 = btnBorderUv.roundedTop + btnBorderWidth + vOffset,
                u1 = btnBorderUv.roundedRight,
                v1 = btnBorderUv.roundedBottom - btnBorderWidth + vOffset
            )
            tessellator.draw()
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        }

        /**
         * Draws a stylized tooltip at the specified position, and clamps to the edges (to be tested).
         * @param text  text to be displayed in the tooltip;
         * @param x     x position at the center of the tooltip, in case no clamping needs to be done;
         * @param y     y position of the top of the tooltip;
         * @param z     z position of the tooltip.
         */
        fun drawBoxedString(text: String, x: Int, y: Int, z: Float, horizontalAlign: HorizontalAlignment, verticalAlign: VerticalAlignment) {
            val outlineThickness = 4
            val horizontalPadding = 2
            val verticalPadding = 1
            val width = lpFontRenderer.getStringWidth(text) + 2 * horizontalPadding
            val height = lpFontRenderer.getFontHeight() + 2 * verticalPadding
            val outerArea = Rectangle(
                width = width + 2 * outlineThickness,
                height = height + 2 * outlineThickness
            )
            outerArea.setPos(
                newX = when (horizontalAlign) {
                    HorizontalAlignment.CENTER -> x - outerArea.roundedWidth / 2
                    HorizontalAlignment.LEFT -> x
                    HorizontalAlignment.RIGHT -> x - outerArea.roundedWidth
                },
                newY = when (verticalAlign) {
                    VerticalAlignment.CENTER -> y - outerArea.roundedHeight / 2
                    VerticalAlignment.TOP -> y
                    VerticalAlignment.BOTTOM -> y - outerArea.roundedHeight
                })
            val screen = Rectangle(Minecraft.getMinecraft().currentScreen!!.width, Minecraft.getMinecraft().currentScreen!!.height)
            if (outerArea.x0 < 0) outerArea.translate(translateX = -outerArea.x0)
            if (outerArea.x1 > screen.roundedWidth) outerArea.translate(translateX = screen.roundedWidth - outerArea.x1)
            if (outerArea.y0 < 0) outerArea.translate(translateY = -outerArea.y0)
            if (outerArea.y1 > screen.roundedHeight) outerArea.translate(translateY = screen.roundedHeight - outerArea.y1)
            val innerArea = Rectangle(width, height).translated(outerArea).translated(outlineThickness, outlineThickness)
            val outerAreaTexture = Rectangle(112, 32, 16, 16)
            val innerAreaTexture = Rectangle(116, 36, 8, 8)
            GlStateManager.pushMatrix()
            lpFontRenderer.zLevel += z
            lpFontRenderer.drawString(text, innerArea.x0 + horizontalPadding, innerArea.y0 + verticalPadding, defaultDrawableState.color, defaultDrawableState.format, 1.0f)
            lpFontRenderer.zLevel -= z
            GlStateManager.enableAlpha()
            val bufferBuilder = startBuffer()
            putTexturedRectangle(bufferBuilder, innerArea, innerAreaTexture, z)
            // Corners: TopLeft, TopRight, BottomLeft & BottomRight
            putTexturedRectangle(bufferBuilder, outerArea.x0, outerArea.y0, innerArea.x0, innerArea.y0, z, outerAreaTexture.roundedLeft, outerAreaTexture.roundedTop, innerAreaTexture.roundedLeft, innerAreaTexture.roundedTop)
            putTexturedRectangle(bufferBuilder, innerArea.x1, outerArea.y0, outerArea.x1, innerArea.y0, z, innerAreaTexture.roundedRight, outerAreaTexture.roundedTop, outerAreaTexture.roundedRight, innerAreaTexture.roundedTop)
            putTexturedRectangle(bufferBuilder, outerArea.x0, innerArea.y1, innerArea.x0, outerArea.y1, z, outerAreaTexture.roundedLeft, innerAreaTexture.roundedBottom, innerAreaTexture.roundedLeft, outerAreaTexture.roundedBottom)
            putTexturedRectangle(bufferBuilder, innerArea.x1, innerArea.y1, outerArea.x1, outerArea.y1, z, innerAreaTexture.roundedRight, innerAreaTexture.roundedBottom, outerAreaTexture.roundedRight, outerAreaTexture.roundedBottom)
            // Edges: Top, Bottom, Left & Right
            putTexturedRectangle(bufferBuilder, innerArea.x0, outerArea.y0, innerArea.x1, innerArea.y0, z, innerAreaTexture.roundedLeft, outerAreaTexture.roundedTop, innerAreaTexture.roundedRight, innerAreaTexture.roundedTop)
            putTexturedRectangle(bufferBuilder, innerArea.x0, innerArea.y1, innerArea.x1, outerArea.y1, z, innerAreaTexture.roundedLeft, innerAreaTexture.roundedBottom, innerAreaTexture.roundedRight, outerAreaTexture.roundedBottom)
            putTexturedRectangle(bufferBuilder, outerArea.x0, innerArea.y0, innerArea.x0, innerArea.y1, z, outerAreaTexture.roundedLeft, innerAreaTexture.roundedTop, innerAreaTexture.roundedLeft, innerAreaTexture.roundedBottom)
            putTexturedRectangle(bufferBuilder, innerArea.x1, innerArea.y0, outerArea.x1, innerArea.y1, z, innerAreaTexture.roundedRight, innerAreaTexture.roundedTop, outerAreaTexture.roundedRight, innerAreaTexture.roundedBottom)
            drawBuffer()
            GlStateManager.disableAlpha()
            GlStateManager.popMatrix()
        }

        private fun startBuffer(): BufferBuilder {
            Minecraft.getMinecraft().renderEngine.bindTexture(GuideBookConstants.guiBookTexture)
            // Four vertices of square following order: TopLeft, TopRight, BottomLeft, BottomRight
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
            val tessellator = Tessellator.getInstance()
            val bufferBuilder = tessellator.buffer
            bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
            return bufferBuilder
        }

        private fun drawBuffer() {
            Tessellator.getInstance().draw()
        }

        /**
         * Draws a colored horizontal line.
         * @param x0        starting position of the line
         * @param x1        ending position of the line
         * @param y         y axis of the line.
         * @param thickness thickness of the line which will be added below the y axis.
         * @param color     color of the line formatted as #aarrggbb integer.
         */
        fun drawHorizontalLine(x0: Float, x1: Float, y: Float, z: Float, thickness: Int, color: Int) {
            val r = color.red()
            val g = color.green()
            val b = color.blue()
            val a = color.alpha()
            drawLine { bufferBuilder ->
                bufferBuilder.pos(x0, y + thickness, z).color(r, g, b, a).endVertex()
                bufferBuilder.pos(x1, y + thickness, z).color(r, g, b, a).endVertex()
                bufferBuilder.pos(x1, y, z).color(r, g, b, a).endVertex()
                bufferBuilder.pos(x0, y, z).color(r, g, b, a).endVertex()
            }
        }

        /**
         * Draws a colored vertical line.
         * @param x         line's x axis
         * @param y0        line's starting position
         * @param y1        line's ending position
         * @param thickness line's thickness which will be added to the right of the x axis
         * @param color     color of the line formatted as #aarrggbb integer.
         */
        private fun drawVerticalLine(x: Float, y0: Float, y1: Float, z: Float, thickness: Int, color: Int) {
            val r = color.red()
            val g = color.green()
            val b = color.blue()
            val a = color.alpha()
            drawLine { bufferBuilder ->
                bufferBuilder.pos(x, y1, z).color(r, g, b, a).endVertex()
                bufferBuilder.pos(x + thickness, y1, z).color(r, g, b, a).endVertex()
                bufferBuilder.pos(x + thickness, y0, z).color(r, g, b, a).endVertex()
                bufferBuilder.pos(x, y0, z).color(r, g, b, a).endVertex()
            }
        }

        private fun drawLine(bufferAction: (BufferBuilder) -> Unit) {
            val tessellator = Tessellator.getInstance()
            val bufferBuilder = tessellator.buffer
            GlStateManager.disableTexture2D()
            bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR)

            bufferAction(bufferBuilder)

            tessellator.draw()
            GlStateManager.enableTexture2D()
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GlStateManager.disableBlend()
        }

        /**
         * Draws a small plus sign next to the mouse cursor.
         * Can be used to indicate there is more information to show about the hovered element when shift is pressed.
         * @param mouseX    cursor x position.
         * @param mouseY    cursor y position.
         */
        fun drawLinkIndicator(mouseX: Int, mouseY: Int) {
            drawVerticalLine(mouseX + 5f, mouseY - 5f, mouseY - 2f, GuideBookConstants.Z_TOOLTIP, 1, MinecraftColor.WHITE.colorCode)
            drawHorizontalLine(mouseX + 4f, mouseX + 7f, mouseY - 4f, GuideBookConstants.Z_TOOLTIP, 1, MinecraftColor.WHITE.colorCode)
        }

        fun drawImage(imageBody: Rectangle, visibleArea: Rectangle, image: ResourceLocation) {
            val visibleImageBody = imageBody.overlap(visibleArea)
            val xOffset = min(imageBody.x0 - visibleArea.x0, 0f)
            val yOffset = min(imageBody.y0 - visibleArea.y0, 0f)
            val visibleImageTexture = Rectangle.fromRectangle(visibleImageBody)
                .resetPos()
                .translate(xOffset, -yOffset)
            GlStateManager.pushMatrix()
            Minecraft.getMinecraft().textureManager.bindTexture(image)
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
            val tessellator = Tessellator.getInstance()
            val bufferBuilder = tessellator.buffer
            bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX)
            putTexturedImage(
                bufferBuilder = bufferBuilder,
                x0 = visibleImageBody.x0,
                y0 = visibleImageBody.y0,
                x1 = visibleImageBody.x1,
                y1 = visibleImageBody.y1,
                z = GuideBookConstants.Z_TEXT,
                uw = imageBody.roundedWidth,
                vh = imageBody.roundedHeight,
                u0 = visibleImageTexture.roundedLeft,
                v0 = visibleImageTexture.roundedTop,
                u1 = visibleImageTexture.roundedRight,
                v1 = visibleImageTexture.roundedBottom,
            )
            tessellator.draw()
            GlStateManager.popMatrix()
        }

        fun drawRectangleOutline(rect: Rectangle, z: Float, color: Int) {
            GlStateManager.pushMatrix()
            GlStateManager.disableAlpha()
            GlStateManager.disableBlend()
            drawHorizontalLine(rect.x0 - 1, rect.x1, rect.y0 - 1, z, 1, color) // TOP
            drawHorizontalLine(rect.x0, rect.x1 + 1, rect.y1, z, 1, color) // BOTTOM
            drawVerticalLine(rect.x0 - 1, rect.y0, rect.y1 + 1, z, 1, color) // LEFT
            drawVerticalLine(rect.x1, rect.y0 - 1, rect.y1, z, 1, color) // RIGHT
            GlStateManager.enableAlpha()
            GlStateManager.enableBlend()
            GlStateManager.popMatrix()
        }
    }

}

fun BufferBuilder.pos(x: Float, y: Float, z: Float): BufferBuilder = pos(x.toDouble(), y.toDouble(), z.toDouble())

fun BufferBuilder.tex(u: Float, v: Float): BufferBuilder = tex(u.toDouble(), v.toDouble())
