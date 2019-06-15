package logisticspipes.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import org.lwjgl.opengl.GL11;

import logisticspipes.LPConstants;
import logisticspipes.items.ItemGuideBook;
import logisticspipes.utils.GuideBookContents;

public class GuiGuideBook extends GuiScreen {

	private static final ResourceLocation GUI_BOOK_TEXTURE = new ResourceLocation(LPConstants.LP_MOD_ID, "textures/gui/guide_book.png");

	private GuiButton nextPageBtn, prevPageBtn, slider;

	private EnumHand hand;

	// Slider vars
	private int dragSrcY;
	private float sliderProgress;
	private int sliderTopY;
	private int sliderBotY;

	// Book vars
	private GuideBookContents.Page page;
	private int maxLength;
	private int textX, textY;
	private int maxLines;

	// Drawing Constants
	private final float textureSizeX = 256.0F;
	private final float textureSizeY = 256.0F;
	private final int tileSize = 32;
	private final int borderSize = tileSize / 2;
	private int acrossX, acrossY, leftX, topY;

	private final GuideBookContents gbc;

	private boolean needsInit = true;

	public GuiGuideBook(EnumHand hand, GuideBookContents gbc) {
		super();
		this.hand = hand;
		this.gbc = gbc;
		this.page = gbc.getPage(0);
	}

	@Override
	public void initGui() {
		// Defining drawing constraints
		final int sliderWidth = 20;
		final int sliderHeight = 20;

		int guiWidthX = (int) (this.width * 0.75);
		int guiWidthY = (int) (this.height * 0.75);

		acrossX = guiWidthX - 2 * tileSize;
		acrossY = guiWidthY - 2 * tileSize;
		leftX = this.width / 2 - guiWidthX / 2;
		topY = this.height / 2 - guiWidthY / 2;
		int rightX = this.width / 2 + guiWidthX / 2;
		int bottomX = this.height / 2 + guiWidthY / 2;

		// Setting slider constraints
		sliderTopY = topY + borderSize;
		sliderBotY = bottomX - borderSize - sliderHeight;

		// Setting text constraints
		int textInset = borderSize + 8;
		textX = leftX + textInset;
		textY = topY + textInset;
		maxLength = guiWidthX - sliderWidth - textInset * 2;
		maxLines = (acrossY + borderSize) / 10;

		// Setting up buttons
		this.buttonList.clear();
		this.nextPageBtn = this.addButton(new GuiButton(0, this.width / 2 + 100, this.height - 30, 20, 20, ">"));
		this.prevPageBtn = this.addButton(new GuiButton(1, this.width / 2 - 120, this.height - 30, 20, 20, "<"));
		this.slider = this.addButton(new GuiButton(2, rightX - borderSize - sliderHeight, 0, sliderWidth, sliderHeight, "|||"));
		updateSliderPosition();

		// Getting information from item NBT
		if (needsInit) {
			ItemStack book;
			if (hand == EnumHand.MAIN_HAND)
				book = mc.player.getHeldItemMainhand();
			else
				book = mc.player.getHeldItemOffhand();
			if (book.hasTagCompound()) {
				NBTTagCompound nbtTagCompound = book.getTagCompound();
				GuideBookContents.Page p = gbc.getPage(nbtTagCompound.getInteger("page"));
				if (p != null) this.page = p;
				this.sliderProgress = nbtTagCompound.getFloat("sliderProgress");
			} else {
				setPage(0);
			}
			needsInit = false;
		}
		updateButtonStates();
	}

	private void drawPageContents() {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		// Calculating translation amount and setting slider properties

		final List<String> text = splitLines(page.getText(), fontRenderer, maxLength);

		if (text.size() <= maxLines) {
			slider.enabled = false;
			sliderProgress = 0;
			updateSliderPosition();
		} else {
			slider.enabled = true;
		}

		// Drawing Translated Text
		float translateY = sliderProgress * 10 * (text.size() - maxLines);
		for (int i = 0; i < text.size(); i++) {
			int drawY = textY + (10 * i) - (int) translateY;
			if (drawY > textY - 20 && drawY < (this.height * 7 / 8) - 10) {
				drawString(this.fontRenderer, text.get(i), textX, drawY, 0xFFFFFF);
			}
		}

		GlStateManager.popMatrix();
		/** Top & Bottom */
		drawScaledCustomSizeModalRect(this.width * 1 / 8 + tileSize, this.height * 1 / 8, 24, 0, 8, tileSize, this.width * 6 / 8 - 2 * tileSize, tileSize, 256.0F, 256.0F);
		drawScaledCustomSizeModalRect(this.width * 1 / 8 + tileSize, this.height * 7 / 8 - tileSize, 24, tileSize, 8, tileSize, this.width * 6 / 8 - 2 * tileSize, tileSize, 256.0F, 256.0F);
		/**Left & Right*/
		drawScaledCustomSizeModalRect(this.width * 1 / 8, this.height * 1 / 8 + tileSize, 0, 24, tileSize, 8, tileSize, this.height * 6 / 8 - 2 * tileSize, 256.0F, 256.0F);
		drawScaledCustomSizeModalRect(this.width * 7 / 8 - tileSize, this.height * 1 / 8 + tileSize, tileSize, 24, tileSize, 8, tileSize, this.height * 6 / 8 - 2 * tileSize, 256.0F, 256.0F);
		/**Background*/
		drawScaledCustomSizeModalRect(this.width * 1 / 8 + tileSize, this.height * 1 / 8 + tileSize, 24, 24, 8, 8, this.width * 6 / 8 - 2 * tileSize, this.height * 6 / 8 - 2 * tileSize, 256.0F, 256.0F);
	}

	private void drawPageBorder() {
		int rightX = leftX + tileSize + acrossX;
		int bottomY = topY + tileSize + acrossY;

		GlStateManager.enableBlend();
		// Drawing Foreground Border
		mc.renderEngine.bindTexture(GUI_BOOK_TEXTURE);
		// TopLeft, TopRight, BottomLeft & BottomRight
		drawScaledCustomSizeModalRect(leftX, topY, 0, 0, tileSize, tileSize, tileSize, tileSize, textureSizeX, textureSizeY);
		drawScaledCustomSizeModalRect(rightX, topY, tileSize, 0, tileSize, tileSize, tileSize, tileSize, textureSizeX, textureSizeY);
		drawScaledCustomSizeModalRect(leftX, bottomY, 0, tileSize, tileSize, tileSize, tileSize, tileSize, textureSizeX, textureSizeY);
		drawScaledCustomSizeModalRect(rightX, bottomY, tileSize, tileSize, tileSize, tileSize, tileSize, tileSize, textureSizeX, textureSizeY);
		// Top, Bottom, Left & Right
		drawScaledCustomSizeModalRect(leftX + tileSize, topY, 24, 0, 8, tileSize, acrossX, tileSize, textureSizeX, textureSizeY);
		drawScaledCustomSizeModalRect(leftX + tileSize, bottomY, 24, tileSize, 8, tileSize, acrossX, tileSize, textureSizeX, textureSizeY);
		drawScaledCustomSizeModalRect(leftX, topY + tileSize, 0, 24, tileSize, 8, tileSize, acrossY, textureSizeX, textureSizeY);
		drawScaledCustomSizeModalRect(rightX, topY + tileSize, tileSize, 24, tileSize, 8, tileSize, acrossY, textureSizeX, textureSizeY);
	}

	private void drawPageBackground() {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		mc.renderEngine.bindTexture(GUI_BOOK_TEXTURE);
		drawScaledCustomSizeModalRect(leftX + borderSize, topY + borderSize, tileSize * 2, 0, tileSize / 2, tileSize / 2, acrossX + tileSize, acrossY + tileSize, textureSizeX, textureSizeY);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawPageBackground();
		drawPageContents();
		drawPageBorder();
		drawCenteredString(this.fontRenderer, gbc.getTitle(), this.width / 2, this.height / 8 + 4, 0xFFFFFF);
		drawCenteredString(this.fontRenderer, String.format("Page %d out of %d", page.getIndex() + 1, gbc.getPages()), this.width / 2, this.height - 25, 0xFFFFFF);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		switch (button.id) {
			case 0:
				if ((page.getIndex() + 1) < gbc.getPages()) {
					setPage(page.getIndex() + 1);
				}
				break;
			case 1:
				if ((page.getIndex() - 1) >= 0) {
					setPage(page.getIndex() - 1);
				}
				break;
		}
	}

	private void updateButtonStates() {
		this.nextPageBtn.enabled = page.getIndex() < gbc.getPages() - 1;
		this.prevPageBtn.enabled = page.getIndex() > 0;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		dragSrcY = mouseY;
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		if (slider.enabled) {
			int dy = mouseY - dragSrcY;
			slider.y = MathHelper.clamp(slider.y + dy, sliderTopY, sliderBotY);
			sliderProgress = ((float) slider.y - sliderTopY) / (sliderBotY - sliderTopY);
			dragSrcY = mouseY;
		}
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}

	private void setPage(int index) {
		page = gbc.getPage(index);
		setSliderProgress(0);
		updateButtonStates();
	}

	private void setSliderProgress(float p) {
		sliderProgress = p;
		updateSliderPosition();
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	public void onGuiClosed() {
		ItemGuideBook.setCurrentPage(Minecraft.getMinecraft().player.getHeldItem(hand), page.getIndex(), 0, sliderProgress, hand);
		super.onGuiClosed();
	}

	/*
	 * Make the slider button follow the mouse if the click was performed over it as well as
	 * calculate sliderProgress based on the slider position between it's max and min Y.
	 */
	private void updateSliderPosition() {
		slider.y = (int) (sliderTopY + (sliderBotY - sliderTopY) * sliderProgress);
	}

	private static ArrayList<String> splitLines(String text, FontRenderer fontRenderer, int maxLength) {
		if (text.charAt(text.length() - 1) != '\n') text += '\n';
		ArrayList<String> lines = new ArrayList<>();
		StringBuilder currentLine = new StringBuilder();
		int lastSplitPoint = 0;
		int curLength = 0;
		for (char c : text.toCharArray()) {
			if (c == '\n') {
				lines.add(currentLine.toString());
				currentLine.delete(0, currentLine.length());
				lastSplitPoint = 0;
				curLength = fontRenderer.getStringWidth(currentLine.toString());
			} else if (Character.isWhitespace(c)) {
				currentLine.append(c);
				curLength += fontRenderer.getCharWidth(c);
				lastSplitPoint = currentLine.length();
			} else {
				if ((currentLine.length() > 1 && curLength + fontRenderer.getCharWidth(c) > maxLength)) {
					if (lastSplitPoint == 0) lastSplitPoint = currentLine.length();
					lines.add(currentLine.substring(0, lastSplitPoint));
					currentLine.delete(0, lastSplitPoint);
					lastSplitPoint = 0;
					curLength = fontRenderer.getStringWidth(currentLine.toString());
				}
				currentLine.append(c);
				curLength += fontRenderer.getCharWidth(c);
			}
		}
		return lines;
	}

}
