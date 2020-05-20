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

package network.rs485.logisticspipes.gui.guidebook;

import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import lombok.Getter;
import org.lwjgl.opengl.GL11;

import logisticspipes.LPConstants;
import logisticspipes.items.ItemGuideBook;
import network.rs485.logisticspipes.gui.guidebook.book.DrawablePage;
import network.rs485.logisticspipes.gui.guidebook.book.MenuItem;

public class GuiGuideBook extends GuiScreen {

	/*
	 * Z Levels:
	 * Tooltip          : 20
	 * Title and Buttons: 15
	 * Background frame : 10
	 * Text             : 5
	 * Background       : 0
	 * Background tint  : -5
	 */

	private boolean loadedNBT = false;

	@Getter
	private final int zTooltip = 20;      // Tooltip z
	@Getter
	private final int zTitleButtons = 15; // Title and Buttons Z
	@Getter
	private final int zFrame = 10;        // Frame Z
	@Getter
	private final int zText = 5;          // Text/Information Z
	@Getter
	private final int zBackground = 0;    // Background Z

	public static final ResourceLocation GUI_BOOK_TEXTURE = new ResourceLocation(LPConstants.LP_MOD_ID, "textures/gui/guide_book.png");

	// Buttons
	private GuiGuideBookSlider slider;
	private GuiGuideBookTexturedButton home;
	private int maxTabs = 10;
	private ArrayList<GuiGuideBookTabButton> tabList;
	private GuiButton button;
	private int tabID = 50;

	private int mouseX, mouseY;
	public static SavedTab currentPage;
	private SavedTab menuPage;
	private static String title;

	// Book Experimental variables
	private EnumHand hand;

	// Gui Drawing variables
	@Getter
	private int guiX0, guiY0, guiX1, guiY1, guiX2, guiY2, guiX3, guiY3, guiWidth, guiHeight, guiInnerWidth, guiInnerHeight, guiBgX0, guiBgY0, guiBgX1, guiBgY1;
	@Getter
	private int guiSepX0, guiSepY0, guiSepY1, guiSepX1, guiSepY2, guiSepY3, guiSliderX, guiSliderY0, guiSliderY1;
	private final int guiBorderThickness = 16, guiShadowThickness = 5, guiSeparatorThickness = 6;
	private final int guiBorderWithShadowThickness = guiBorderThickness + guiShadowThickness;
	private final int guiSliderWidth = 12, guiSliderHeight = 15, guiSepExtra = 1;
	private final int guiTabWidth = 24, guiTabHeight = 24, guiFullTabHeight = 32;
	// Usable area
	@Getter
	private int areaX0, areaY0, areaX1, areaY1, areaAcrossX, areaCenterX, areaAcrossY, areaCurrentlyDrawnY, areaOffsetCenterX;
	// Menu Tiles and Text sizes
	@Getter
	private final int tileSpacing = 5;
	@Getter
	private int tileMax;
	@Getter
	private final int tileSize = 40;

	// Texture atlas constants
	private static final int atlasWidth = 256;
	private static final int atlasHeight = 256;
	private static final double atlasWidthScale = 1.0D / atlasWidth;
	private static final double atlasHeightScale = 1.0D / atlasHeight;
	// Gui Atlas constants
	private final int guiAtlasWidth = 64, guiAtlasHeight = 64;
	private final int guiAtlasBgU0 = 64, guiAtlasBgV0 = 0, guiAtlasBgU1 = 80, guiAtlasBgV1 = 16;
	private final int guiAtlasU0 = 0, guiAtlasU1 = guiBorderWithShadowThickness, guiAtlasU2 = guiAtlasWidth - guiBorderWithShadowThickness, guiAtlasU3 = guiAtlasWidth;
	private final int guiAtlasV0 = 0, guiAtlasV1 = guiBorderWithShadowThickness, guiAtlasV2 = guiAtlasWidth - guiBorderWithShadowThickness, guiAtlasV3 = guiAtlasWidth;
	private final int guiAtlasSepU0 = 96, guiAtlasSepV0 = 32, guiAtlasSepV1 = 33, guiAtlasSepU1 = 112, guiAtlasSepV2 = 63, guiAtlasSepV3 = 64;

	public GuiGuideBook(EnumHand hand) {
		super();
		this.hand = hand;
		currentPage = new SavedTab();
		this.menuPage = new SavedTab();
		this.tabList = new ArrayList<>();
	}

	/*
	 * Chooses the appropriate draw methods for the current to be drawn content
	 */
	protected void drawCurrentEvent() {
		int yOffset = slider.enabled ? -(int) (MathHelper.clamp(slider.getProgress() * (areaCurrentlyDrawnY - areaAcrossY), 0, areaCurrentlyDrawnY - areaAcrossY)) : 0;
		areaCurrentlyDrawnY = DrawablePage.draw(mc, currentPage, this, mouseX, mouseY, yOffset);
	}

	/*
	 * Calculates varius coordinates based on current width and height
	 */
	protected void calculateConstraints() {
		// Start Gui
		guiWidth = (int) (6.0D / 8.0D * this.width);
		guiHeight = (int) (6.0D / 8.0D * this.height);
		guiInnerWidth = guiWidth - 2 * guiBorderWithShadowThickness;
		guiInnerHeight = guiHeight - 2 * guiBorderWithShadowThickness;
		guiX0 = (int) (1.0D / 8.0D * this.width);
		guiY0 = (int) (1.0D / 8.0D * this.height);
		guiX1 = guiX0 + guiBorderWithShadowThickness;
		guiY1 = guiY0 + guiBorderWithShadowThickness;
		guiX2 = guiX1 + guiInnerWidth;
		guiY2 = guiY1 + guiInnerHeight;
		guiX3 = guiX2 + guiBorderWithShadowThickness;
		guiY3 = guiY2 + guiBorderWithShadowThickness;
		guiBgX0 = guiX0 + guiBorderThickness;
		guiBgY0 = guiY0 + guiBorderThickness;
		guiBgX1 = guiX0 + guiWidth - guiBorderThickness;
		guiBgY1 = guiY0 + guiHeight - guiBorderThickness;
		guiSliderX = guiX3 - guiBorderThickness - guiSliderWidth;
		guiSliderY0 = guiY0 + guiBorderThickness;
		guiSliderY1 = guiY3 - guiBorderThickness;
		guiSepX0 = guiX2 - guiSeparatorThickness - guiSliderWidth;
		guiSepY0 = guiSliderY0 - guiSepExtra;
		guiSepY1 = guiSliderY0;
		guiSepX1 = guiSepX0 + guiSeparatorThickness + 2 * guiShadowThickness;
		guiSepY2 = guiSliderY1;
		guiSepY3 = guiSliderY1 + guiSepExtra;
		// End Gui
		// Calculate usable area
		areaX0 = guiX1;
		areaY0 = guiY1;
		areaX1 = guiX2 - 2 * guiShadowThickness - guiSeparatorThickness;
		areaY1 = guiY2;
		areaAcrossX = areaX1 - areaX0;
		areaCenterX = areaX0 + areaAcrossX / 2;
		areaAcrossY = areaY1 - areaY0;
		areaOffsetCenterX = (int) (1.0 / 4 * areaAcrossX);
		// End usable area
		// Menu tiles and text
		tileMax = (int) ((areaAcrossX) / (float) (tileSize + tileSpacing));
		// End menu
	}

	/**
	 * Gets information from the item's nbt
	 */
	protected boolean getDataFromNBT() {
		ItemStack bookItemStack = mc.player.getHeldItem(hand);
		if (bookItemStack.hasTagCompound()) {
			NBTTagCompound nbt = bookItemStack.getTagCompound();
			currentPage = new SavedTab().fromTag(nbt.getCompoundTag("page"));
			NBTTagList tagList = nbt.getTagList("bookmarks", 10);
			for (NBTBase tag : tagList) tabList.add(new GuiGuideBookTabButton(tabID++, guiX3 - 2 - 2 * guiTabWidth + (tabList.size() * guiTabWidth), guiY0, new SavedTab().fromTag((NBTTagCompound) tag)));
			return true;
		} else {
			SavedTab defaultPage = new SavedTab();
			currentPage = new SavedTab(defaultPage);
			return true;
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.drawTransparentOverlay();
		this.drawCurrentEvent();
		slider.enabled = areaCurrentlyDrawnY > areaAcrossY;
		super.drawScreen(mouseX, mouseY, partialTicks);
		for (GuiGuideBookTabButton tab : tabList) tab.drawButton(mc, mouseX, mouseY, partialTicks);
		this.drawGuiScroll();
		this.drawTitle();
	}

	@Override
	public void initGui() {
		if (!loadedNBT) loadedNBT = this.getDataFromNBT();
		this.calculateConstraints();
		title = updateTitle();
		this.slider = this.addButton(new GuiGuideBookSlider(0, guiSliderX, guiSliderY0, guiSliderY1, zTitleButtons, currentPage.getProgress(), guiSliderWidth, guiSliderHeight));
		this.slider.enabled = false;
		this.home = this.addButton(new GuiGuideBookTexturedButton(1, guiX3 - guiTabWidth, guiY0 - guiTabHeight, guiTabWidth, guiFullTabHeight, 16, 64, zTitleButtons, 128, 0, 16, 16, false, GuiGuideBookTexturedButton.EnumButtonType.TAB));
		this.button = this.addButton(new GuiGuideBookTexturedButton(4, guiX3 - 18 - guiTabWidth + 4, guiY0 - 18, 16, 16, 0, 0, zTitleButtons, 192, 0, 16, 16, true, GuiGuideBookTexturedButton.EnumButtonType.NORMAL));
		this.updateButtonVisibility();
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void onGuiClosed() {
		currentPage.setProgress(slider.getProgress());
		ArrayList<SavedTab> tabs = new ArrayList<>();
		for (GuiGuideBookTabButton tab : tabList) tabs.add(tab.getTab());
		ItemGuideBook.setCurrentPage(Minecraft.getMinecraft().player.getHeldItem(hand), currentPage, tabs, hand);
		super.onGuiClosed();
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
			case 1:
				currentPage = new SavedTab(menuPage);
				slider.reset();
				break;
			case 2:
				if (!currentPage.isEqual(menuPage)) {
					if (tabNotFound(currentPage)) tryAddTab(currentPage);
				}
			default:
				break;
		}
		updateButtonVisibility();
	}

	private void tryAddTab(SavedTab currentPage) {
		tabList.add(new GuiGuideBookTabButton(tabID++, guiX3 - 2 - 2 * guiTabWidth + (tabList.size() * guiTabWidth), guiY0, currentPage));
		updateButtonVisibility();
	}

	private void tryRemoveTab(GuiGuideBookTabButton tab) {
		//tabList.remove(tab);
		updateButtonVisibility();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (!currentPage.getMenuItems().isEmpty()) {
			for (MenuItemsDivision div : currentPage.getMenuItems()) {
				for (MenuItem item : div.getList()) {
					if (item.mousePressed()) {
						item.playPressSound(this.mc.getSoundHandler());
						this.pressedItem(item);
						updateButtonVisibility();
						return;
					}
				}
			}
		}
		if(tabList != null && tabList.size() > 0) {
			for (GuiGuideBookTabButton tab : tabList) {
				if (tab.mousePressed(mc, mouseX, mouseY)) {
					if (mouseButton == 0) {
						currentPage = new SavedTab(tab.getTab());
						tab.playPressSound(this.mc.getSoundHandler());
					} else if (mouseButton == 1) {
						tryRemoveTab(tab);
					} else if (mouseButton == 2) {
						tab.cycleColor();
					}
				}
			}
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
		updateButtonVisibility();
	}

	private void pressedItem(MenuItem item) {
		selectPage(item);
	}

	protected void selectPage(MenuItem item) {
		currentPage = new SavedTab(item.getTarget(), 0, 0.0F);
		title = updateTitle();
		updateButtonVisibility();
	}

	protected static String updateTitle() {
		return currentPage.getMetadata().getTitle();
	}

	protected void updateButtonVisibility() {
		this.home.visible = !currentPage.isEqual(menuPage);
		int offset = 0;
		for (GuiGuideBookTabButton tab : tabList) {
			tab.y = guiY0;
			tab.x = guiX3 - 2 - 2 * guiTabWidth - offset;
			offset += guiTabWidth;
			tab.isActive = tab.getTab().isEqual(currentPage);
		}
		this.button.visible = !currentPage.isEqual(menuPage) && tabList.size() < maxTabs;
		this.button.enabled = tabNotFound(currentPage);
		this.button.x = guiX3 - 20 - guiTabWidth - offset;
		title = updateTitle();
	}

	protected boolean tabNotFound(SavedTab checkTab) {
		for (GuiGuideBookTabButton tab : tabList) if (tab.getTab().isEqual(checkTab)) return false;
		return true;
	}

	/**
	 * Draws the main title on the centre of the top border of the GUI
	 */
	protected void drawTitle() {
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, 200);
		this.drawCenteredString(this.fontRenderer, title, this.width / 2, guiY0 + 4, 0xFFFFFF);
		GlStateManager.popMatrix();
	}

	/**
	 * Draws the main GUI border and background
	 */
	@SuppressWarnings("Duplicates")
	protected void drawGui() {
		mc.renderEngine.bindTexture(GUI_BOOK_TEXTURE);
		// Background
		this.drawRepeatingSquare(guiBgX0, guiBgY0, guiBgX1, guiBgY1, zBackground, guiAtlasBgU0, guiAtlasBgV0, guiAtlasBgU1, guiAtlasBgV1, false);
		// Corners: TopLeft, TopRight, BottomLeft & BottomRight
		this.drawStretchingSquare(guiX0, guiY0, guiX1, guiY1, zFrame, guiAtlasU0, guiAtlasV0, guiAtlasU1, guiAtlasV1, true);
		this.drawStretchingSquare(guiX2, guiY0, guiX3, guiY1, zFrame, guiAtlasU2, guiAtlasV0, guiAtlasU3, guiAtlasV1, true);
		this.drawStretchingSquare(guiX0, guiY2, guiX1, guiY3, zFrame, guiAtlasU0, guiAtlasV2, guiAtlasU1, guiAtlasV3, true);
		this.drawStretchingSquare(guiX2, guiY2, guiX3, guiY3, zFrame, guiAtlasU2, guiAtlasV2, guiAtlasU3, guiAtlasV3, true);
		// Edges: Top, Bottom, Left & Right
		this.drawStretchingSquare(guiX1, guiY0, guiX2, guiY1, zFrame, guiAtlasU1, guiAtlasV0, guiAtlasU2, guiAtlasV1, true);
		this.drawStretchingSquare(guiX1, guiY2, guiX2, guiY3, zFrame, guiAtlasU1, guiAtlasV2, guiAtlasU2, guiAtlasV3, true);
		this.drawStretchingSquare(guiX0, guiY1, guiX1, guiY2, zFrame, guiAtlasU0, guiAtlasV1, guiAtlasU1, guiAtlasV2, true);
		this.drawStretchingSquare(guiX2, guiY1, guiX3, guiY2, zFrame, guiAtlasU2, guiAtlasV1, guiAtlasU3, guiAtlasV2, true);

	}

	/**
	 * Draws the main GUI border with a separator for the slider button
	 */
	protected void drawGuiScroll() {
		drawGui();
		this.drawStretchingSquare(guiSepX0, guiSepY0, guiSepX1, guiSepY1, zFrame, guiAtlasSepU0, guiAtlasSepV0, guiAtlasSepU1, guiAtlasSepV1, true);
		this.drawStretchingSquare(guiSepX0, guiSepY1, guiSepX1, guiSepY2, zFrame, guiAtlasSepU0, guiAtlasSepV1, guiAtlasSepU1, guiAtlasSepV2, true);
		this.drawStretchingSquare(guiSepX0, guiSepY2, guiSepX1, guiSepY3, zFrame, guiAtlasSepU0, guiAtlasSepV2, guiAtlasSepU1, guiAtlasSepV3, true);
	}

	/**
	 * Draws a transparent layer around the GUI to erase the text drawn under
	 */
	protected void drawTransparentOverlay() {
		GlStateManager.pushMatrix();
		GlStateManager.disableAlpha();
		mc.renderEngine.bindTexture(GUI_BOOK_TEXTURE);
		this.drawStretchingSquare(0, 0, this.width, guiY0 + guiShadowThickness, zFrame, 0, 0, 0, 0, true);
		this.drawStretchingSquare(0, guiY3 - guiShadowThickness, this.width, this.height, zFrame, 0, 0, 0, 0, true);
		this.drawStretchingSquare(0, guiY0 + guiShadowThickness, guiX0 + guiShadowThickness, guiY3 - guiShadowThickness, zFrame, 0, 0, 0, 0, true);
		this.drawStretchingSquare(guiX3 - guiShadowThickness, guiY0 + guiShadowThickness, this.width, guiY3 - guiShadowThickness, zFrame, 0, 0, 0, 0, true);
		GlStateManager.enableAlpha();
		GlStateManager.popMatrix();
	}

	/**
	 * Draws the underlay below where the page arrows and page count are to be drawn
	 */
	protected void drawCenteredArrowUnderlay(int width) {
		//Positioning
		int slideX1 = areaCenterX - width / 2;
		int slideX2 = areaCenterX + width / 2;
		int slideY0 = guiY3 - 4;
		int slideX0 = slideX1 - 12;
		int slideX3 = slideX2 + 12;
		int slideY1 = slideY0 + 14;
		//Texture
		int slideU0 = 0;
		int slideU1 = 12;
		int slideV0 = 96;
		int slideU2 = 20;
		int slideU3 = 32;
		int slideV1 = 110;
		// Draw Left & Right slides.
		this.drawStretchingSquare(slideX0, slideY0, slideX1, slideY1, zFrame, slideU0, slideV0, slideU1, slideV1, true);
		this.drawStretchingSquare(slideX2, slideY0, slideX3, slideY1, zFrame, slideU2, slideV0, slideU3, slideV1, true);
		// Draw in-between slides.
		this.drawStretchingSquare(slideX1, slideY0, slideX2, slideY1, zFrame, slideU1, slideV0, slideU2, slideV1, true);
	}

	/**
	 * Draws a text line that separates tile groups.
	 */
	public static void drawMenuText(Minecraft mc, int x, int y, int sizeX, int sizeY, String text) {
		GlStateManager.color(1.0F, 1.0F, 1.0F);
		int text$size = sizeY / 2;
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 5, ((sizeY - 8) / 2.0) + y, 5);
		GlStateManager.scale(text$size / 8, text$size / 8, 0);
		mc.fontRenderer.drawStringWithShadow(text, 0, 0, 0xFFFFFF);
		GlStateManager.popMatrix();
		GlStateManager.color(1.0F, 1.0F, 1.0F);
	}

	/**
	 * Draws a square based on two vertices with (stretching) texture also determined by two vertices: TopLeft & BottomRight
	 * The vertex(xy) and vertex1(xy) translate to vertex(uv) and vertex1(uv) in the texture atlas.
	 * The Y increases from the top to the bottom. Blending turned off.
	 */
	public static void drawStretchingSquare(int x0, int y0, int x1, int y1, int z, double u0, double v0, double u1, double v1) {
		drawStretchingSquare(x0, y0, x1, y1, z, u0, v0, u1, v1, false);
	}

	/**
	 * Draws a square based on two vertices with (stretching) texture also determined by two vertices: TopLeft & BottomRight
	 * The vertex(xy) and vertex1(xy) translate to vertex(uv) and vertex1(uv) in the texture atlas.
	 * The Y increases from the top to the bottom. Blend optional
	 */
	public static void drawStretchingSquare(int x0, int y0, int x1, int y1, int z, double u0, double v0, double u1, double v1, boolean blend) {
		drawStretchingSquare(x0, y0, x1, y1, z, u0, v0, u1, v1, blend, 0xFFFFFF);
	}

	public static void drawStretchingSquare(int x0, int y0, int x1, int y1, int z, double u0, double v0, double u1, double v1, boolean blend, int color) {

		Minecraft.getMinecraft().renderEngine.bindTexture(GUI_BOOK_TEXTURE);

		float r = (color >> 16 & 255) / 255.0F;
		float g = (color >> 8 & 255) / 255.0F;
		float b = (color & 255) / 255.0F;

		u0 *= atlasWidthScale;
		v0 *= atlasHeightScale;
		u1 *= atlasWidthScale;
		v1 *= atlasHeightScale;
		// Four vertices of square following order: TopLeft, TopRight, BottomLeft, BottomRight
		if (blend) GlStateManager.enableBlend();
		if (blend) GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(r, g, b, 1.0F);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos(x0, y1, z).tex(u0, v1).endVertex();
		bufferbuilder.pos(x1, y1, z).tex(u1, v1).endVertex();
		bufferbuilder.pos(x1, y0, z).tex(u1, v0).endVertex();
		bufferbuilder.pos(x0, y0, z).tex(u0, v0).endVertex();
		tessellator.draw();
		if (blend) GlStateManager.disableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}

	/**
	 * Draws a square based on two vertices with repeating texture also determined by two vertices: TopLeft & BottomRight
	 * If the texture size is smaller than the draw size the texture will be repeated until it fills all the area to be drawn.
	 * The Y increases from the top to the bottom.
	 */
	public static void drawRepeatingSquare(int x0, int y0, int x1, int y1, int z, double u0, double v0, double u1, double v1, boolean blend) {
		drawRepeatingSquare(x0, y0, x1, y1, z, u0, v0, u1, v1, blend, 0xFFFFFF);
	}

	public static void drawRepeatingSquare(int x0, int y0, int x1, int y1, int z, double u0, double v0, double u1, double v1, boolean blend, int color) {
		if (x1 - x0 <= u1 - u0 && y1 - y0 <= v1 - v0) {
			drawStretchingSquare(x0, y0, x1, y1, z, u0, v0, u1, v1, blend, color);
			return;
		}
		int x = x1 - x0;
		int y = y1 - y0;
		int u = (int) (u1 - u0);
		int v = (int) (v1 - v0);
		int timesX = (int) Math.floor(x / u);
		int timesY = (int) Math.floor(y / v);
		int remainderX = x % u;
		int remainderY = y % v;
		for (int i = 0; i <= timesY; i++) {
			for (int j = 0; j <= timesX; j++) {
				if (j == timesX && i == timesY) {
					drawStretchingSquare(x0 + (j * u), y0 + (i * v), x0 + (j * u) + remainderX, y0 + (i * v) + remainderY, z, u0, v0, u0 + remainderX, v0 + remainderY, blend, color);
				} else if (j == timesX) {
					drawStretchingSquare(x0 + (j * u), y0 + (i * v), x0 + (j * u) + remainderX, y0 + ((i + 1) * v), z, u0, v0, u0 + remainderX, v1, blend, color);
				} else if (i == timesY) {
					drawStretchingSquare(x0 + (j * u), y0 + (i * v), x0 + ((j + 1) * u), y0 + (i * v) + remainderY, z, u0, v0, u1, v0 + remainderY, blend, color);
				} else {
					drawStretchingSquare(x0 + (j * u), y0 + (i * v), x0 + ((j + 1) * u), y0 + ((i + 1) * v), z, u0, v0, u1, v1, blend, color);
				}
			}
		}
	}

	public static void drawBoxedCenteredString(Minecraft mc, String text, int x, int y, int z) {
		int width = mc.fontRenderer.getStringWidth(text);
		int x1 = x - (width / 2 + 1);
		int x0 = x1 - 4;
		int x2 = x + (width / 2 + 1);
		int x3 = x2 + 4;
		int y0 = y;
		int y1 = y0 + 4;
		int y2 = y1 + 10;
		int y3 = y2 + 4;
		int u0 = 112;
		int v0 = 32;
		int u1 = 116;
		int v1 = 36;
		int u2 = 124;
		int v2 = 44;
		int u3 = 128;
		int v3 = 48;
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, z);
		GlStateManager.translate(0.0F, 0.0F, 100.0F);
		drawCenteredStringStatic(mc.fontRenderer, text, x, y + 5, 0xFFFFFF);
		GlStateManager.translate(0.0F, 0.0F, -100.0F);
		GlStateManager.enableAlpha();
		mc.renderEngine.bindTexture(GUI_BOOK_TEXTURE);
		// Background
		drawRepeatingSquare(x1, y1, x2, y2, z, u1, v1, u2, v2, false);
		// Corners: TopLeft, TopRight, BottomLeft & BottomRight
		drawStretchingSquare(x0, y0, x1, y1, z, u0, v0, u1, v1, true);
		drawStretchingSquare(x2, y0, x3, y1, z, u2, v0, u3, v1, true);
		drawStretchingSquare(x0, y2, x1, y3, z, u0, v2, u1, v3, true);
		drawStretchingSquare(x2, y2, x3, y3, z, u2, v2, u3, v3, true);
		// Edges: Top, Bottom, Left & Right
		drawStretchingSquare(x1, y0, x2, y1, z, u1, v0, u2, v1, true);
		drawStretchingSquare(x1, y2, x2, y3, z, u1, v2, u2, v3, true);
		drawStretchingSquare(x0, y1, x1, y2, z, u0, v1, u1, v2, true);
		drawStretchingSquare(x2, y1, x3, y2, z, u2, v1, u3, v2, true);
		GlStateManager.disableAlpha();
		GlStateManager.popMatrix();
	}

	public static void drawCenteredStringStatic(FontRenderer fontRendererIn, String text, int x, int y, int color) {
		fontRendererIn.drawStringWithShadow(text, (float) (x - fontRendererIn.getStringWidth(text) / 2), (float) y, color);
	}

	@Override
	public void drawHorizontalLine(int startX, int endX, int y, int color) {
		super.drawHorizontalLine(startX, endX, y, color);
	}

	public static class MenuItemsDivision {

		public String name;

		@Getter
		private final ArrayList<MenuItem> list;

		public MenuItemsDivision(String name, ArrayList<MenuItem> list) {
			this.list = list;
			this.name = name;
		}

		public void add(MenuItem item) {
			list.add(item);
		}
	}
}
