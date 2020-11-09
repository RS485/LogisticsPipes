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
import logisticspipes.utils.MinecraftColor
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.EnumHand
import net.minecraft.util.ResourceLocation
import network.rs485.logisticspipes.gui.LPFontRenderer
import network.rs485.logisticspipes.gui.guidebook.GuideBookConstants.GUI_BOOK_TEXTURE
import network.rs485.logisticspipes.guidebook.BookContents
import network.rs485.logisticspipes.guidebook.BookContents.DEBUG_FILE
import network.rs485.logisticspipes.guidebook.BookContents.MAIN_MENU_FILE
import network.rs485.logisticspipes.util.*
import network.rs485.logisticspipes.util.math.Rectangle
import network.rs485.markdown.TextFormat
import org.lwjgl.opengl.GL11
import java.util.*

object GuideBookConstants {
    val GUI_BOOK_TEXTURE = ResourceLocation(LPConstants.LP_MOD_ID, "textures/gui/guide_book.png")

    // Texture
    private const val atlasWidth = 256.0
    private const val atlasHeight = 256.0
    const val atlasWidthScale = 1 / atlasWidth
    const val atlasHeightScale = 1 / atlasHeight

    // Tile Constants


    // Z Levels
    const val zTooltip = 20.0 // Tooltip z
    const val zTitleButtons = 15.0 // Title and Buttons Z
    const val zFrame = 10.0 // Frame Z
    const val zText = 5.0 // Text/Information Z
    const val zBackground = 0.0 // Background Z
}

// Gui Frame Constants
private const val guiBorderThickness = 16
private const val guiShadowThickness = 6
private const val guiSeparatorThickness = 5
private const val guiBorderWithShadowThickness = guiBorderThickness + guiShadowThickness
private const val guiAtlasSize = 64
private val innerFrameTexture = Rectangle(guiBorderWithShadowThickness, guiBorderWithShadowThickness, guiAtlasSize - (guiBorderWithShadowThickness * 2), guiAtlasSize - (guiBorderWithShadowThickness * 2))
private val outerFrameTexture = Rectangle(0, 0, guiAtlasSize, guiAtlasSize)
private val sliderSeparatorTexture = Rectangle(96, 33, 16, 30)
private val backgroundFrameTexture = Rectangle(64, 0, 32, 32)


// Slider
private const val guiSliderWidth = 12
private const val guiSliderHeight = 15

// Tabs
private const val guiTabWidth = 24
private const val guiTabHeight = 24
private const val guiFullTabHeight = 32
private const val maxTabs = 10

class GuiGuideBook(val hand: EnumHand) : GuiScreen() {
    private val innerGui = Rectangle()
    private val outerGui = Rectangle()
    private val sliderSeparator = Rectangle()
    private val usableArea = Rectangle()

    private val cachedPages = hashMapOf<String, SavedPage>()
    var currentPage: SavedPage = cachedPages.getOrPut(MAIN_MENU_FILE) { SavedPage(MAIN_MENU_FILE, 0, 0.0f) }

    // Drawing vars
    private var guiSliderX = 0
    private var guiSliderY0 = 0
    private var guiSliderY1 = 0

    // Buttons
    private lateinit var slider: SliderButton
    private lateinit var home: TexturedButton
    private lateinit var addTab: TexturedButton
    private val tabs = mutableListOf<TabButton>()


    init {
        setPage(DEBUG_FILE)
    }

    fun setPage(path: String) {
        currentPage = cachedPages.getOrPut(path) { SavedPage(path, 0, 0.0f) }
        currentPage.initDrawables(usableArea)
        if (this::slider.isInitialized) slider.setProgressF(currentPage.progress)
    }

    // (Re)calculates gui element sizes and positions, this is run on gui init
    private fun calculateGuiConstraints() {
        outerGui.setPos((1.0 / 8.0 * width).toInt(), (1.0 / 8.0 * height).toInt()).setSize((6.0 / 8.0 * width).toInt(), (6.0 / 8.0 * height).toInt())
        innerGui.setPos(outerGui.x0 + guiBorderThickness, outerGui.y0 + guiBorderThickness).setSize(outerGui.width - 2 * guiBorderThickness, outerGui.height - 2 * guiBorderThickness)
        sliderSeparator.setPos(innerGui.x1 - guiSliderWidth - guiSeparatorThickness - guiShadowThickness, innerGui.y0).setSize(2 * guiShadowThickness + guiSeparatorThickness, innerGui.height)
        guiSliderX = innerGui.x1 - guiSliderWidth
        guiSliderY0 = innerGui.y0
        guiSliderY1 = innerGui.y1
        usableArea.setPos(innerGui.x0 + guiShadowThickness, innerGui.y0).setSize(innerGui.width - 2 * guiShadowThickness - guiSliderWidth - guiSeparatorThickness, innerGui.height)
        currentPage.initDrawables(usableArea)
        updateButtonVisibility()
    }

    private fun updateButtonVisibility() {
        if (this::home.isInitialized) home.visible = currentPage.page != MAIN_MENU_FILE
        if (this::slider.isInitialized) slider.enabled = currentPage.height > usableArea.height
        var xOffset = 0
        for (tab: TabButton in tabs) {
            tab.setPos(outerGui.x1 - 2 - 2 * guiTabWidth - xOffset, outerGui.y0)
            xOffset += guiTabWidth
            tab.isActive = tab.tab.isEqual(currentPage)
        }
        if (this::addTab.isInitialized) {
            addTab.visible = currentPage.page != MAIN_MENU_FILE && tabs.size < maxTabs
            addTab.enabled = tabNotFound(currentPage)
            addTab.setX(outerGui.x1 - 20 - guiTabWidth - xOffset)
        }
    }

    private fun tabNotFound(checkTab: SavedPage): Boolean {
        for (tab in tabs) if (tab.tab.isEqual(checkTab)) return false
        return true
    }


    // Overrides

    override fun initGui() {
        calculateGuiConstraints()
        slider = addButton(SliderButton(0, innerGui.x1 - guiSliderWidth, innerGui.y0, innerGui.height, guiSliderWidth, guiSliderHeight, currentPage.progress, ::setPageProgress))
        home = addButton(TexturedButton(1, outerGui.x1 - guiTabWidth, outerGui.y0 - guiTabHeight, guiTabWidth, guiFullTabHeight, GuideBookConstants.zTitleButtons, 16, 64, false, ButtonType.TAB).setOverlayTexture(128, 0, 16, 16))
        addTab = addButton(TexturedButton(2, outerGui.x1 - 18 - guiTabWidth + 4, outerGui.y0 - 18, 16, 16, GuideBookConstants.zTitleButtons, 192, 0, true, ButtonType.NORMAL))
        updateButtonVisibility()
    }

    override fun onGuiClosed() {
        // TODO store book state/data to item NBT
        BookContents.clear()
        super.onGuiClosed()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        buttonList.forEach { it.drawButton(mc, mouseX, mouseY, partialTicks) }
        currentPage.draw(mouseX, mouseY, partialTicks, usableArea)
        drawGui()
        if (tabs.isNotEmpty()) tabs.forEach { it.drawButton(mc, mouseX, mouseY, partialTicks) }
        drawTitle()
    }

    override fun doesGuiPauseGame() = false

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        // TODO if within the usable area pass click and check if links or menu items were clicked
        // TODO Sort buttons by zLevel and filter out inactive buttons for it to work as expected
        // TODO replicate super but without ignoring "non-left-clicks"
        val allButtons = (buttonList + tabs).sortedBy { it.zLevel }.filter { it.visible && it.enabled }
        for (button in allButtons) {
            if (button.mousePressed(mc, mouseX, mouseY)) {
                selectedButton = button
                button.playPressSound(mc.soundHandler)
                when (mouseButton) {
                    0 -> {
                        actionPerformed(button)
                    }
                    1 -> {
                        rightClick(button)
                    }
                }
            }
        }

    }

    override fun actionPerformed(button: GuiButton) {
        when (button) {
            home -> setPage(MAIN_MENU_FILE)
            addTab -> addBookmark(currentPage)
            is TabButton -> if (!button.isActive) setPage(button.tab.page)
            else -> {
            }
        }
        updateButtonVisibility()
    }

    private fun rightClick(button: GuiButton) {
        when (button) {
            is TabButton -> {
                if (button.isActive) {
                    if (isCtrlKeyDown() && isShiftKeyDown()) {
                        removeBookmark(button)
                    } else if (isShiftKeyDown()) {
                        button.cycleColor(inverted = true)
                    } else {
                        button.cycleColor(inverted = false)
                    }
                }
            }
            else -> {
            }
        }
    }

    // Bookmark logic
    private fun addBookmark(currentPage: SavedPage) {
        if (!checkBookmarks(TabButton(outerGui.x1 - 2 - 2 * guiTabWidth, outerGui.y0, currentPage)) && tabs.size < maxTabs) {
            tabs.add(TabButton(outerGui.x1 - 2 - 2 * guiTabWidth, outerGui.y0, currentPage))
        }
    }

    private fun removeBookmark(tabButton: TabButton) {
        if (tabs.size < 0 && checkBookmarks(tabButton) && tabs.contains(tabButton)) {
            tabs.remove(tabButton)
        }
    }

    private fun checkBookmarks(tabButtonIn: TabButton): Boolean {
        tabs.forEach { tabButton -> tabButton.tab.isEqual(tabButtonIn.tab) }
        return false
    }


    // TODO get book state/data from item NBT

    fun setPageProgress(progress: Float) {
        currentPage.progress = progress
    }

    private fun drawTitle() {
        lpFontRenderer.zLevel = GuideBookConstants.zTitleButtons
        lpFontRenderer.drawCenteredString(currentPage.loadedPage.metadata.title, width / 2, outerGui.y0 + 4, MinecraftColor.WHITE.colorCode, EnumSet.of(TextFormat.Shadow), 1.0)
        lpFontRenderer.zLevel = GuideBookConstants.zText
    }

    private fun drawGui() {
        Minecraft.getMinecraft().renderEngine.bindTexture(GUI_BOOK_TEXTURE)
        // Four vertices of square following order: TopLeft, TopRight, BottomLeft, BottomRight
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT)
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT)
        val tessellator = Tessellator.getInstance()
        val bufferBuilder = tessellator.buffer
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX)
        // Background
        putRepeatingTexturedRectangle(bufferBuilder, innerGui.x0, innerGui.y0, innerGui.x1, innerGui.y1, GuideBookConstants.zBackground, backgroundFrameTexture.x0, backgroundFrameTexture.y0, backgroundFrameTexture.x1, backgroundFrameTexture.y1)
        // Corners: TopLeft, TopRight, BottomLeft & BottomRight
        putTexturedRectangle(bufferBuilder, outerGui.x0, outerGui.y0, innerGui.x0 + guiShadowThickness, innerGui.y0 + guiShadowThickness, GuideBookConstants.zFrame, outerFrameTexture.x0, outerFrameTexture.y0, innerFrameTexture.x0, innerFrameTexture.y0)
        putTexturedRectangle(bufferBuilder, innerGui.x1 - guiShadowThickness, outerGui.y0, outerGui.x1, innerGui.y0 + guiShadowThickness, GuideBookConstants.zFrame, innerFrameTexture.x1, outerFrameTexture.y0, outerFrameTexture.x1, innerFrameTexture.y0)
        putTexturedRectangle(bufferBuilder, outerGui.x0, innerGui.y1 - guiShadowThickness, innerGui.x0 + guiShadowThickness, outerGui.y1, GuideBookConstants.zFrame, outerFrameTexture.x0, innerFrameTexture.y1, innerFrameTexture.x0, outerFrameTexture.y1)
        putTexturedRectangle(bufferBuilder, innerGui.x1 - guiShadowThickness, innerGui.y1 - guiShadowThickness, outerGui.x1, outerGui.y1, GuideBookConstants.zFrame, innerFrameTexture.x1, innerFrameTexture.y1, outerFrameTexture.x1, outerFrameTexture.y1)
        // Edges: Top, Bottom, Left & Right
        putTexturedRectangle(bufferBuilder, innerGui.x0 + guiShadowThickness, outerGui.y0, innerGui.x1 - guiShadowThickness, innerGui.y0 + guiShadowThickness, GuideBookConstants.zFrame, innerFrameTexture.x0, outerFrameTexture.y0, innerFrameTexture.x1, innerFrameTexture.y0)
        putTexturedRectangle(bufferBuilder, innerGui.x0 + guiShadowThickness, innerGui.y1 - guiShadowThickness, innerGui.x1 - guiShadowThickness, outerGui.y1, GuideBookConstants.zFrame, innerFrameTexture.x0, innerFrameTexture.y1, innerFrameTexture.x1, outerFrameTexture.y1)
        putTexturedRectangle(bufferBuilder, outerGui.x0, innerGui.y0 + guiShadowThickness, innerGui.x0 + guiShadowThickness, innerGui.y1 - guiShadowThickness, GuideBookConstants.zFrame, outerFrameTexture.x0, innerFrameTexture.y0, innerFrameTexture.x0, innerFrameTexture.y1)
        putTexturedRectangle(bufferBuilder, innerGui.x1 - guiShadowThickness, innerGui.y0 + guiShadowThickness, outerGui.x1, innerGui.y1 - guiShadowThickness, GuideBookConstants.zFrame, innerFrameTexture.x1, innerFrameTexture.y0, outerFrameTexture.x1, innerFrameTexture.y1)
        // Slider Separator
        putTexturedRectangle(bufferBuilder, sliderSeparator.x0, sliderSeparator.y0 - 1, sliderSeparator.x1, sliderSeparator.y0, GuideBookConstants.zFrame, sliderSeparatorTexture.x0, sliderSeparatorTexture.y0 - 1, sliderSeparatorTexture.x1, sliderSeparatorTexture.y0)
        putTexturedRectangle(bufferBuilder, sliderSeparator.x0, sliderSeparator.y0, sliderSeparator.x1, sliderSeparator.y1, GuideBookConstants.zFrame, sliderSeparatorTexture.x0, sliderSeparatorTexture.y0, sliderSeparatorTexture.x1, sliderSeparatorTexture.y1)
        putTexturedRectangle(bufferBuilder, sliderSeparator.x0, sliderSeparator.y1, sliderSeparator.x1, sliderSeparator.y1 + 1, GuideBookConstants.zFrame, sliderSeparatorTexture.x0, sliderSeparatorTexture.y1, sliderSeparatorTexture.x1, sliderSeparatorTexture.y1 + 1)
        tessellator.draw()
        GlStateManager.disableBlend()
    }

    companion object {
        val lpFontRenderer = LPFontRenderer("ter-u12n")

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
        @JvmStatic
        fun drawStretchingRectangle(x0: Int, y0: Int, x1: Int, y1: Int, z: Double, u0: Int, v0: Int, u1: Int, v1: Int, blend: Boolean) {
            drawStretchingRectangle(x0, y0, x1, y1, z, u0, v0, u1, v1, blend, 0xFFFFFF)
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
            Minecraft.getMinecraft().renderEngine.bindTexture(GUI_BOOK_TEXTURE)
            val r = redF(color)
            val g = greenF(color)
            val b = blueF(color)
            // Four vertices of square following order: TopLeft, TopRight, BottomLeft, BottomRight
            if (blend) GlStateManager.enableBlend()
            if (blend) GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GlStateManager.color(r, g, b, 1.0f)
            val tessellator = Tessellator.getInstance()
            val bufferBuilder = tessellator.buffer
            bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX)
            putTexturedRectangle(bufferBuilder, x0, y0, x1, y1, z, u0, v0, u1, v1)
            tessellator.draw()
            if (blend) GlStateManager.disableBlend()
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
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
        fun putRepeatingTexturedRectangle(bufferBuilder: BufferBuilder, x0: Int, y0: Int, x1: Int, y1: Int, z: Double, u0: Int, v0: Int, u1: Int, v1: Int) {
            val x = x1 - x0
            val y = y1 - y0
            val u = u1 - u0
            val v = v1 - v0
            val timesX = x / u
            val timesY = y / v
            val remainderX = x % u
            val remainderY = y % v
            for (i in 0..timesY) {
                for (j in 0..timesX) {
                    if (j == timesX && i == timesY) {
                        putTexturedRectangle(bufferBuilder, x0 + j * u, y0 + i * v, x0 + j * u + remainderX, y0 + i * v + remainderY, z, u0, v0, u0 + remainderX, v0 + remainderY)
                    } else if (j == timesX) {
                        putTexturedRectangle(bufferBuilder, x0 + j * u, y0 + i * v, x0 + j * u + remainderX, y0 + (i + 1) * v, z, u0, v0, u0 + remainderX, v1)
                    } else if (i == timesY) {
                        putTexturedRectangle(bufferBuilder, x0 + j * u, y0 + i * v, x0 + (j + 1) * u, y0 + i * v + remainderY, z, u0, v0, u1, v0 + remainderY)
                    } else {
                        putTexturedRectangle(bufferBuilder, x0 + j * u, y0 + i * v, x0 + (j + 1) * u, y0 + (i + 1) * v, z, u0, v0, u1, v1)
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
        fun putTexturedRectangle(bufferBuilder: BufferBuilder, area: Rectangle, textureArea: Rectangle, z: Double) {
            putTexturedRectangle(bufferBuilder, area.x0, area.y0, area.x1, area.y1, z, textureArea.x0, textureArea.y0, textureArea.x1, textureArea.y1)
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
        fun putTexturedRectangle(bufferBuilder: BufferBuilder, x0: Int, y0: Int, x1: Int, y1: Int, z: Double, u0: Int, v0: Int, u1: Int, v1: Int) {
            // Scaled
            val u0S = u0 * GuideBookConstants.atlasWidthScale
            val v0S = v0 * GuideBookConstants.atlasHeightScale
            val u1S = u1 * GuideBookConstants.atlasWidthScale
            val v1S = v1 * GuideBookConstants.atlasHeightScale
            // Four vertices of square following order: TopLeft, TopRight, BottomLeft, BottomRight
            bufferBuilder.pos(x0.toDouble(), y1.toDouble(), z).tex(u0S, v1S).endVertex()
            bufferBuilder.pos(x1.toDouble(), y1.toDouble(), z).tex(u1S, v1S).endVertex()
            bufferBuilder.pos(x1.toDouble(), y0.toDouble(), z).tex(u1S, v0S).endVertex()
            bufferBuilder.pos(x0.toDouble(), y0.toDouble(), z).tex(u0S, v0S).endVertex()
        }

        /**
         * Draws a Tile of size btn, with a specific border.
         * @param btn       defines the size and position of where to draw the tile;
         * @param z         defines the z height of the drawn tile;
         * @param isEnabled defines whether or not the tile is enabled, if it isn't it can't be hovered and the texture is darker;
         * @param isHovered defines whether or not the tile is being hovered, this will make the like have a blue tint;
         * @param color     color to apply to the whole tile.
         */
        fun drawRectangleTile(btn: Rectangle, z: Double, isEnabled: Boolean, isHovered: Boolean, color: Int) {
            // TODO make it cut the shape depending on broken borders

            // Tile drawing constants
            val btnBackgroundUv = Rectangle(64, 32, 32, 32)
            val btnBorderUv = Rectangle(0, 64, 16, 16)
            val btnBorderWidth = 2
            Minecraft.getMinecraft().renderEngine.bindTexture(GUI_BOOK_TEXTURE)
            GlStateManager.color(redF(color), greenF(color), blueF(color), 1.0f)
            val tessellator = Tessellator.getInstance()
            val bufferBuilder = tessellator.buffer
            bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX)
            val hovered = if (isHovered) 1 else 0
            val enabled = if (isEnabled) 1 else 2
            // Fill: Middle
            putRepeatingTexturedRectangle(bufferBuilder, btn.x0 + btnBorderWidth, btn.y0 + btnBorderWidth, btn.x1 - btnBorderWidth, btn.y1 - btnBorderWidth, z, btnBackgroundUv.x0, btnBackgroundUv.y0 + hovered * enabled * btnBackgroundUv.width, btnBackgroundUv.x1,
                    btnBackgroundUv.y1 + hovered * enabled * btnBackgroundUv.width)
            // Corners: TopLeft, TopRight, BottomLeft & BottomRight
            putTexturedRectangle(bufferBuilder, btn.x0, btn.y0, btn.x0 + btnBorderWidth, btn.y0 + btnBorderWidth, z, btnBorderUv.x0, btnBorderUv.y0 + hovered * enabled * btnBorderUv.height, btnBorderUv.x0 + btnBorderWidth,
                    btnBorderUv.y0 + btnBorderWidth + hovered * enabled * btnBorderUv.height)
            putTexturedRectangle(bufferBuilder, btn.x1 - btnBorderWidth, btn.y0, btn.x1, btn.y0 + btnBorderWidth, z, btnBorderUv.x1 - btnBorderWidth, btnBorderUv.y0 + hovered * enabled * btnBorderUv.height, btnBorderUv.x1,
                    btnBorderUv.y0 + btnBorderWidth + hovered * enabled * btnBorderUv.height)
            putTexturedRectangle(bufferBuilder, btn.x0, btn.y1 - btnBorderWidth, btn.x0 + btnBorderWidth, btn.y1, z, btnBorderUv.x0, btnBorderUv.y1 - btnBorderWidth + hovered * enabled * btnBorderUv.height, btnBorderUv.x0 + btnBorderWidth,
                    btnBorderUv.y1 + hovered * enabled * btnBorderUv.height)
            putTexturedRectangle(bufferBuilder, btn.x1 - btnBorderWidth, btn.y1 - btnBorderWidth, btn.x1, btn.y1, z, btnBorderUv.x1 - btnBorderWidth, btnBorderUv.y1 - btnBorderWidth + hovered * enabled * btnBorderUv.height, btnBorderUv.x1,
                    btnBorderUv.y1 + hovered * enabled * btnBorderUv.height)
            // Edges: Top, Bottom, Left & Right
            putTexturedRectangle(bufferBuilder, btn.x0 + btnBorderWidth, btn.y0, btn.x1 - btnBorderWidth, btn.y0 + btnBorderWidth, z, btnBorderUv.x0 + btnBorderWidth, btnBorderUv.y0 + hovered * enabled * btnBorderUv.height, btnBorderUv.x1 - btnBorderWidth,
                    btnBorderUv.y0 + btnBorderWidth + hovered * enabled * btnBorderUv.height)
            putTexturedRectangle(bufferBuilder, btn.x0 + btnBorderWidth, btn.y1 - btnBorderWidth, btn.x1 - btnBorderWidth, btn.y1, z, btnBorderUv.x0 + btnBorderWidth, btnBorderUv.y1 - btnBorderWidth + hovered * enabled * btnBorderUv.height,
                    btnBorderUv.x1 - btnBorderWidth, btnBorderUv.y1 + hovered * enabled * btnBorderUv.height)
            putTexturedRectangle(bufferBuilder, btn.x0, btn.y0 + btnBorderWidth, btn.x0 + btnBorderWidth, btn.y1 - btnBorderWidth, z, btnBorderUv.x0, btnBorderUv.y0 + btnBorderWidth + hovered * enabled * btnBorderUv.height, btnBorderUv.x0 + btnBorderWidth,
                    btnBorderUv.y1 - btnBorderWidth + hovered * enabled * btnBorderUv.height)
            putTexturedRectangle(bufferBuilder, btn.x1 - btnBorderWidth, btn.y0 + btnBorderWidth, btn.x1, btn.y1 - btnBorderWidth, z, btnBorderUv.x1 - btnBorderWidth, btnBorderUv.y0 + btnBorderWidth + hovered * enabled * btnBorderUv.height, btnBorderUv.x1,
                    btnBorderUv.y1 - btnBorderWidth + hovered * enabled * btnBorderUv.height)
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
        fun drawBoxedCenteredString(text: String, x: Int, y: Int, z: Double) {
            // TODO clamp to the size of the screen
            val width = lpFontRenderer.getStringWidth(text) + 8
            val height = lpFontRenderer.getFontHeight()
            val outerArea = Rectangle(x - width / 2 - 4, y, width + 8, height + 8)
            val screenWidth = Minecraft.getMinecraft().currentScreen!!.width
            when {
                outerArea.x0 < 0 -> outerArea.translate(-outerArea.x0, 0)
                outerArea.x1 > screenWidth -> outerArea.translate(screenWidth - outerArea.x1, 0)
            }
            val innerArea = Rectangle(outerArea.x0 + 4, outerArea.y0 + 4, width, height)
            if (outerArea.x0 < 0) outerArea.translate(-outerArea.x0, 0)
            if (outerArea.x1 > Minecraft.getMinecraft().currentScreen!!.width) outerArea.translate(-outerArea.x0, 0)
            val outerAreaTexture = Rectangle(112, 32, 16, 16)
            val innerAreaTexture = Rectangle(116, 36, 8, 8)
            GlStateManager.pushMatrix()
            lpFontRenderer.zLevel += z
            lpFontRenderer.drawString(text, innerArea.x0 + 4, innerArea.y0 + 1, MinecraftColor.WHITE.colorCode, EnumSet.noneOf(TextFormat::class.java), 1.0)
            lpFontRenderer.zLevel -= z
            GlStateManager.enableAlpha()
            // Background
            Minecraft.getMinecraft().renderEngine.bindTexture(GUI_BOOK_TEXTURE)
            // Four vertices of square following order: TopLeft, TopRight, BottomLeft, BottomRight
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
            val tessellator = Tessellator.getInstance()
            val bufferBuilder = tessellator.buffer
            bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX)
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
            val r = red(color)
            val g = green(color)
            val b = blue(color)
            val a = alpha(color)
            val tessellator = Tessellator.getInstance()
            val bufferBuilder = tessellator.buffer
            GlStateManager.disableTexture2D()
            bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR)
            bufferBuilder.pos(x0.toDouble(), y + thickness.toDouble(), z).color(r, g, b, a).endVertex()
            bufferBuilder.pos(x1.toDouble(), y + thickness.toDouble(), z).color(r, g, b, a).endVertex()
            bufferBuilder.pos(x1.toDouble(), y.toDouble(), z).color(r, g, b, a).endVertex()
            bufferBuilder.pos(x0.toDouble(), y.toDouble(), z).color(r, g, b, a).endVertex()
            tessellator.draw()
            GlStateManager.enableTexture2D()
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GlStateManager.disableBlend()
        }

        /**
         * Draws a colored vertical line.
         * @param x         line's x axis
         * @param y0        line's starting position
         * @param y1        line's ending position
         * @param thickness line's thickness which will be added to the right of the x axis
         * @param color     color of the line formatted as #aarrggbb integer.
         */
        fun drawVerticalLine(x: Int, y0: Int, y1: Int, z: Double, thickness: Int, color: Int) {
            val r = red(color)
            val g = green(color)
            val b = blue(color)
            val a = alpha(color)
            val tessellator = Tessellator.getInstance()
            val bufferBuilder = tessellator.buffer
            GlStateManager.disableTexture2D()
            bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR)
            bufferBuilder.pos(x.toDouble(), y1.toDouble(), z).color(r, g, b, a).endVertex()
            bufferBuilder.pos(x + thickness.toDouble(), y1.toDouble(), z).color(r, g, b, a).endVertex()
            bufferBuilder.pos(x + thickness.toDouble(), y0.toDouble(), z).color(r, g, b, a).endVertex()
            bufferBuilder.pos(x.toDouble(), y0.toDouble(), z).color(r, g, b, a).endVertex()
            tessellator.draw()
            GlStateManager.enableTexture2D()
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GlStateManager.disableBlend()
        }
    }
}