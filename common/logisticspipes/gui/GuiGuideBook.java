package logisticspipes.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import logisticspipes.LPConstants;
import logisticspipes.utils.string.StringUtils;

public class GuiGuideBook extends GuiScreen {

	private static final ResourceLocation GUI_BOOK_TEXTURE = new ResourceLocation(LPConstants.LP_MOD_ID, "textures/gui/guide_book.png");

	private GuiButton nextPageBtn, prevPageBtn, slider;

	private int mouseX, mouseY;
	private final ItemStack book;

	//Slider vars
	private boolean dragging = false;
	private boolean draggable = false;
	private float sliderProgress;
	private int sliderTopY;
	private int sliderBotY;

	//Book vars
	private int page;
	private int pageMax = 10;
	private int maxLength;
	private int textX, textY;
	private int maxLines;

	public GuiGuideBook(ItemStack book) {
		super();
		this.book = book;
		if (book.hasTagCompound()) {
			NBTTagCompound nbtTagCompound = book.getTagCompound();
			this.page = nbtTagCompound.getInteger("page");
			this.sliderProgress = nbtTagCompound.getFloat("sliderProgress");
			if (this.page == 0) {
				this.page = 1;
				this.sliderProgress = 0.0F;
				nbtTagCompound.setInteger("page", 1);
				nbtTagCompound.setFloat("sliderProgress", sliderProgress);
			}
		} else {
			NBTTagCompound nbtTagCompound = new NBTTagCompound();
			this.page = 1;
			this.sliderProgress = 0.0F;
			nbtTagCompound.setInteger("page", 1);
			nbtTagCompound.setFloat("sliderProgress", sliderProgress);
			book.setTagCompound(nbtTagCompound);
		}
	}

	/**
	 * Draw a background container sized based on window width and height.
	 * TODO use stretch on a single draw instead of successive draws.
	 */

	protected void drawGuiBackgroundLayer() {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		/**Background*/
		mc.renderEngine.bindTexture(GUI_BOOK_TEXTURE);
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, -2.0F);
		this.drawDefaultBackground();
		GlStateManager.popMatrix();
		GlStateManager.pushMatrix();
		/**Drawing Constants*/
		float textureSide$x = 256.0F;
		float textureSide$y = 256.0F;
		int tileSize = 32;
		int borderSize = tileSize / 2;
		int acrossX = this.width * 6 / 8 - 2 * tileSize;
		int acrossY = this.height * 6 / 8 - 2 * tileSize;
		int leftX = this.width * 1 / 8;
		int topY = this.height * 1 / 8;
		int rightX = this.width * 7 / 8 - tileSize;
		int bottomY = (topY + tileSize) + (acrossY);
		/**TopLeft*/
		drawScaledCustomSizeModalRect(leftX, topY, 0, 0, tileSize, tileSize, tileSize, tileSize, textureSide$x, textureSide$y);
		/**TopRight*/
		drawScaledCustomSizeModalRect(rightX, topY, tileSize, 0, tileSize, tileSize, tileSize, tileSize, textureSide$x, textureSide$y);
		/**BottomLeft*/
		drawScaledCustomSizeModalRect(leftX, bottomY, 0, tileSize, tileSize, tileSize, tileSize, tileSize, textureSide$x, textureSide$y);
		/**BottomRight*/
		drawScaledCustomSizeModalRect(rightX, bottomY, tileSize, tileSize, tileSize, tileSize, tileSize, tileSize, textureSide$x, textureSide$y);
		/** Top & Bottom */
		drawScaledCustomSizeModalRect(leftX + tileSize, topY, 24, 0, 8, tileSize, acrossX, tileSize, textureSide$x, textureSide$y);
		drawScaledCustomSizeModalRect(leftX + tileSize, bottomY, 24, tileSize, 8, tileSize, acrossX, tileSize, textureSide$x, textureSide$y);
		/**Left & Right*/
		drawScaledCustomSizeModalRect(leftX, topY + tileSize, 0, 24, tileSize, 8, tileSize, acrossY, textureSide$x, textureSide$y);
		drawScaledCustomSizeModalRect(rightX, topY + tileSize, tileSize, 24, tileSize, 8, tileSize, acrossY, textureSide$x, textureSide$y);
		/**Background*/
		drawScaledCustomSizeModalRect(leftX + tileSize, topY + tileSize, 24, 24, 8, 8, acrossX, acrossY, textureSide$x, textureSide$y);
		/**Setting slider constraints*/
		sliderTopY = topY + borderSize;
		sliderBotY = bottomY + borderSize - slider.height;
		slider.x = rightX + borderSize - slider.height;
		/**Setting text constraints*/
		maxLength = acrossX + 3 / 2 * borderSize - slider.width;
		textX = leftX + 24;
		textY = topY + 24;
		maxLines = (acrossY - borderSize) / 10;
		GlStateManager.popMatrix();
	}

	/**
	 * Draw a foreground layer containing all the titles and all the text.
	 * TODO maybe work on the newline algorithm.
	 */

	protected void drawGuiForeGroundLayer() {
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, 10.0F);
		drawCenteredString(this.fontRenderer, StringUtils.translate("book.quickstart.title").trim(), this.width / 2, this.height * 1 / 8 + 4, 0xFFFFFF);
		drawCenteredString(this.fontRenderer, "Page " + page + " out of " + pageMax, this.width / 2, this.height - 25, 0xFFFFFF);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		String delimiter = "\\\\n";
		String currentPage = StringUtils.translate("book.quickstart." + page);
		String[] currentPageLines = currentPage.split(delimiter);
		String line;
		int line_number = 0;
		int length = 0;
		for (String sentence : currentPageLines) {
			do {
				line = fontRenderer.trimStringToWidth(sentence, maxLength);
				length = line.length();
				if (fontRenderer.getStringWidth(sentence) > maxLength) {
					line = fontRenderer.trimStringToWidth(sentence, maxLength);
					if (line.charAt(line.length() - 1) != ' ') {
						if (line.charAt(line.length() - 2) != ' ') {
							line = line.substring(0, line.length() - 1) + '-';
							length -= 1;
						} else {
							line = line.substring(0, line.length() - 1) + ' ';
							length -= 1;
						}
					}
					if (line.charAt(0) == ' ') line = line.substring(1);
					sentence = sentence.substring(length);
				} else {
					line = sentence;
					sentence = "";
				}
				drawString(this.fontRenderer, line, textX, textY + (10 * line_number++), 0xFFFFFF);
			} while (sentence.length() != 0);
			line_number++;
		}
		if (line_number < maxLines) {
			this.draggable = false;
			slider.enabled = false;
		}
		GlStateManager.popMatrix();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawGuiBackgroundLayer();
		this.drawGuiForeGroundLayer();
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void initGui() {
		this.buttonList.clear();
		this.nextPageBtn = this.addButton(new GuiButton(0, this.width / 2 + 100, this.height - 30, 20, 20, ">"));
		this.prevPageBtn = this.addButton(new GuiButton(1, this.width / 2 - 120, this.height - 30, 20, 20, "<"));
		this.slider = this.addButton(new GuiButton(2, 0, this.height * 1 / 8 + 16, 20, 20, "|||"));
		this.drawGuiBackgroundLayer();
		updateSlider();
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0) {
			if (page < pageMax) page++;
			sliderProgress = 0.0F;
			updateSlider();
		}
		if (button.id == 1) {
			if (page > 1) page--;
			sliderProgress = 0.0F;
			updateSlider();
		}
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
		NBTTagCompound nbtTagCompound = book.getTagCompound();
		nbtTagCompound.setInteger("page", page);
		nbtTagCompound.setFloat("sliderProgress", sliderProgress);
		super.onGuiClosed();
	}
}
