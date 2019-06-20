package logisticspipes.gui.guidebook;

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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import lombok.Getter;
import lombok.Setter;
import org.lwjgl.opengl.GL11;

import logisticspipes.LPConstants;
import logisticspipes.gui.guidebook.book.MenuItem;
import logisticspipes.items.ItemGuideBook;
import logisticspipes.utils.GuideBookContents;
import logisticspipes.utils.string.StringUtils;

public class GuiGuideBook extends GuiScreen {

	/*
	 * TODO - Back button or bookmarks, whichever is more wanted
	 *		- Image support
	 *		- Text personalization support (bold, italics, underline)
	 * 			(I'd suggest a subset of markdown format for that, which has just the italic/bold/underline syntax, so that it can be expanded later if needed- @farfetched)
	 * 		- Link?
	 * 		- Search bar?
	 * 		- NEI/JEI integration - Information page.
	 */

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
	private final int z$tooltip = 20;      // Tooltip z
	private final int z$titleButtons = 15; // Title and Buttons Z
	private final int z$frame = 10;        // Frame Z
	private final int z$text = 5;          // Text/Information Z
	private final int z$background = 0;    // Background Z

	private static final ResourceLocation GUI_BOOK_TEXTURE = new ResourceLocation(LPConstants.LP_MOD_ID, "textures/gui/guide_book.png");

	private GuideBookContents gbc;
	//////// Buttons
	private GuiGuideBookSlider slider;
	private GuiGuideBookTexturedButton home, prevPage, nextPage;
	private int maxTabs = 10;
	private ArrayList<GuiGuideBookTabButton> tabList;
	private SavedTabs savedTabs;
	///////
	private int mouseX, mouseY;
	private ArrayList<MenuItemsDivision> divisionsList;
	private State state;
	private PageInformation currentPage;
	private PageState page;
	private MenuState menu;
	private String title;

	//////// Experimental variables
	//// Book
	private EnumHand hand;

	//////// Drawing variables
	//// Gui
	private int gui$x0, gui$y0, gui$x1, gui$y1, gui$x2, gui$y2, gui$x3, gui$y3, gui$width, gui$height, gui$innerWidth, gui$innerHeight, gui$bgX0, gui$bgY0, gui$bgX1, gui$bgY1;
	private int gui$sepX0, gui$sepY0, gui$sepY1, gui$sepX1, gui$sepY2, gui$sepY3, gui$sliderX, gui$sliderY0, gui$sliderY1;
	private final int gui$borderThickness = 16, gui$shadowThickness = 5, gui$separatorThickness = 6;
	private final int gui$borderWithShadowThickness = gui$borderThickness + gui$shadowThickness;
	private final int gui$sliderWidth = 12, gui$sliderHeight = 15, gui$sepExtra = 1;
	private final int gui$tagWidth = 24, gui$tagHeight = 24, gui$fullTagHeight = 32;
	private final int gui$arrowWidth = 22, gui$arrowHeight = 16;
	//// Usable area
	private int area$x0, area$y0, area$x1, area$y1, area$acrossX, area$xCenter, area$acrossY, area$currentlyDrawnY, area$offsetCenterX;
	//// Menu Tiles and Text sizes
	private int tile$spacing, tile$max;
	private final int tile$size = 40;

	//////// Texture atlas constants
	//// Atlas
	private static final int atlas$width = 256;
	private static final int atlas$height = 256;
	private static final double atlas$widthScale = 1.0D / atlas$width;
	private static final double atlas$heightScale = 1.0D / atlas$height;
	//// Gui
	private final int atlas$gui$width = 64, atlas$gui$height = 64;
	private final int atlas$gui$bgU0 = 64, atlas$gui$bgV0 = 0, atlas$gui$bgU1 = 80, atlas$gui$bgV1 = 16;
	private final int atlas$gui$u0 = 0, atlas$gui$u1 = gui$borderWithShadowThickness, atlas$gui$u2 = atlas$gui$width - gui$borderWithShadowThickness, atlas$gui$u3 = atlas$gui$width;
	private final int atlas$gui$v0 = 0, atlas$gui$v1 = gui$borderWithShadowThickness, atlas$gui$v2 = atlas$gui$width - gui$borderWithShadowThickness, atlas$gui$v3 = atlas$gui$width;
	private final int atlas$gui$sepU0 = 96, atlas$gui$sepV0 = 32, atlas$gui$sepV1 = 33, atlas$gui$sepU1 = 112, atlas$gui$sepV2 = 63, atlas$gui$sepV3 = 64;

	public GuiGuideBook(EnumHand hand, GuideBookContents gbc) {
		super();
		this.hand = hand;
		this.gbc = gbc;
		this.currentPage = new PageInformation();
		this.page = new PageState();
		this.menu = new MenuState();
		this.divisionsList = new ArrayList<>();
		this.savedTabs = new SavedTabs(maxTabs);
		this.tabList = new ArrayList<>();
		for (GuideBookContents.Division div : gbc.getDivisions()) {
			divisionsList.add(new MenuItemsDivision());
			for (GuideBookContents.Chapter chapter : div.getChapters()) {
				divisionsList.get(div.getDindex()).getList().add(new MenuItem(chapter));
			}
		}
	}

	/*
	 * Chooses the appropriate draw methods for the current to be drawn content
	 */
	protected void drawCurrentEvent() {
		int yOffset = slider.enabled ? -(int) (MathHelper.clamp(slider.getProgress() * (area$currentlyDrawnY - area$acrossY), 0, area$currentlyDrawnY - area$acrossY)) : 0;
		area$currentlyDrawnY = state.draw(mc, mouseX, mouseY, yOffset);
	}

	/*
	 * Calculates varius coordinates based on current width and height
	 */
	protected void calculateConstraints() {
		// Start Gui
		gui$width = (int) (6.0D / 8.0D * this.width);
		gui$height = (int) (6.0D / 8.0D * this.height);
		gui$innerWidth = gui$width - 2 * gui$borderWithShadowThickness;
		gui$innerHeight = gui$height - 2 * gui$borderWithShadowThickness;
		gui$x0 = (int) (1.0D / 8.0D * this.width);
		gui$y0 = (int) (1.0D / 8.0D * this.height);
		gui$x1 = gui$x0 + gui$borderWithShadowThickness;
		gui$y1 = gui$y0 + gui$borderWithShadowThickness;
		gui$x2 = gui$x1 + gui$innerWidth;
		gui$y2 = gui$y1 + gui$innerHeight;
		gui$x3 = gui$x2 + gui$borderWithShadowThickness;
		gui$y3 = gui$y2 + gui$borderWithShadowThickness;
		gui$bgX0 = gui$x0 + gui$borderThickness;
		gui$bgY0 = gui$y0 + gui$borderThickness;
		gui$bgX1 = gui$x0 + gui$width - gui$borderThickness;
		gui$bgY1 = gui$y0 + gui$height - gui$borderThickness;
		gui$sliderX = gui$x3 - gui$borderThickness - gui$sliderWidth;
		gui$sliderY0 = gui$y0 + gui$borderThickness;
		gui$sliderY1 = gui$y3 - gui$borderThickness;
		gui$sepX0 = gui$x2 - gui$separatorThickness - gui$sliderWidth;
		gui$sepY0 = gui$sliderY0 - gui$sepExtra;
		gui$sepY1 = gui$sliderY0;
		gui$sepX1 = gui$sepX0 + gui$separatorThickness + 2 * gui$shadowThickness;
		gui$sepY2 = gui$sliderY1;
		gui$sepY3 = gui$sliderY1 + gui$sepExtra;
		// End Gui
		// Calculate usable area
		area$x0 = gui$x1;
		area$y0 = gui$y1;
		area$x1 = gui$x2 - 2 * gui$shadowThickness - gui$separatorThickness;
		area$y1 = gui$y2;
		area$acrossX = area$x1 - area$x0;
		area$xCenter = area$x0 + area$acrossX / 2;
		area$acrossY = area$y1 - area$y0;
		area$offsetCenterX = (int) (1.0 / 4 * area$acrossX);
		// End usable area
		// Menu tiles and text
		tile$max = area$acrossX / tile$size;
		tile$spacing = 5;
		while ((tile$max * (tile$size + tile$spacing)) - tile$spacing > area$acrossX) {
			tile$max--;
			tile$spacing = (int) ((area$acrossX % (tile$size * tile$max)) / (float) (tile$max - 1));
		}
		tile$spacing = (int) ((area$acrossX % (tile$size * tile$max)) / (float) (tile$max - 1));
		// End menu
	}

	/*
	 * Gets information from the item's nbt
	 */
	protected void getDataFromNBT() {
		if (loadedNBT) return;
		ItemStack bookItemStack = mc.player.getHeldItem(hand);
		if (bookItemStack.hasTagCompound()) {
			state = page;
			NBTTagCompound nbtTagCompound = bookItemStack.getTagCompound();
			currentPage.page = gbc.getPage(nbtTagCompound.getInteger("division"), nbtTagCompound.getInteger("chapter"), nbtTagCompound.getInteger("page"));
			currentPage.setProgress(nbtTagCompound.getFloat("sliderProgress"));
		} else {
			state = menu;
		}
		loadedNBT = true;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.drawTransparentOverlay();
		this.drawCurrentEvent();
		slider.enabled = area$currentlyDrawnY > area$acrossY;
		super.drawScreen(mouseX, mouseY, partialTicks);
		for (GuiGuideBookTabButton tab : tabList) tab.drawButton(mc, mouseX, mouseY, partialTicks);
		this.drawGuiScroll();
		if (prevPage.visible && nextPage.visible) this.drawCenteredArrowUnderlay(area$offsetCenterX * 2);
		this.drawTitle();
	}

	@Override
	public void initGui() {
		this.tabList.clear();
		this.getDataFromNBT();
		this.calculateConstraints();
		this.updateTitle();
		this.slider = this.addButton(new GuiGuideBookSlider(0, gui$sliderX, gui$sliderY0, gui$sliderY1, z$titleButtons, currentPage.getProgress(), gui$sliderWidth, gui$sliderHeight));
		this.slider.enabled = false;
		this.home = this.addButton(new GuiGuideBookTexturedButton(1, gui$x3 - gui$tagWidth, gui$y0 - gui$tagHeight, gui$tagWidth, gui$fullTagHeight, 16, 64, z$titleButtons, 128, 0, 16, 16, false, GuiGuideBookTexturedButton.EnumButtonType.TAB));
		this.prevPage = this.addButton(new GuiGuideBookTexturedButton(2, area$xCenter - area$offsetCenterX, gui$y3 - 12, gui$arrowWidth, gui$arrowHeight, 0, 0, z$titleButtons, 144, 0, 24, 16, true, GuiGuideBookTexturedButton.EnumButtonType.NORMAL));
		this.nextPage = this.addButton(new GuiGuideBookTexturedButton(3, area$xCenter + area$offsetCenterX - gui$arrowWidth, gui$y3 - 12, gui$arrowWidth, gui$arrowHeight, 0, 0, z$titleButtons, 168, 0, 24, 16, true, GuiGuideBookTexturedButton.EnumButtonType.NORMAL));
		for (int i = 0; i < maxTabs; i++) {
			tabList.add(new GuiGuideBookTabButton(i, gui$x3 - 2 - 2 * gui$tagWidth - (i * gui$tagWidth), gui$y0, gui$tagWidth, gui$tagHeight, ""));
		}
		this.updateButtonVisibility();
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void onGuiClosed() {
		currentPage.setProgress(slider.getProgress());
		ItemGuideBook.setCurrentPage(Minecraft.getMinecraft().player.getHeldItem(hand), currentPage, hand);
		super.onGuiClosed();
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
			case 1:
				state = menu;
				slider.reset();
				break;
			case 2:
				prevPage();
				break;
			case 3:
				nextPage();
				break;
			default:
				break;
		}
		updateTitle();
		updateButtonVisibility();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (state == menu)
			for (MenuItemsDivision div : divisionsList) {
				for (MenuItem item : div.getList()) {
					if (item.mousePressed()) {
						item.playPressSound(this.mc.getSoundHandler());
						this.pressedItem(item);
						updateButtonVisibility();
						return;
					}
				}
			}
		for (GuiGuideBookTabButton tab : tabList) {
			if (tab.mousePressed(mc, mouseX, mouseY)) {
				tab.playPressSound(this.mc.getSoundHandler());
				if (!tab.isSet && state != menu) {
					PageInformation setPage = new PageInformation();
					setPage.page = currentPage.page;
					setPage.progress = currentPage.progress;
					if (savedTabs.addTab(new SavedTab(setPage, state))) {
						tab.isSet = true;
						if (tab.isSet) tab.isActive = true;
					}else{
						return;
					}
				} else if(tab.isSet && !tab.isActive){
					SavedTab info = new SavedTab(savedTabs.tabs.get(tab.id).page, savedTabs.tabs.get(tab.id).state);
					state = page;
					currentPage.set(info);
				}
				updateButtonVisibility();
				return;
			}
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
		updateButtonVisibility();
	}

	private void pressedItem(MenuItem item) {
		selectChapter(item);
	}

	protected void selectChapter(MenuItem item) {
		currentPage.set(item);
		state = page;
		updateTitle();
		updateButtonVisibility();
	}

	protected void updateTitle() {
		String title = "";
		title += gbc.getTitle();
		if (state == menu) title += ": Menu";
		else {
			title += ": " + gbc.getDivision(currentPage.getDivision()).getTitle();
			if (currentPage.getChapter() != -1) {
				title += " - " + gbc.getDivision(currentPage.getDivision()).getChapter(currentPage.getChapter()).getTitle();
			}
		}
		this.title = title;
	}

	protected void updateButtonVisibility() {
		this.prevPage.enabled = currentPage.getIndex() != 0;
		this.prevPage.visible = state == page;
		this.nextPage.enabled = currentPage.getIndex() < currentPage.getPageCount() - 1;
		this.nextPage.visible = state == page;
		this.home.visible = state != menu;
		for(GuiGuideBookTabButton tab: tabList){
			tab.visible = false;
			tab.isActive = false;
		}
		for (int i = 0; i < savedTabs.tabs.size(); i++) {
			tabList.get(i).visible = true;
			tabList.get(i).isSet = true;
			tabList.get(i).isActive = equals(savedTabs.tabs.get(i), new SavedTab(currentPage, state));
		}
		if (savedTabs.tabs.size() < maxTabs) tabList.get(savedTabs.tabs.size()).visible = true;
	}

	protected void nextPage() {
		if (nextPage.enabled) currentPage.nextPage();
		updateButtonVisibility();
	}

	protected void prevPage() {
		if (prevPage.enabled) currentPage.prevPage();
		updateButtonVisibility();
	}

	/* *********************************************** draw functions with a twist *********************************************** */

	/*
	 * Draws the main title on the centre of the top border of the GUI
	 */
	protected void drawTitle() {
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, z$titleButtons);
		this.drawCenteredString(this.fontRenderer, title, this.width / 2, gui$y0 + 4, 0xFFFFFF);
		GlStateManager.popMatrix();
	}

	/*
	 * Draws the page count in between the page arrows
	 */
	protected void drawPageCount() {
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, z$titleButtons);
		this.drawCenteredString(this.fontRenderer, currentPage.getIndex() + 1 + "/" + currentPage.getPageCount(), this.width / 2, gui$y2 + 13, 0xFFFFFF);
		GlStateManager.popMatrix();
	}

	/*
	 * Draws the main GUI border and background
	 */
	protected void drawGui() {
		mc.renderEngine.bindTexture(GUI_BOOK_TEXTURE);
		// Background
		this.drawRepeatingSquare(gui$bgX0, gui$bgY0, gui$bgX1, gui$bgY1, z$background, atlas$gui$bgU0, atlas$gui$bgV0, atlas$gui$bgU1, atlas$gui$bgV1, false);
		// Corners: TopLeft, TopRight, BottomLeft & BottomRight
		this.drawStretchingSquare(gui$x0, gui$y0, gui$x1, gui$y1, z$frame, atlas$gui$u0, atlas$gui$v0, atlas$gui$u1, atlas$gui$v1, true);
		this.drawStretchingSquare(gui$x2, gui$y0, gui$x3, gui$y1, z$frame, atlas$gui$u2, atlas$gui$v0, atlas$gui$u3, atlas$gui$v1, true);
		this.drawStretchingSquare(gui$x0, gui$y2, gui$x1, gui$y3, z$frame, atlas$gui$u0, atlas$gui$v2, atlas$gui$u1, atlas$gui$v3, true);
		this.drawStretchingSquare(gui$x2, gui$y2, gui$x3, gui$y3, z$frame, atlas$gui$u2, atlas$gui$v2, atlas$gui$u3, atlas$gui$v3, true);
		// Edges: Top, Bottom, Left & Right
		this.drawStretchingSquare(gui$x1, gui$y0, gui$x2, gui$y1, z$frame, atlas$gui$u1, atlas$gui$v0, atlas$gui$u2, atlas$gui$v1, true);
		this.drawStretchingSquare(gui$x1, gui$y2, gui$x2, gui$y3, z$frame, atlas$gui$u1, atlas$gui$v2, atlas$gui$u2, atlas$gui$v3, true);
		this.drawStretchingSquare(gui$x0, gui$y1, gui$x1, gui$y2, z$frame, atlas$gui$u0, atlas$gui$v1, atlas$gui$u1, atlas$gui$v2, true);
		this.drawStretchingSquare(gui$x2, gui$y1, gui$x3, gui$y2, z$frame, atlas$gui$u2, atlas$gui$v1, atlas$gui$u3, atlas$gui$v2, true);

	}

	/*
	 * Draws the main GUI border with a separator for the slider button
	 */
	protected void drawGuiScroll() {
		drawGui();
		this.drawStretchingSquare(gui$sepX0, gui$sepY0, gui$sepX1, gui$sepY1, z$frame, atlas$gui$sepU0, atlas$gui$sepV0, atlas$gui$sepU1, atlas$gui$sepV1, true);
		this.drawStretchingSquare(gui$sepX0, gui$sepY1, gui$sepX1, gui$sepY2, z$frame, atlas$gui$sepU0, atlas$gui$sepV1, atlas$gui$sepU1, atlas$gui$sepV2, true);
		this.drawStretchingSquare(gui$sepX0, gui$sepY2, gui$sepX1, gui$sepY3, z$frame, atlas$gui$sepU0, atlas$gui$sepV2, atlas$gui$sepU1, atlas$gui$sepV3, true);
	}

	/*
	 * Draws a transparent layer around the GUI to erase the text drawn under
	 */
	protected void drawTransparentOverlay() {
		GlStateManager.pushMatrix();
		GlStateManager.disableAlpha();
		mc.renderEngine.bindTexture(GUI_BOOK_TEXTURE);
		this.drawStretchingSquare(0, 0, this.width, gui$y0 + gui$shadowThickness, z$frame, 0, 0, 0, 0, true);
		this.drawStretchingSquare(0, gui$y3 - gui$shadowThickness, this.width, this.height, z$frame, 0, 0, 0, 0, true);
		this.drawStretchingSquare(0, gui$y0 + gui$shadowThickness, gui$x0 + gui$shadowThickness, gui$y3 - gui$shadowThickness, z$frame, 0, 0, 0, 0, true);
		this.drawStretchingSquare(gui$x3 - gui$shadowThickness, gui$y0 + gui$shadowThickness, this.width, gui$y3 - gui$shadowThickness, z$frame, 0, 0, 0, 0, true);
		GlStateManager.enableAlpha();
		GlStateManager.popMatrix();
	}

	/*
	 * Draws the underlay below where the page arrows and page count are to be drawn
	 */
	protected void drawCenteredArrowUnderlay(int width) {
		//Positioning
		int slide$x1 = area$xCenter - width / 2;
		int slide$x2 = area$xCenter + width / 2;
		int slide$y0 = gui$y3 - 4;
		int slide$x0 = slide$x1 - 12;
		int slide$x3 = slide$x2 + 12;
		int slide$y1 = slide$y0 + 14;
		//Texture
		int slide$u0 = 0;
		int slide$u1 = 12;
		int slide$v0 = 96;
		int slide$u2 = 20;
		int slide$u3 = 30;
		int slide$v1 = 110;
		// Draw Left & Right slides.
		this.drawStretchingSquare(slide$x0, slide$y0, slide$x1, slide$y1, z$frame, slide$u0, slide$v0, slide$u1, slide$v1, true);
		this.drawStretchingSquare(slide$x2, slide$y0, slide$x3, slide$y1, z$frame, slide$u2, slide$v0, slide$u3, slide$v1, true);
		// Draw in-between slides.
		this.drawStretchingSquare(slide$x1, slide$y0, slide$x2, slide$y1, z$frame, slide$u1, slide$v0, slide$u2, slide$v1, true);
	}

	/*
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

	/*
	 * Draws a square based on two vertices with (stretching) texture also determined by two vertices: TopLeft & BottomRight
	 * The vertex(xy) and vertex1(xy) translate to vertex(uv) and vertex1(uv) in the texture atlas.
	 * The Y increases from the top to the bottom. Blending turned off.
	 */
	public static void drawStretchingSquare(int x0, int y0, int x1, int y1, int z, double u0, double v0, double u1, double v1) {
		drawStretchingSquare(x0, y0, x1, y1, z, u0, v0, u1, v1, false);
	}

	/*
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

		u0 *= atlas$widthScale;
		v0 *= atlas$heightScale;
		u1 *= atlas$widthScale;
		v1 *= atlas$heightScale;
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

	/*
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

	boolean equals(SavedTab a, SavedTab b) {
		return a.page.getPage() == b.page.getPage() && a.page.getChapter() == b.page.getChapter() && a.page.getDivision() == b.page.getDivision() && a.state == b.state;
	}

	public class MenuItemsDivision {

		@Getter
		private ArrayList<MenuItem> list;

		public MenuItemsDivision() {
			this.list = new ArrayList<>();
		}

		public void add(MenuItem item) {
			list.add(item);
		}
	}

	public interface State {

		int draw(Minecraft mc, int mouseX, int mouseY, int yOffset);
	}

	public class MenuState implements State {

		public MenuState() {
			super();
		}

		@Override
		public int draw(Minecraft mc, int mouseX, int mouseY, int yOffset) {
			int area$currentY = 0;
			mouseX = mouseX < gui$x0 || mouseX > gui$x3 ? 0 : mouseX;
			mouseY = mouseY < gui$y0 || mouseY > gui$y3 ? 0 : mouseY;
			for (GuideBookContents.Division div : gbc.getDivisions()) {
				drawMenuText(mc, area$x0, area$y0 + area$currentY + yOffset, area$acrossX, 19, div.getTitle());
				area$currentY += 20;
				for (int chapterIndex = 0; chapterIndex < div.getChapters().size(); chapterIndex++) {
					divisionsList.get(div.getDindex()).getList().get(chapterIndex).drawMenuItem(mc, mouseX, mouseY, area$x0 + (chapterIndex % tile$max * (tile$size + tile$spacing)), area$y0 + area$currentY + yOffset, tile$size, tile$size, false);
					int tileBottom = (area$y0 + area$currentY + yOffset + tile$size);
					int maxBottom = area$y1;
					boolean above = tileBottom > maxBottom;
					divisionsList.get(div.getDindex()).getList().get(chapterIndex).drawTitle(mc, mouseX, mouseY, above);
					if ((chapterIndex + 1) % tile$max == 0) area$currentY += tile$spacing + tile$size;
					if (chapterIndex == div.getChapters().size() - 1) area$currentY += tile$size;
				}
			}
			return area$currentY;
		}
	}

	public class PageState implements State {

		@Override
		public int draw(Minecraft mc, int mouseX, int mouseY, int yOffset) {
			String unformattedText = currentPage.page.getText();
			ArrayList<String> text = StringUtils.splitLines(unformattedText, fontRenderer, area$acrossX);
			int area$currentY = 0;
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, z$text);
			for (String line : text) {
				fontRenderer.drawString(line, area$x0, area$y0 + area$currentY + yOffset, 0xFFFFFF);
				area$currentY += 10;
			}
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0F, 0.0F, z$titleButtons);
			drawPageCount();
			GlStateManager.popMatrix();
			return area$currentY;
		}
	}

	public class PageInformation {

		@Getter
		@Setter
		GuideBookContents.Page page;
		@Getter
		@Setter
		float progress;

		public int getIndex() {
			return page.getIndex();
		}

		public int getChapter() {
			return page.getCindex();
		}

		public int getDivision() {
			return page.getDindex();
		}

		public int getPageCount() {
			return gbc.getDivision(page.getDindex()).getChapter(page.getCindex()).getNPages();
		}

		public void changePage(PageInformation pageInfo) {
			this.page = pageInfo.page;
			this.progress = pageInfo.progress;
		}

		public void set(SavedTab tab){
			page = tab.page.page;
			progress = tab.page.progress;
			updateTitle();
		}

		public void set(MenuItem item) {
			set(item.getChapter().getDindex(), item.getChapter().getCindex(), 0, 0.0F);
		}

		public void set(int dindex, int cindex, int index, float progress) {
			this.page = gbc.getDivision(dindex).getChapter(cindex).getPage(index);
			this.progress = progress;
			updateTitle();
		}

		public void nextPage() {
			page = gbc.getDivision(page.getDindex()).getChapter(page.getCindex()).getPage(page.getIndex() + 1);
		}

		public void prevPage() {
			page = gbc.getDivision(page.getDindex()).getChapter(page.getCindex()).getPage(page.getIndex() - 1);
		}

		public PageInformation() {
			page = new GuideBookContents.Page(-1, 0, 0, "");
			progress = 0.0F;
		}
	}

	public class SavedTab {

		public GuiGuideBook.PageInformation page;
		public GuiGuideBook.State state;
		public int color;

		public void setTab(GuiGuideBook.PageInformation page, GuiGuideBook.State state) {
			this.page = page;
			this.state = state;
		}

		public SavedTab(PageInformation page, State state) {
			this.page = page;
			this.state = state;
			this.color = 0xFF0000;
		}

		public SavedTab() {
			page = new PageInformation();
			state = new PageState();
			this.color = 0xFF0000;
		}
	}

	public class SavedTabs {

		public ArrayList<SavedTab> tabs;
		private int maxTabs;

		public boolean addTab(SavedTab tab) {
			if (tabs.size() > maxTabs - 1) return false;
			for (SavedTab setTab : tabs) {
				if (equals(tab, setTab)) return false;
			}
			tabs.add(tab);
			return true;
		}

		public void removeTab(int tabIndex) {
			tabs.remove(tabIndex);
		}

		boolean equals(SavedTab a, SavedTab b) {
			return a.page.getPage() == b.page.getPage() && a.page.getChapter() == b.page.getChapter() && a.page.getDivision() == b.page.getDivision() && a.state == b.state;
		}

		public SavedTabs(int maxTabs) {
			tabs = new ArrayList<>();
			this.maxTabs = maxTabs;
		}
	}
}
