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
import logisticspipes.utils.MinecraftColor
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import network.rs485.logisticspipes.gui.LPFontRenderer
import network.rs485.logisticspipes.guidebook.BookContents
import network.rs485.logisticspipes.guidebook.BookContents.MAIN_MENU_FILE
import network.rs485.logisticspipes.guidebook.ItemGuideBook
import network.rs485.logisticspipes.util.*
import network.rs485.logisticspipes.util.math.Rectangle
import network.rs485.markdown.TextFormat
import network.rs485.markdown.defaultDrawableState
import org.lwjgl.opengl.GL11
import java.util.*
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
    const val Z_TOOLTIP = 20.0 // Tooltip z
    const val Z_TITLE_BUTTONS = 15.0 // Title and Buttons Z
    const val Z_FRAME = 10.0 // Frame Z
    const val Z_TEXT = 5.0 // Text/Information Z
    const val Z_BACKGROUND = 0.0 // Background Z

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
    private val sliderSeparatorTexture = Rectangle(96, 33, 16, 30)
    private val backgroundFrameTexture = Rectangle(64, 0, 32, 32)

    // Slider
    private val guiSliderWidth = 12
    private val guiSliderHeight = 15

    // Tabs
    private val guiTabWidth = 24
    private val guiTabHeight = 24
    private val guiFullTabHeight = 32
    private val maxTabs = 10

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

    // TODO make button have add and remove states.
    private lateinit var addTabButton: BookmarkManagingButton

    // initialize tabs from the stack NBT
    private val tabButtons = state.bookmarks.map(::createGuiTabButton).toMutableList()
    private var freeColor: Int = state.bookmarks.maxOfOrNull { it.color ?: 0 } ?: 0

    // initialize cached pages with initial open page
    private val cachedPages = state.bookmarks.plus(state.currentPage).associateBy { it.page }.toMutableMap()

    private val actionListener = ActionListener()

    inner class ActionListener {
        fun onMenuButtonClick(newPage: String) = changePage(newPage)
    }

    private fun changePage(path: String) {
        val newPage = cachedPages.getOrPut(path) { SavedPage(path) }
        state.currentPage = newPage
        newPage.setDrawablesPosition(visibleArea)
        if (this::slider.isInitialized) slider.setProgressF(newPage.progress)
        updateButtonVisibility()
    }

    // (Re)calculates gui element sizes and positions, this is run on gui init
    private fun calculateGuiConstraints() {
        val marginRatio = 1.0 / 8.0
        val sizeRatio = 6.0 / 8.0
        outerGui.setPos((marginRatio * width).toInt(), (marginRatio * height).toInt()).setSize((sizeRatio * width).toInt(), (sizeRatio * height).toInt())
        innerGui.setPos(outerGui.x0 + guiBorderThickness, outerGui.y0 + guiBorderThickness).setSize(outerGui.width - 2 * guiBorderThickness, outerGui.height - 2 * guiBorderThickness)
        sliderSeparator.setPos(innerGui.x1 - guiSliderWidth - guiSeparatorThickness - guiShadowThickness, innerGui.y0).setSize(2 * guiShadowThickness + guiSeparatorThickness, innerGui.height)
        guiSliderX = innerGui.x1 - guiSliderWidth
        guiSliderY0 = innerGui.y0
        guiSliderY1 = innerGui.y1
        visibleArea.setPos(innerGui.x0 + guiShadowThickness, innerGui.y0).setSize(innerGui.width - sliderSeparator.width - guiSliderWidth, innerGui.height)
        state.currentPage.setDrawablesPosition(visibleArea)
        updateButtonVisibility()
    }

    // Checks each button for visibility and updates tab positions.
    private fun updateButtonVisibility() {
        if (this::home.isInitialized) home.visible = state.currentPage.page != MAIN_MENU_FILE
        if (this::slider.isInitialized) slider.enabled = state.currentPage.drawablePage.height > visibleArea.height
        var xOffset = 0
        for (button: TabButton in tabButtons) {
            button.setPos(outerGui.x1 - 2 - 2 * guiTabWidth - xOffset, outerGui.y0)
            xOffset += guiTabWidth
        }
        if (this::addTabButton.isInitialized) {
            addTabButton.updateState()
            addTabButton.setX(outerGui.x1 - 20 - guiTabWidth - xOffset)
        }
    }

    private fun isTabAbsent(page: SavedPage): Boolean = state.bookmarks.none { it.pageEquals(page) }

    override fun initGui() {
        calculateGuiConstraints()
        slider = addButton(
            SliderButton(
                buttonId = 0,
                x = innerGui.x1 - guiSliderWidth,
                y = innerGui.y0,
                railHeight = innerGui.height,
                buttonWidth = guiSliderWidth,
                buttonHeight = guiSliderHeight,
                progress = state.currentPage.progress,
                setProgressCallback = { progress -> state.currentPage.progress = progress }
            )
        )
        home = addButton(
            HomeButton(
                x = outerGui.x1,
                y = outerGui.y0
            ) { mouseButton ->
                if (mouseButton == 0) {
                    changePage(MAIN_MENU_FILE)
                }
                return@HomeButton true
            }
        )
        addTabButton = addButton(
            BookmarkManagingButton(
                x = outerGui.x1 - 18 - guiTabWidth + 4,
                y = outerGui.y0 - 2,
                onClickAction = { buttonState ->
                    when (buttonState) {
                        BookmarkManagingButton.ButtonState.ADD -> {
                            addBookmark()
                            true
                        }
                        BookmarkManagingButton.ButtonState.REMOVE -> removeBookmark(state.currentPage)
                        BookmarkManagingButton.ButtonState.DISABLED -> false
                    }
                },
                additionStateUpdater = {
                    when {
                        state.currentPage.page == MAIN_MENU_FILE -> BookmarkManagingButton.ButtonState.DISABLED
                        isTabAbsent(state.currentPage) -> BookmarkManagingButton.ButtonState.ADD
                        else -> BookmarkManagingButton.ButtonState.REMOVE
                    }
                }
            )
        )
        updateButtonVisibility()
    }

    override fun onGuiClosed() {
        LPItems.itemGuideBook.saveState(state)
        // TODO remove this for release
        BookContents.clear()
        super.onGuiClosed()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        buttonList.forEach { it.drawButton(mc, mouseX, mouseY, partialTicks) }
        state.currentPage.run {
            updateScrollPosition(visibleArea)
            drawablePage.draw(mouseX, mouseY, partialTicks, visibleArea)
        }
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

    override fun actionPerformed(button: GuiButton) {
        when (button) {
            home -> if (home.click(0)) {
                button.playPressSound(mc.soundHandler)
            }
            addTabButton -> if (addTabButton.click(0)) {
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

    private fun createGuiTabButton(tabPage: SavedPage): TabButton =
        TabButton(outerGui.x1 - 2 - 2 * guiTabWidth, outerGui.y0, object : TabButtonReturn {
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

            override fun getHoverText(): String = tabPage.title
        })

    private fun removeBookmark(page: SavedPage): Boolean {
        val bookmarkedPage: SavedPage? = state.bookmarks.find { it.pageEquals(page) }
        val idx = state.bookmarks.indexOf(bookmarkedPage)
        return if (idx != -1) {
            tabButtons.removeAt(idx)
            state.bookmarks.removeAt(idx)
            true
        } else {
            false
        }
    }

    private fun drawTitle() {
        lpFontRenderer.zLevel = GuideBookConstants.Z_TITLE_BUTTONS
        lpFontRenderer.drawCenteredString(state.currentPage.title, width / 2, outerGui.y0 + 4, MinecraftColor.WHITE.colorCode, EnumSet.of(TextFormat.Shadow), 1.0)
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
        putRepeatingTexturedRectangle(
            bufferBuilder = bufferBuilder,
            x0 = innerGui.x0,
            y0 = innerGui.y0,
            x1 = innerGui.x1,
            y1 = innerGui.y1,
            z = GuideBookConstants.Z_BACKGROUND,
            u0 = backgroundFrameTexture.x0,
            v0 = backgroundFrameTexture.y0,
            u1 = backgroundFrameTexture.x1,
            v1 = backgroundFrameTexture.y1,
            uStartOffset = 0,
            vStartOffset = 0
        )
        // Corners: TopLeft, TopRight, BottomLeft & BottomRight
        putTexturedRectangle(
            bufferBuilder = bufferBuilder,
            x0 = outerGui.x0,
            y0 = outerGui.y0,
            x1 = innerGui.x0 + guiShadowThickness,
            y1 = innerGui.y0 + guiShadowThickness,
            z = GuideBookConstants.Z_FRAME,
            u0 = outerFrameTexture.x0,
            v0 = outerFrameTexture.y0,
            u1 = innerFrameTexture.x0,
            v1 = innerFrameTexture.y0
        )
        putTexturedRectangle(
            bufferBuilder = bufferBuilder,
            x0 = innerGui.x1 - guiShadowThickness,
            y0 = outerGui.y0,
            x1 = outerGui.x1,
            y1 = innerGui.y0 + guiShadowThickness,
            z = GuideBookConstants.Z_FRAME,
            u0 = innerFrameTexture.x1,
            v0 = outerFrameTexture.y0,
            u1 = outerFrameTexture.x1,
            v1 = innerFrameTexture.y0
        )
        putTexturedRectangle(
            bufferBuilder = bufferBuilder,
            x0 = outerGui.x0,
            y0 = innerGui.y1 - guiShadowThickness,
            x1 = innerGui.x0 + guiShadowThickness,
            y1 = outerGui.y1,
            z = GuideBookConstants.Z_FRAME,
            u0 = outerFrameTexture.x0,
            v0 = innerFrameTexture.y1,
            u1 = innerFrameTexture.x0,
            v1 = outerFrameTexture.y1
        )
        putTexturedRectangle(
            bufferBuilder = bufferBuilder,
            x0 = innerGui.x1 - guiShadowThickness,
            y0 = innerGui.y1 - guiShadowThickness,
            x1 = outerGui.x1,
            y1 = outerGui.y1,
            z = GuideBookConstants.Z_FRAME,
            u0 = innerFrameTexture.x1,
            v0 = innerFrameTexture.y1,
            u1 = outerFrameTexture.x1,
            v1 = outerFrameTexture.y1
        )
        // Edges: Top, Bottom, Left & Right
        putTexturedRectangle(
            bufferBuilder = bufferBuilder,
            x0 = innerGui.x0 + guiShadowThickness,
            y0 = outerGui.y0,
            x1 = innerGui.x1 - guiShadowThickness,
            y1 = innerGui.y0 + guiShadowThickness,
            z = GuideBookConstants.Z_FRAME,
            u0 = innerFrameTexture.x0,
            v0 = outerFrameTexture.y0,
            u1 = innerFrameTexture.x1,
            v1 = innerFrameTexture.y0
        )
        putTexturedRectangle(
            bufferBuilder = bufferBuilder,
            x0 = innerGui.x0 + guiShadowThickness,
            y0 = innerGui.y1 - guiShadowThickness,
            x1 = innerGui.x1 - guiShadowThickness,
            y1 = outerGui.y1,
            z = GuideBookConstants.Z_FRAME,
            u0 = innerFrameTexture.x0,
            v0 = innerFrameTexture.y1,
            u1 = innerFrameTexture.x1,
            v1 = outerFrameTexture.y1
        )
        putTexturedRectangle(
            bufferBuilder = bufferBuilder,
            x0 = outerGui.x0,
            y0 = innerGui.y0 + guiShadowThickness,
            x1 = innerGui.x0 + guiShadowThickness,
            y1 = innerGui.y1 - guiShadowThickness,
            z = GuideBookConstants.Z_FRAME,
            u0 = outerFrameTexture.x0,
            v0 = innerFrameTexture.y0,
            u1 = innerFrameTexture.x0,
            v1 = innerFrameTexture.y1
        )
        putTexturedRectangle(
            bufferBuilder = bufferBuilder,
            x0 = innerGui.x1 - guiShadowThickness,
            y0 = innerGui.y0 + guiShadowThickness,
            x1 = outerGui.x1,
            y1 = innerGui.y1 - guiShadowThickness,
            z = GuideBookConstants.Z_FRAME,
            u0 = innerFrameTexture.x1,
            v0 = innerFrameTexture.y0,
            u1 = outerFrameTexture.x1,
            v1 = innerFrameTexture.y1
        )
        // Slider Separator
        putTexturedRectangle(
            bufferBuilder = bufferBuilder,
            x0 = sliderSeparator.x0,
            y0 = sliderSeparator.y0 - 1,
            x1 = sliderSeparator.x1,
            y1 = sliderSeparator.y0,
            z = GuideBookConstants.Z_FRAME,
            u0 = sliderSeparatorTexture.x0,
            v0 = sliderSeparatorTexture.y0 - 1,
            u1 = sliderSeparatorTexture.x1,
            v1 = sliderSeparatorTexture.y0
        )
        putTexturedRectangle(
            bufferBuilder = bufferBuilder,
            x0 = sliderSeparator.x0,
            y0 = sliderSeparator.y0,
            x1 = sliderSeparator.x1,
            y1 = sliderSeparator.y1,
            z = GuideBookConstants.Z_FRAME,
            u0 = sliderSeparatorTexture.x0,
            v0 = sliderSeparatorTexture.y0,
            u1 = sliderSeparatorTexture.x1,
            v1 = sliderSeparatorTexture.y1
        )
        putTexturedRectangle(
            bufferBuilder = bufferBuilder,
            x0 = sliderSeparator.x0,
            y0 = sliderSeparator.y1,
            x1 = sliderSeparator.x1,
            y1 = sliderSeparator.y1 + 1,
            z = GuideBookConstants.Z_FRAME,
            u0 = sliderSeparatorTexture.x0,
            v0 = sliderSeparatorTexture.y1,
            u1 = sliderSeparatorTexture.x1,
            v1 = sliderSeparatorTexture.y1 + 1
        )
        tessellator.draw()
        GlStateManager.disableBlend()
    }

    companion object {
        val lpFontRenderer = LPFontRenderer.get("ter-u12n")

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
        fun drawStretchingRectangle(x0: Int, y0: Int, x1: Int, y1: Int, z: Double, u0: Int, v0: Int, u1: Int, v1: Int, blend: Boolean) {
            drawStretchingRectangle(x0, y0, x1, y1, z, u0, v0, u1, v1, blend, MinecraftColor.WHITE.colorCode)
        }

        fun drawStretchingRectangle(rectangle: Rectangle, z: Double, texture: Rectangle, blend: Boolean, color: Int) {
            drawStretchingRectangle(rectangle.x0, rectangle.y0, rectangle.x1, rectangle.y1, z, texture.x0, texture.y0, texture.x1, texture.y1, blend, color)
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
        fun drawStretchingRectangle(x0: Int, y0: Int, x1: Int, y1: Int, z: Double, u0: Int, v0: Int, u1: Int, v1: Int, blend: Boolean, color: Int) {
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

        private fun putTexturedImage(bufferBuilder: BufferBuilder, x0: Int, y0: Int, x1: Int, y1: Int, z: Double, uw: Int, vh: Int, u0: Int, v0: Int, u1: Int, v1: Int) {
            val atlasWidthScale = 1 / uw.toDouble()
            val atlasHeightScale = 1 / vh.toDouble()
            val u0S = u0 * atlasWidthScale
            val v0S = v0 * atlasHeightScale
            val u1S = u1 * atlasWidthScale
            val v1S = v1 * atlasHeightScale
            bufferBuilder.pos(x0.toDouble(), y1.toDouble(), z).tex(u0S, v1S).endVertex()
            bufferBuilder.pos(x1.toDouble(), y1.toDouble(), z).tex(u1S, v1S).endVertex()
            bufferBuilder.pos(x1.toDouble(), y0.toDouble(), z).tex(u1S, v0S).endVertex()
            bufferBuilder.pos(x0.toDouble(), y0.toDouble(), z).tex(u0S, v0S).endVertex()
        }

        /**
         * Adds multiple repeating textured rectangles to fill the specified area without stretching the given texture to the buffer. This method assumes the bound texture is 256x256 in size.
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
        private fun putRepeatingTexturedRectangle(bufferBuilder: BufferBuilder, x0: Int, y0: Int, x1: Int, y1: Int, z: Double, u0: Int, v0: Int, u1: Int, v1: Int, color: Int = MinecraftColor.WHITE.colorCode, uStartOffset: Int, vStartOffset: Int) {
            val texture = Rectangle(width = u1 - u0, height = v1 - v0)
            val outerArea = Rectangle(x = x0, y = y0, width = x1 - x0, height = y1 - y0)
            if (texture.width < 1 || texture.height < 1 || outerArea.width < 1 || outerArea.height < 1) return
            val leftRemainder = min(outerArea.width, if (uStartOffset == 0) 0 else uStartOffset % texture.width)
            val topRemainder = min(outerArea.height, if (vStartOffset == 0) 0 else vStartOffset % texture.height)
            val rightRemainder = max(0, (outerArea.width - leftRemainder) % texture.width)
            val bottomRemainder = max(0, (outerArea.height - topRemainder) % texture.height)
            val innerArea = Rectangle(x0 + leftRemainder, y0 + topRemainder, outerArea.width - leftRemainder - rightRemainder, outerArea.height - topRemainder - bottomRemainder)

            if (leftRemainder > 0) {
                putRepeatingTexturedRectangle(
                    bufferBuilder = bufferBuilder,
                    x0 = outerArea.x0,
                    y0 = innerArea.y0,
                    x1 = innerArea.x0,
                    y1 = innerArea.y1,
                    z = z,
                    u0 = u1 - leftRemainder,
                    v0 = v0,
                    u1 = u1,
                    v1 = v1,
                    uStartOffset = 0,
                    vStartOffset = 0
                )
            }
            if (topRemainder > 0) {
                putRepeatingTexturedRectangle(
                    bufferBuilder = bufferBuilder,
                    x0 = outerArea.x0,
                    y0 = outerArea.y0,
                    x1 = outerArea.x1,
                    y1 = innerArea.y0,
                    z = z,
                    u0 = u0,
                    v0 = v1 - topRemainder,
                    u1 = u1,
                    v1 = v1,
                    uStartOffset = leftRemainder,
                    vStartOffset = 0
                )
            }
            if (rightRemainder > 0) {
                putRepeatingTexturedRectangle(
                    bufferBuilder = bufferBuilder,
                    x0 = innerArea.x1,
                    y0 = innerArea.y0,
                    x1 = outerArea.x1,
                    y1 = innerArea.y1,
                    z = z,
                    u0 = u0,
                    v0 = v0,
                    u1 = u0 + rightRemainder,
                    v1 = v1,
                    uStartOffset = 0,
                    vStartOffset = 0
                )
            }
            if (bottomRemainder > 0) {
                putRepeatingTexturedRectangle(
                    bufferBuilder = bufferBuilder,
                    x0 = outerArea.x0,
                    y0 = innerArea.y1,
                    x1 = outerArea.x1,
                    y1 = outerArea.y1,
                    z = z,
                    u0 = u0,
                    v0 = v0,
                    u1 = u1,
                    v1 = v0 + bottomRemainder,
                    uStartOffset = leftRemainder,
                    vStartOffset = 0
                )
            }

            if (innerArea.width > 0 && innerArea.height > 0) {
                val timesX = innerArea.width / texture.width
                val timesY = innerArea.height / texture.height
                for (i in 0 until timesY) {
                    for (j in 0 until timesX) {
                        val xOffset = j * texture.width
                        val yOffset = i * texture.height
                        // Center area, full texture dimensions.
                        putTexturedRectangle(
                            bufferBuilder = bufferBuilder,
                            x0 = x0 + leftRemainder + xOffset,
                            y0 = y0 + topRemainder + yOffset,
                            x1 = x0 + leftRemainder + xOffset + texture.width,
                            y1 = y0 + topRemainder + yOffset + texture.height,
                            z = z,
                            u0 = u0,
                            v0 = v0,
                            u1 = u1,
                            v1 = v1,
                            color = color
                        )
                    }
                }
            }
        }

        /**
         * Adds a textured rectangle to the given buffer. This method assumes the bound texture is 256x256 in size.
         * @param bufferBuilder buffer that needs to be initialized before it is given to this method;
         * @param area          defines position and size of the desired rectangle;
         * @param textureArea   defines position and size of the desired rectangle's texture;
         * @param z             defines z level of the desired rectangle.
         */
        private fun putTexturedRectangle(bufferBuilder: BufferBuilder, area: Rectangle, textureArea: Rectangle, z: Double) {
            putTexturedRectangle(bufferBuilder, area.x0, area.y0, area.x1, area.y1, z, textureArea.x0, textureArea.y0, textureArea.x1, textureArea.y1)
        }

        private fun putTexturedRectangle(bufferBuilder: BufferBuilder, x0: Int, y0: Int, x1: Int, y1: Int, z: Double, u0: Int, v0: Int, u1: Int, v1: Int) {
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
        private fun putTexturedRectangle(bufferBuilder: BufferBuilder, x0: Int, y0: Int, x1: Int, y1: Int, z: Double, u0: Int, v0: Int, u1: Int, v1: Int, color: Int) {
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
            bufferBuilder.pos(x0.toDouble(), y1.toDouble(), z).tex(u0S, v1S).color(r, g, b, a).endVertex()
            bufferBuilder.pos(x1.toDouble(), y1.toDouble(), z).tex(u1S, v1S).color(r, g, b, a).endVertex()
            bufferBuilder.pos(x1.toDouble(), y0.toDouble(), z).tex(u1S, v0S).color(r, g, b, a).endVertex()
            bufferBuilder.pos(x0.toDouble(), y0.toDouble(), z).tex(u0S, v0S).color(r, g, b, a).endVertex()
        }

        /**
         * Draws a Tile of size btn, with a specific border.
         * @param btn       defines the size and position of where to draw the tile;
         * @param z         defines the z height of the drawn tile;
         * @param isEnabled defines whether or not the tile is enabled, if it isn't it can't be hovered and the texture is darker;
         * @param isHovered defines whether or not the tile is being hovered, this will make the like have a blue tint;
         * @param color     color to apply to the whole tile.
         */
        fun drawRectangleTile(btn: Rectangle, visibleArea: Rectangle, z: Double, isEnabled: Boolean, isHovered: Boolean, color: Int) {
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
            val vOffset = uvOffsetModifier * btnBorderUv.height
            val uOffset = uvOffsetModifier * btnBackgroundUv.width
            //
            val visibleBtn = visibleArea.translated(0, -2).grow(0, 4).overlap(btn)
            val uStartOffset = if (visibleArea.x0 > btn.x0) btnBackgroundUv.width - (btn.width - visibleBtn.width) else 0
            val vStartOffset = if (visibleArea.y0 > btn.y0) btnBackgroundUv.height - (btn.height - visibleBtn.height) else 0
            // Fill: Middle
            putRepeatingTexturedRectangle(
                bufferBuilder = bufferBuilder,
                x0 = visibleBtn.x0 + btnBorderWidth,
                y0 = visibleBtn.y0 + btnBorderWidth,
                x1 = visibleBtn.x1 - btnBorderWidth,
                y1 = visibleBtn.y1 - btnBorderWidth,
                z = z,
                u0 = btnBackgroundUv.x0,
                v0 = btnBackgroundUv.y0 + uOffset,
                u1 = btnBackgroundUv.x1,
                v1 = btnBackgroundUv.y1 + uOffset,
                MinecraftColor.WHITE.colorCode,
                uStartOffset = uStartOffset,
                vStartOffset = vStartOffset
            )
            // Corners: TopLeft, TopRight, BottomLeft & BottomRight
            putTexturedRectangle(
                bufferBuilder = bufferBuilder,
                x0 = visibleBtn.x0,
                y0 = visibleBtn.y0,
                x1 = visibleBtn.x0 + btnBorderWidth,
                y1 = visibleBtn.y0 + btnBorderWidth,
                z = z,
                u0 = btnBorderUv.x0,
                v0 = btnBorderUv.y0 + vOffset,
                u1 = btnBorderUv.x0 + btnBorderWidth,
                v1 = btnBorderUv.y0 + btnBorderWidth + vOffset
            )
            putTexturedRectangle(
                bufferBuilder = bufferBuilder,
                x0 = visibleBtn.x1 - btnBorderWidth,
                y0 = visibleBtn.y0,
                x1 = visibleBtn.x1,
                y1 = visibleBtn.y0 + btnBorderWidth,
                z = z,
                u0 = btnBorderUv.x1 - btnBorderWidth,
                v0 = btnBorderUv.y0 + vOffset,
                u1 = btnBorderUv.x1,
                v1 = btnBorderUv.y0 + btnBorderWidth + vOffset
            )
            putTexturedRectangle(
                bufferBuilder = bufferBuilder,
                x0 = visibleBtn.x0,
                y0 = visibleBtn.y1 - btnBorderWidth,
                x1 = visibleBtn.x0 + btnBorderWidth,
                y1 = visibleBtn.y1,
                z = z,
                u0 = btnBorderUv.x0,
                v0 = btnBorderUv.y1 - btnBorderWidth + vOffset,
                u1 = btnBorderUv.x0 + btnBorderWidth,
                v1 = btnBorderUv.y1 + vOffset
            )
            putTexturedRectangle(
                bufferBuilder = bufferBuilder,
                x0 = visibleBtn.x1 - btnBorderWidth,
                y0 = visibleBtn.y1 - btnBorderWidth,
                x1 = visibleBtn.x1,
                y1 = visibleBtn.y1,
                z = z,
                u0 = btnBorderUv.x1 - btnBorderWidth,
                v0 = btnBorderUv.y1 - btnBorderWidth + vOffset,
                u1 = btnBorderUv.x1,
                v1 = btnBorderUv.y1 + vOffset
            )
            // Edges: Top, Bottom, Left & Right
            putTexturedRectangle(
                bufferBuilder = bufferBuilder,
                x0 = visibleBtn.x0 + btnBorderWidth,
                y0 = visibleBtn.y0,
                x1 = visibleBtn.x1 - btnBorderWidth,
                y1 = visibleBtn.y0 + btnBorderWidth,
                z = z,
                u0 = btnBorderUv.x0 + btnBorderWidth,
                v0 = btnBorderUv.y0 + vOffset,
                u1 = btnBorderUv.x1 - btnBorderWidth,
                v1 = btnBorderUv.y0 + btnBorderWidth + vOffset
            )
            putTexturedRectangle(
                bufferBuilder = bufferBuilder,
                x0 = visibleBtn.x0 + btnBorderWidth,
                y0 = visibleBtn.y1 - btnBorderWidth,
                x1 = visibleBtn.x1 - btnBorderWidth,
                y1 = visibleBtn.y1,
                z = z,
                u0 = btnBorderUv.x0 + btnBorderWidth,
                v0 = btnBorderUv.y1 - btnBorderWidth + vOffset,
                u1 = btnBorderUv.x1 - btnBorderWidth,
                v1 = btnBorderUv.y1 + vOffset
            )
            putTexturedRectangle(
                bufferBuilder = bufferBuilder,
                x0 = visibleBtn.x0,
                y0 = visibleBtn.y0 + btnBorderWidth,
                x1 = visibleBtn.x0 + btnBorderWidth,
                y1 = visibleBtn.y1 - btnBorderWidth,
                z = z,
                u0 = btnBorderUv.x0,
                v0 = btnBorderUv.y0 + btnBorderWidth + vOffset,
                u1 = btnBorderUv.x0 + btnBorderWidth,
                v1 = btnBorderUv.y1 - btnBorderWidth + vOffset
            )
            putTexturedRectangle(
                bufferBuilder = bufferBuilder,
                x0 = visibleBtn.x1 - btnBorderWidth,
                y0 = visibleBtn.y0 + btnBorderWidth,
                x1 = visibleBtn.x1,
                y1 = visibleBtn.y1 - btnBorderWidth,
                z = z,
                u0 = btnBorderUv.x1 - btnBorderWidth,
                v0 = btnBorderUv.y0 + btnBorderWidth + vOffset,
                u1 = btnBorderUv.x1,
                v1 = btnBorderUv.y1 - btnBorderWidth + vOffset
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
        fun drawBoxedString(text: String, x: Int, y: Int, z: Double, horizontalAlign: HorizontalAlignment, verticalAlign: VerticalAlignment) {
            val width = lpFontRenderer.getStringWidth(text) + 8
            val height = lpFontRenderer.getFontHeight()
            val outerArea = Rectangle(
                x = when (horizontalAlign) {
                    HorizontalAlignment.CENTER -> x - width / 2 - 4
                    HorizontalAlignment.LEFT -> x
                    HorizontalAlignment.RIGHT -> x - width - 8
                },
                y = when (verticalAlign) {
                    VerticalAlignment.CENTER -> y - height / 2 - 4
                    VerticalAlignment.TOP -> y
                    VerticalAlignment.BOTTOM -> y - height - 8
                },
                width = width + 8,
                height = height + 8
            )
            val screen = Rectangle(Minecraft.getMinecraft().currentScreen!!.width, Minecraft.getMinecraft().currentScreen!!.height)
            when {
                outerArea.x0 < 0 -> outerArea.translate(-outerArea.x0, 0)
                outerArea.x1 > screen.width -> outerArea.translate(screen.width - outerArea.x1, 0)
                outerArea.y0 < 0 -> outerArea.translate(0, -outerArea.y0)
                outerArea.y1 > screen.height -> outerArea.translate(0, screen.height - outerArea.y1)
            }
            val innerArea = Rectangle(outerArea.x0 + 4, outerArea.y0 + 4, width, height)
            val outerAreaTexture = Rectangle(112, 32, 16, 16)
            val innerAreaTexture = Rectangle(116, 36, 8, 8)
            GlStateManager.pushMatrix()
            lpFontRenderer.zLevel += z
            lpFontRenderer.drawString(text, innerArea.x0 + 4, innerArea.y0 + 1, defaultDrawableState.color, defaultDrawableState.format, 1.0)
            lpFontRenderer.zLevel -= z
            GlStateManager.enableAlpha()
            // Background
            Minecraft.getMinecraft().renderEngine.bindTexture(GuideBookConstants.guiBookTexture)
            // Four vertices of square following order: TopLeft, TopRight, BottomLeft, BottomRight
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
            val tessellator = Tessellator.getInstance()
            val bufferBuilder = tessellator.buffer
            bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
            putTexturedRectangle(bufferBuilder, innerArea, innerAreaTexture, z)
            // Corners: TopLeft, TopRight, BottomLeft & BottomRight
            putTexturedRectangle(bufferBuilder, outerArea.x0, outerArea.y0, innerArea.x0, innerArea.y0, z, outerAreaTexture.x0, outerAreaTexture.y0, innerAreaTexture.x0, innerAreaTexture.y0)
            putTexturedRectangle(bufferBuilder, innerArea.x1, outerArea.y0, outerArea.x1, innerArea.y0, z, innerAreaTexture.x1, outerAreaTexture.y0, outerAreaTexture.x1, innerAreaTexture.y0)
            putTexturedRectangle(bufferBuilder, outerArea.x0, innerArea.y1, innerArea.x0, outerArea.y1, z, outerAreaTexture.x0, innerAreaTexture.y1, innerAreaTexture.x0, outerAreaTexture.y1)
            putTexturedRectangle(bufferBuilder, innerArea.x1, innerArea.y1, outerArea.x1, outerArea.y1, z, innerAreaTexture.x1, innerAreaTexture.y1, outerAreaTexture.x1, outerAreaTexture.y1)
            // Edges: Top, Bottom, Left & Right
            putTexturedRectangle(bufferBuilder, innerArea.x0, outerArea.y0, innerArea.x1, innerArea.y0, z, innerAreaTexture.x0, outerAreaTexture.y0, innerAreaTexture.x1, innerAreaTexture.y0)
            putTexturedRectangle(bufferBuilder, innerArea.x0, innerArea.y1, innerArea.x1, outerArea.y1, z, innerAreaTexture.x0, innerAreaTexture.y1, innerAreaTexture.x1, outerAreaTexture.y1)
            putTexturedRectangle(bufferBuilder, outerArea.x0, innerArea.y0, innerArea.x0, innerArea.y1, z, outerAreaTexture.x0, innerAreaTexture.y0, innerAreaTexture.x0, innerAreaTexture.y1)
            putTexturedRectangle(bufferBuilder, innerArea.x1, innerArea.y0, outerArea.x1, innerArea.y1, z, innerAreaTexture.x1, innerAreaTexture.y0, outerAreaTexture.x1, innerAreaTexture.y1)
            tessellator.draw()
            GlStateManager.disableAlpha()
            GlStateManager.popMatrix()
        }

        /**
         * Draws a colored horizontal line.
         * @param x0        starting position of the line
         * @param x1        ending position of the line
         * @param y         y axis of the line.
         * @param thickness thickness of the line which will be added below the y axis.
         * @param color     color of the line formatted as #aarrggbb integer.
         */
        fun drawHorizontalLine(x0: Int, x1: Int, y: Int, z: Double, thickness: Int, color: Int) {
            val r = color.red()
            val g = color.green()
            val b = color.blue()
            val a = color.alpha()
            drawLine { bufferBuilder ->
                bufferBuilder.pos(x0.toDouble(), y + thickness.toDouble(), z).color(r, g, b, a).endVertex()
                bufferBuilder.pos(x1.toDouble(), y + thickness.toDouble(), z).color(r, g, b, a).endVertex()
                bufferBuilder.pos(x1.toDouble(), y.toDouble(), z).color(r, g, b, a).endVertex()
                bufferBuilder.pos(x0.toDouble(), y.toDouble(), z).color(r, g, b, a).endVertex()
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
        private fun drawVerticalLine(x: Int, y0: Int, y1: Int, z: Double, thickness: Int, color: Int) {
            val r = color.red()
            val g = color.green()
            val b = color.blue()
            val a = color.alpha()
            drawLine { bufferBuilder ->
                bufferBuilder.pos(x.toDouble(), y1.toDouble(), z).color(r, g, b, a).endVertex()
                bufferBuilder.pos(x + thickness.toDouble(), y1.toDouble(), z).color(r, g, b, a).endVertex()
                bufferBuilder.pos(x + thickness.toDouble(), y0.toDouble(), z).color(r, g, b, a).endVertex()
                bufferBuilder.pos(x.toDouble(), y0.toDouble(), z).color(r, g, b, a).endVertex()
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
            drawVerticalLine(mouseX + 3, mouseY - 5, mouseY - 2, GuideBookConstants.Z_TOOLTIP, 1, MinecraftColor.WHITE.colorCode)
            drawHorizontalLine(mouseX + 2, mouseX + 5, mouseY - 4, GuideBookConstants.Z_TOOLTIP, 1, MinecraftColor.WHITE.colorCode)
        }

        fun drawImage(imageBody: Rectangle, visibleArea: Rectangle, image: ResourceLocation) {
            val visibleImageBody = imageBody.overlap(visibleArea)
            val xOffset = min(imageBody.x0 - visibleArea.x0, 0)
            val yOffset = min(imageBody.y0 - visibleArea.y0, 0)
            val visibleImageTexture = Rectangle.fromRectangle(visibleImageBody)
                .setPos(0, 0)
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
                uw = imageBody.width,
                vh = imageBody.height,
                u0 = visibleImageTexture.x0,
                v0 = visibleImageTexture.y0,
                u1 = visibleImageTexture.x1,
                v1 = visibleImageTexture.y1,
            )
            tessellator.draw()
            GlStateManager.popMatrix()
        }

        fun drawRectangleOutline(rect: Rectangle, z: Int, color: Int) {
            GlStateManager.pushMatrix()
            GlStateManager.disableAlpha()
            GlStateManager.disableBlend()
            drawHorizontalLine(rect.x0 - 1, rect.x1, rect.y0 - 1, z.toDouble(), 1, color) // TOP
            drawHorizontalLine(rect.x0, rect.x1 + 1, rect.y1, z.toDouble(), 1, color) // BOTTOM
            drawVerticalLine(rect.x0 - 1, rect.y0, rect.y1 + 1, z.toDouble(), 1, color) // LEFT
            drawVerticalLine(rect.x1, rect.y0 - 1, rect.y1, z.toDouble(), 1, color) // RIGHT
            GlStateManager.enableAlpha()
            GlStateManager.enableBlend()
            GlStateManager.popMatrix()
        }
    }

    enum class HorizontalAlignment {
        CENTER,
        LEFT,
        RIGHT;
    }

    enum class VerticalAlignment {
        CENTER,
        TOP,
        BOTTOM;
    }
}