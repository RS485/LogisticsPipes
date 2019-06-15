package logisticspipes.gui.guidebook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import lombok.Getter;
import lombok.Setter;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;

import logisticspipes.LPConstants;
import logisticspipes.gui.guidebook.book.GuideBook;
import logisticspipes.gui.guidebook.book.MenuItem;
import logisticspipes.gui.guidebook.book.Page;
import logisticspipes.items.ItemGuideBook;
import logisticspipes.utils.string.StringUtils;

public class GuiGuideBook extends GuiScreen {

	/*
	 * Z Levels:
	 * Title and Buttons: 15
	 * Background frame : 10
	 * Text             : 5
	 * Background       : 0
	 * Background tint  : -5
	 */

	private final int z$titleButtons = 15; // Title and Buttons Z
	private final int z$frame = 10; // Frame Z
	private final int z$text = 5;// Text/Information Z
	private final int z$background = 0;  // Background Z



	/*
	 * TODO fix slider being 'enabled' when it's not supposed to.
	 */

	private static final ResourceLocation GUI_BOOK_TEXTURE = new ResourceLocation(LPConstants.LP_MOD_ID, "textures/gui/guide_book.png");

	private GuiButton nextPageBtn, prevPageBtn;
	private GuiGuideBookSlider slider;
	private GuiGuideBookTexturedButton home, prev;

	//////// Experimental variables
	//// Book
	private GuideBook book;
	private int currentChapter;
	private int currentPage;
	private int currentPageCount;
	private float currentSliderProgress;
	private EnumHand hand;

	//////// Drawing variables
	//// Gui
	private int gui$x0, gui$y0, gui$x1, gui$y1, gui$x2, gui$y2, gui$x3, gui$y3, gui$width, gui$height, gui$innerWidth, gui$innerHeight, gui$bgX0, gui$bgY0, gui$bgX1, gui$bgY1;
	private int gui$sepX0, gui$sepY0, gui$sepY1, gui$sepX1, gui$sepY2, gui$sepY3, gui$sliderX, gui$sliderY0, gui$sliderY1;
	private final int gui$borderThickness = 16, gui$shadowThickness = 5, gui$separatorThickness = 6;
	private final int gui$borderWithShadowThickness = gui$borderThickness + gui$shadowThickness;
	private final int gui$sliderWidth = 12, gui$sliderHeight = 15, gui$sepExtra = 1;
	private final int gui$tagWidth = 24, gui$tagHeight = 24, gui$fullTagHeight = 32;
	//// Usable area
	private int area$x0, area$y0, area$x1, area$y1, area$acrossX, area$acrossY;

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

	public GuiGuideBook(EnumHand hand) {
		super();
		this.hand = hand;
		book = new GuideBook("book");
	}

	protected void drawCurrentEvent(int mouseX, int mouseY) {
		if(currentChapter == -1) drawMenu(mouseX, mouseY);
		else drawPage(book.menuItems.get(currentChapter).pages.get(currentPage));
	}

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

	protected void drawGuiScroll() {
		drawGui();
		this.drawStretchingSquare(gui$sepX0, gui$sepY0, gui$sepX1, gui$sepY1, z$frame, atlas$gui$sepU0, atlas$gui$sepV0, atlas$gui$sepU1, atlas$gui$sepV1, true);
		this.drawStretchingSquare(gui$sepX0, gui$sepY1, gui$sepX1, gui$sepY2, z$frame, atlas$gui$sepU0, atlas$gui$sepV1, atlas$gui$sepU1, atlas$gui$sepV2, true);
		this.drawStretchingSquare(gui$sepX0, gui$sepY2, gui$sepX1, gui$sepY3, z$frame, atlas$gui$sepU0, atlas$gui$sepV2, atlas$gui$sepU1, atlas$gui$sepV3, true);
	}

	protected void drawTransparentOverlay() {
		GlStateManager.pushMatrix();
		GlStateManager.disableAlpha();
		this.drawStretchingSquare(0, 0, this.width, gui$y0 + gui$shadowThickness, z$frame, 0, 0, 1, 1, false);
		this.drawStretchingSquare(0, gui$y3 - gui$shadowThickness, this.width, this.height, z$frame, 0, 0, 1, 1, false);
		this.drawStretchingSquare(0, gui$y0 + gui$shadowThickness, gui$x0 + gui$shadowThickness, gui$y3 - gui$shadowThickness, z$frame, 0, 0, 1, 1, false);
		this.drawStretchingSquare(gui$x3 - gui$shadowThickness, gui$y0 + gui$shadowThickness, this.width, gui$y3 - gui$shadowThickness, z$frame, 0, 0, 1, 1, false);
		GlStateManager.enableAlpha();
		GlStateManager.popMatrix();
	}

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
		area$x1 = gui$x2 - gui$shadowThickness - gui$separatorThickness;
		area$y1 = gui$y2;
		area$acrossX = area$x1 - area$x0;
		area$acrossY = area$y1 - area$y0;
		// End usable area
	}

	protected void getDataFromNBT() {
		// Getting information from item NBT
		ItemStack bookItemStack = mc.player.getHeldItem(hand);
		if (bookItemStack.hasTagCompound()) {
			NBTTagCompound nbtTagCompound = bookItemStack.getTagCompound();
			this.currentPage = nbtTagCompound.getInteger("page");
			this.currentChapter = nbtTagCompound.getInteger("chapter");
			this.currentSliderProgress = nbtTagCompound.getFloat("sliderProgress");
		} else {
			this.currentPage = 1;
			this.currentChapter = -1;
			this.currentSliderProgress = 0.0F;
		}
	}

	/* ********************************* trying new stuff ************************************************************************* */

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawTransparentOverlay();
		this.drawMenu(mouseX, mouseY);
		slider.drawButton(mc, mouseX, mouseY, partialTicks);
		home.drawTexturedButton(mc, GUI_BOOK_TEXTURE, 40, 64, z$titleButtons);
		home.drawTexturedButtonForegroundLayer(mc, mouseX, mouseY, GUI_BOOK_TEXTURE, 128, 0, 16);
		this.drawGuiScroll();
	}

	@Override
	public void initGui() {
		this.calculateConstraints();
		this.getDataFromNBT();
		this.slider = this.addButton(new GuiGuideBookSlider(0, gui$sliderX, gui$sliderY0, gui$sliderY1, z$titleButtons, 0.0F, gui$sliderWidth, gui$sliderHeight));
		this.home = this.addButton(new GuiGuideBookTexturedButton(1, gui$x3 - gui$tagWidth, gui$y0 - gui$tagHeight, gui$tagWidth, gui$fullTagHeight));
		this.book.loadBook();
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void onGuiClosed() {
		ItemGuideBook.setCurrentPage(Minecraft.getMinecraft().player.getHeldItem(hand), currentPage, currentChapter, currentSliderProgress, hand);
		super.onGuiClosed();
	}


	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if(button.id == this.home.id){
			this.currentPage = 0;
			this.currentPageCount = 0;
			this.currentChapter = -1;
			this.currentSliderProgress = 0.0F;
		}
	}

	// TODO work better on this!!
	protected void drawMenu(int mouseX, int mouseY){
		int area$totalY = 0;
		int area$totalX = 0;
		int area$tileCount = 0;
		boolean hadTile = false;
		for(int index = 0; index < book.menuItems.size(); index++){
			switch (book.menuItemsType.get(index)){
				case "text":
					if(hadTile) {
						area$totalY += 20;
						hadTile = false;
					}
					book.menuItems.get(index).drawMenuItem(gui$x1, gui$y1 + area$totalY, gui$x2 - gui$x1, 25, true);
					area$totalY += 50;
					area$tileCount = 0;
					break;
				case "item":
					book.menuItems.get(index).drawMenuItemFrame(gui$x1 + 45 * area$tileCount, gui$y1 + area$totalY, 40, 40, mc, mouseX, mouseY);
					area$tileCount++;
					break;
			}
		}
	}

	protected void drawPage(Page page){
		// Do what needs doing when drawing page
	}

	protected void selectChapter(MenuItem item){
		// Do what needs doing when change chapter
	}

	/*				this.renderItem.renderItemAndEffectIntoGUI(this.icon, (int) ((this.x + 5) * 1 / itemScale), (int) ((this.y + 5) * 1 / itemScale));
				this.renderItem.renderItemOverlayIntoGUI(this.fontRenderer, this.icon, (int) ((this.x + 5) * 1 / itemScale), (int) ((this.y + 5) * 1 / itemScale), this.title);
*/

	/*
	 * Enum of what can be being drawn in the gui
	 * - MENU - Group of MenuItems tiles
	 * - LIST - List of ListedPages
	 * - PAGE - Page, could be any kind of page
	 *//*
	enum EnumDrawState {
		MENU,
		LIST,
		PAGE
	}*/

	/* *********************************************** draw functions with a twist *********************************************** */

	/*
	 * Draws a square based on two vertices with (stretching) texture also determined by two vertices: TopLeft & BottomRight
	 * The vertex(xy) and vertex1(xy) translate to vertex(uv) and vertex1(uv) in the texture atlas.
	 * The Y increases from the top to the bottom.
	 */

	public static void drawStretchingSquare(int x0, int y0, int x1, int y1, int z, double u0, double v0, double u1, double v1) {
		drawStretchingSquare(x0, y0, x1, y1, z, u0, v0, u1, v1, false);
	}

	public static void drawStretchingSquare(int x0, int y0, int x1, int y1, int z, double u0, double v0, double u1, double v1, boolean blend) {
		u0 *= atlas$widthScale;
		v0 *= atlas$heightScale;
		u1 *= atlas$widthScale;
		v1 *= atlas$heightScale;
		// Four vertices of square following order: TopLeft, TopRight, BottomLeft, BottomRight
		if (blend) GlStateManager.enableBlend();
		if (blend) GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos(x0, y1, z).tex(u0, v1).endVertex();
		bufferbuilder.pos(x1, y1, z).tex(u1, v1).endVertex();
		bufferbuilder.pos(x1, y0, z).tex(u1, v0).endVertex();
		bufferbuilder.pos(x0, y0, z).tex(u0, v0).endVertex();
		tessellator.draw();
		if (blend) GlStateManager.disableBlend();
	}

	/*
	 * Draws a square based on two vertices with repeating texture also determined by two vertices: TopLeft & BottomRight
	 * If the texture size is smaller than the draw size the texture will be repeated until it fills all the area to be drawn.
	 * The Y increases from the top to the bottom.
	 */

	public static void drawRepeatingSquare(int x0, int y0, int x1, int y1, int z, double u0, double v0, double u1, double v1, boolean blend) {
		if (x1 - x0 <= u1 - u0 && y1 - y0 <= v1 - v0) {
			drawStretchingSquare(x0, y0, x1, y1, z, u0, v0, u1, v1, blend);
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
					drawStretchingSquare(x0 + (j * u), y0 + (i * v), x0 + (j * u) + remainderX, y0 + (i * v) + remainderY, z, u0, v0, u0 + remainderX, v0 + remainderY, blend);
				} else if (j == timesX) {
					drawStretchingSquare(x0 + (j * u), y0 + (i * v), x0 + (j * u) + remainderX, y0 + ((i + 1) * v), z, u0, v0, u0 + remainderX, v1, blend);
				} else if (i == timesY) {
					drawStretchingSquare(x0 + (j * u), y0 + (i * v), x0 + ((j + 1) * u), y0 + (i * v) + remainderY, z, u0, v0, u1, v0 + remainderY, blend);
				} else {
					drawStretchingSquare(x0 + (j * u), y0 + (i * v), x0 + ((j + 1) * u), y0 + ((i + 1) * v), z, u0, v0, u1, v1, blend);
				}
			}
		}
	}

}
