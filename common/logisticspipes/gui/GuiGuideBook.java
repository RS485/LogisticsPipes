package logisticspipes.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLSync;
import scala.actors.threadpool.Arrays;

import logisticspipes.LPConstants;
import logisticspipes.items.ItemGuideBook;
import logisticspipes.utils.string.StringUtils;

public class GuiGuideBook extends GuiScreen {

	/**
	 * Z Levels:
	 * Title and Buttons: 15
	 * Background frame : 10
	 * Text             : 5
	 * Background       : 0
	 * Background tint  : -5
	 */

	private static final ResourceLocation GUI_BOOK_TEXTURE = new ResourceLocation(LPConstants.LP_MOD_ID, "textures/gui/guide_book.png");

	private GuiButton nextPageBtn, prevPageBtn, slider;
	private int itemSlot;

	private int mouseX, mouseY;
	private ItemStack book;

	//Slider vars
	private boolean dragging = false;
	private boolean draggable = false;
	private float sliderProgress;
	private int sliderTopY;
	private int sliderBotY;
	private final int sliderWidth = 20, sliderHeight = 20;

	//Book vars
	private int page;
	private int pageMax = 11;
	private int maxLength;
	private int textX, textY;
	private int maxLines;

	//Text vars
	List<String> text = new ArrayList<>();

	//Drawing Constants
	private final float textureSide$x = 256.0F;
	private final float textureSide$y = 256.0F;
	private final int tileSize = 32;
	private final int borderSize = tileSize / 2;
	private int acrossX, acrossY, leftX, topY, rightX, bottomY;

	public GuiGuideBook() {
		super();
	}

	protected void splitLines() {
		text.clear();
		String delimiter = "\\\\n";
		String translatedText = StringUtils.translate("book.quickstart." + page);
		String[] currentPageLines = translatedText.split(delimiter);
		ArrayList<String> splitSentences = new ArrayList<>(Arrays.asList(currentPageLines));
		for (int i = splitSentences.size() - 1; i > 0; i--) {
			splitSentences.add(i, "           ");
		}
		for (String sentence : splitSentences) {
			do {
				String line;
				if (fontRenderer.getStringWidth(sentence) > maxLength) {
					line = fontRenderer.trimStringToWidth(sentence, maxLength);
					int length = line.length();
					if (sentence.length() > line.length()) {
						if (sentence.charAt(length) != ' ') {
							while (line.charAt(length - 1) != ' ') {
								length--;
							}
							line = line.substring(0, length-1);
							sentence = sentence.substring(length);
						}else{
							line = line.substring(0, length-1);
							sentence = sentence.substring(length);
						}
					} else {
						line = sentence;
						sentence = "";
					}
				}else{
					line = sentence;
					sentence = "";
				}
				if(line.charAt(0) == ' ') line = line.substring(1);
				text.add(line);
			} while (sentence.length() != 0);
		}
	}

	/**
	 * Draw GUI, text, and borders
	 */
	protected void drawTextGui() {
		GlStateManager.pushMatrix();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		/** Drawing Title and Page count Layer 15*/
		GlStateManager.translate(0.0F, 0.0F, 15.0F);
		drawCenteredString(this.fontRenderer, StringUtils.translate("book.quickstart.title").trim(), this.width / 2, this.height * 1 / 8 + 4, 0xFFFFFF);
		drawCenteredString(this.fontRenderer, "Page " + page + " out of " + pageMax, this.width / 2, this.height - 25, 0xFFFFFF);
		/** Calculating translation amount and setting slider properties */
		float translateY;
		if (text.size() <= maxLines) {
			translateY = 0.0F;
			this.draggable = false;
			slider.enabled = false;
		} else {
			translateY = sliderProgress * 10 * (text.size() - maxLines);
			this.draggable = true;
			slider.enabled = true;
		}
		/** Drawing Translated Text */
		GlStateManager.translate(0.0F, 0.0F, -10.0F);
		for (int i = 0; i < text.size(); i++) {
			int drawY = textY + (10 * i) - (int) translateY;
			if (drawY > textY - 20 && drawY < (this.height * 7 / 8) - 10) {
				drawString(this.fontRenderer, text.get(i), textX, drawY, 0xFFFFFF);
			}
		}
		GlStateManager.popMatrix();
	}

	protected void drawForegroundLayer() {
		GlStateManager.pushMatrix();
		/** Drawing Foreground Border */
		GlStateManager.translate(0.0F, 0.0F, 5.0F);
		mc.renderEngine.bindTexture(GUI_BOOK_TEXTURE);
		//TopLeft, TopRight, BottomLeft & BottomRight
		drawScaledCustomSizeModalRect(leftX, topY, 0, 0, tileSize, tileSize, tileSize, tileSize, textureSide$x, textureSide$y);
		drawScaledCustomSizeModalRect(rightX, topY, tileSize, 0, tileSize, tileSize, tileSize, tileSize, textureSide$x, textureSide$y);
		drawScaledCustomSizeModalRect(leftX, bottomY, 0, tileSize, tileSize, tileSize, tileSize, tileSize, textureSide$x, textureSide$y);
		drawScaledCustomSizeModalRect(rightX, bottomY, tileSize, tileSize, tileSize, tileSize, tileSize, tileSize, textureSide$x, textureSide$y);
		// Top, Bottom, Left & Right
		drawScaledCustomSizeModalRect(leftX + tileSize, topY, 24, 0, 8, tileSize, acrossX, tileSize, textureSide$x, textureSide$y);
		drawScaledCustomSizeModalRect(leftX + tileSize, bottomY, 24, tileSize, 8, tileSize, acrossX, tileSize, textureSide$x, textureSide$y);
		drawScaledCustomSizeModalRect(leftX, topY + tileSize, 0, 24, tileSize, 8, tileSize, acrossY, textureSide$x, textureSide$y);
		drawScaledCustomSizeModalRect(rightX, topY + tileSize, tileSize, 24, tileSize, 8, tileSize, acrossY, textureSide$x, textureSide$y);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

	protected void drawBackgroundLayer() {
		GlStateManager.pushMatrix();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		/**Background Layer -5*/
		GlStateManager.translate(0.0F, 0.0F, -5.0F);
		this.drawDefaultBackground();
		/**GUI Background Layer 0*/
		GlStateManager.translate(0.0F, 0.0F, 5.0F);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		mc.renderEngine.bindTexture(GUI_BOOK_TEXTURE);
		drawScaledCustomSizeModalRect(leftX + borderSize, topY + borderSize, tileSize * 2, 0, tileSize / 2, tileSize / 2, acrossX + tileSize, acrossY + tileSize, textureSide$x, textureSide$y);
		GlStateManager.popMatrix();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawBackgroundLayer();
		this.drawTextGui();
		this.drawForegroundLayer();
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, 5.0F);
		super.drawScreen(mouseX, mouseY, partialTicks);
		GlStateManager.popMatrix();
	}

	@Override
	public void initGui() {
		/**Defining drawing constrains*/
		acrossX = this.width * 6 / 8 - 2 * tileSize;
		acrossY = this.height * 6 / 8 - 2 * tileSize;
		leftX = this.width * 1 / 8;
		topY = this.height * 1 / 8;
		rightX = this.width * 7 / 8 - tileSize;
		bottomY = (topY + tileSize) + (acrossY);
		/**Setting slider constraints*/
		sliderTopY = topY + borderSize;
		sliderBotY = bottomY + borderSize - sliderHeight;
		/**Setting text constraints*/
		maxLength = acrossX + 3 / 2 * borderSize - sliderWidth;
		textX = leftX + 24;
		textY = topY + 24;
		maxLines = (acrossY + borderSize) / 10;
		/**Getting information from item NBT*/
		book = mc.player.inventory.getStackInSlot(itemSlot);
		if (book.hasTagCompound()) {
			System.out.println("Book has nbt");
			NBTTagCompound nbtTagCompound = book.getTagCompound();
			this.page = nbtTagCompound.getInteger("page");
			this.sliderProgress = nbtTagCompound.getFloat("sliderProgress");
		} else {
			System.out.println("Book doesn't have NBT");
			this.page = 1;
			this.sliderProgress = 0.0F;
		}
		/**Setting up buttons*/
		this.buttonList.clear();
		this.nextPageBtn = this.addButton(new GuiButton(0, this.width / 2 + 100, this.height - 30, 20, 20, ">"));
		this.prevPageBtn = this.addButton(new GuiButton(1, this.width / 2 - 120, this.height - 30, 20, 20, "<"));
		this.slider = this.addButton(new GuiButton(2, rightX + borderSize - sliderHeight, sliderTopY + (int) ((sliderBotY - sliderTopY) * sliderProgress), sliderWidth, sliderHeight, "|||"));
		/**Splitting current page*/
		splitLines();
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0) {
			if (page < pageMax) page++;
			sliderProgress = 0.0F;
			updateSlider();
			splitLines();
		}
		if (button.id == 1) {
			if (page > 1) page--;
			sliderProgress = 0.0F;
			updateSlider();
			splitLines();
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (slider.isMouseOver()) this.dragging = true;
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		updateSlider();
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		this.dragging = false;
		super.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	public void onGuiClosed() {
		NBTTagCompound nbtTagCompound = new NBTTagCompound();
		nbtTagCompound.setInteger("page", page);
		nbtTagCompound.setFloat("sliderProgress", sliderProgress);
		ItemGuideBook.setCurrentPage(itemSlot, mc.player, nbtTagCompound);
		super.onGuiClosed();
	}

	@Override
	public void onResize(Minecraft mcIn, int w, int h) {
		/**Updating drawing constrains*/
		acrossX = w * 6 / 8 - 2 * tileSize;
		acrossY = h * 6 / 8 - 2 * tileSize;
		leftX = w * 1 / 8;
		topY = h * 1 / 8;
		rightX = w * 7 / 8 - tileSize;
		bottomY = (topY + tileSize) + (acrossY);
		/**Setting slider constraints*/
		sliderTopY = topY + borderSize;
		sliderBotY = bottomY + borderSize - slider.height;
		slider.x = rightX + borderSize - slider.height;
		/**Setting text constraints*/
		maxLength = acrossX + 3 / 2 * borderSize - slider.width;
		textX = leftX + 24;
		textY = topY + 24;
		maxLines = (acrossY + borderSize) / 10;
		/**Updating text splitting*/
		splitLines();
		super.onResize(mc, w, h);
	}

	protected void updateSlider() {
		if (dragging && draggable) {
			if (mouseY < sliderTopY + slider.height / 2) {
				slider.y = sliderTopY;
				sliderProgress = (slider.y - sliderTopY) / (float) (sliderBotY - sliderTopY);
			} else if (mouseY > sliderBotY + slider.height / 2) {
				slider.y = sliderBotY;
				sliderProgress = (slider.y - sliderTopY) / (float) (sliderBotY - sliderTopY);
			} else {
				slider.y = mouseY - slider.height / 2;
				sliderProgress = (slider.y - sliderTopY) / (float) (sliderBotY - sliderTopY);
			}
			System.out.println(sliderProgress);
		} else {
			if (draggable == false) slider.enabled = false;
			slider.y = (int) (sliderTopY + (sliderBotY - sliderTopY) * sliderProgress);
		}
	}
}
