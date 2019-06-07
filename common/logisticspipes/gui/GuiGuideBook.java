package logisticspipes.gui;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import logisticspipes.LPConstants;
import logisticspipes.utils.string.StringUtils;

public class GuiGuideBook extends GuiScreen {

	private static final ResourceLocation GUI_BOOK_TEXTURE = new ResourceLocation(LPConstants.LP_MOD_ID, "textures/gui/guide_book.png");

	private GuiButton nextPageBtn, prevPageBtn, slider;
	private boolean dragging = false;
	private int mouseX, mouseY;

	//Slider vars
	private float sliderProgress = 0.0F;
	private int sliderTopY = 0;
	private int sliderBotY = 0;

	//Book vars
	private int page = 1;
	private int pageMax = 10;
	private int maxLength = 50;

	public GuiGuideBook() {
		super();
	}

	/**
	 * Draw a background container sized based on x, and y size. This is not 1:1 scale.
	 * TODO use stretch on a single draw instead of successive draws.
	 */

	protected void drawGuiBackgroundLayer(int x, int y) {
		/**Setting slider constraints*/
		sliderTopY = this.height * 1 / 8;
		sliderBotY = this.height * 7 / 8 - 21 - slider.height;
		slider.x = this.width * 7 / 8 - 17 - slider.width;
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		/**Background*/
		mc.renderEngine.bindTexture(GUI_BOOK_TEXTURE);
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, -2.0F);
		this.drawDefaultBackground();
		GlStateManager.popMatrix();
		GlStateManager.pushMatrix();
		int tileSize = 32;
		/**TopLeft*/
		drawTexturedModalRect(this.width * 1 / 8, this.height * 1 / 8, 0, 0, tileSize, tileSize);
		/**TopRight*/
		drawTexturedModalRect(this.width * 7 / 8 - tileSize, this.height * 1 / 8, tileSize, 0, tileSize, tileSize);
		/**BottomLeft*/
		drawTexturedModalRect(this.width * 1 / 8, this.height * 7 / 8 - tileSize, 0, tileSize, tileSize, tileSize);
		/**BottomRight*/
		drawTexturedModalRect(this.width * 7 / 8 - tileSize, (this.height * 1 / 8 + tileSize) + (this.height * 6 / 8 - 2 * tileSize), tileSize, tileSize, tileSize, tileSize);
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

	public void drawGuiForeGroundLayer() {

		/**Foreground stuff*/
		GlStateManager.pushMatrix();
		GlStateManager.translate(1.0F, 1.0F, 10.0F);
		drawCenteredString(this.fontRenderer, StringUtils.translate("book.quickstart.title").trim(), this.width / 2, this.height * 1 / 8 + 4, 0xFFFFFF);
		drawCenteredString(this.fontRenderer, "Page " + page + " out of " + pageMax, this.width / 2, this.height - 25, 0xFFFFFF);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		String delimiter = "\\\\n";
		String currentPage = StringUtils.translate("book.quickstart." + page);
		/** Could be less than 75, this is already thinking about the slider button.*/
		maxLength = this.width * 3 / 4 - 75;
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
				drawString(this.fontRenderer, line, this.width * 1 / 8 + 24, this.height * 1 / 8 + 24 + (10 * line_number++), 0xFFFFFF);
			} while (sentence.length() != 0);
			line_number++;
		}
		GlStateManager.popMatrix();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawGuiBackgroundLayer(this.width * 3 / 32, this.height * 3 / 32);
		this.drawGuiForeGroundLayer();
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void initGui() {
		this.buttonList.clear();
		this.nextPageBtn = this.addButton(new GuiButton(0, this.width / 2 + 100, this.height - 30, 20, 20, ">"));
		this.prevPageBtn = this.addButton(new GuiButton(1, this.width / 2 - 120, this.height - 30, 20, 20, "<"));
		this.slider = this.addButton(new GuiButton(2, 0, this.height * 1 / 8 + 20, 20, 20, "|||"));
		//defaultSlider();
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

	protected void defaultSlider() {
		slider.y = sliderTopY;
	}

	protected void updateSlider() {
		if (dragging && slider.y >= sliderTopY && slider.y <= sliderBotY) {
			if (mouseY < sliderTopY + slider.height / 2) {
				slider.y = sliderTopY;
				sliderProgress = (slider.y - sliderTopY) / (sliderBotY - sliderTopY);
			} else if (mouseY > sliderBotY - slider.height / 2) {
				slider.y = sliderBotY;
				sliderProgress = (slider.y - sliderTopY) / (sliderBotY - sliderTopY);
			} else {
				slider.y = mouseY - slider.height / 2;
				float top = (slider.y - sliderTopY);
				float bot = (sliderBotY - sliderTopY);
				sliderProgress = top / bot;
			}
			System.out.println(sliderProgress);
		} else {
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
}
