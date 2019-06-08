package logisticspipes.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.html.parser.Entity;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import buildcraft.builders.snapshot.NbtPath;
import com.sun.media.jfxmedia.events.PlayerEvent;
import ibxm.Player;
import li.cil.oc.api.driver.item.Inventory;
import org.lwjgl.opengl.GL11;

import logisticspipes.LPConstants;
import logisticspipes.items.ItemGuideBook;
import logisticspipes.utils.string.StringUtils;

public class GuiGuideBook extends GuiScreen {

	/**
	 * Z Levels:
	 * 	Title and Buttons: 15
	 * 	Background frame : 10
	 *  Text             : 5
	 *  Background       : 0
	 *  Background tint  : -5
	 * */

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

	//Book vars
	private int page;
	private int pageMax = 11;
	private int maxLength;
	private int textX, textY;
	private int maxLines;

	public GuiGuideBook() {
		super();
	}

	/**
	 * Draw a background container sized based on window width and height.
	 */
	protected void drawGuiBackgroundLayer() {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		/**Background*/
		mc.renderEngine.bindTexture(GUI_BOOK_TEXTURE);
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, -5.0F);
		//this.drawDefaultBackground();
		GlStateManager.popMatrix();
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
		/**TopLeft, TopRight, BottomLeft & BottomRight*/
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, 10.0F);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		drawScaledCustomSizeModalRect(leftX, topY, 0, 0, tileSize, tileSize, tileSize, tileSize, textureSide$x, textureSide$y);
		drawScaledCustomSizeModalRect(rightX, topY, tileSize, 0, tileSize, tileSize, tileSize, tileSize, textureSide$x, textureSide$y);
		drawScaledCustomSizeModalRect(leftX, bottomY, 0, tileSize, tileSize, tileSize, tileSize, tileSize, textureSide$x, textureSide$y);
		drawScaledCustomSizeModalRect(rightX, bottomY, tileSize, tileSize, tileSize, tileSize, tileSize, tileSize, textureSide$x, textureSide$y);
		/** Top, Bottom, Left & Right */
		drawScaledCustomSizeModalRect(leftX + tileSize, topY, 24, 0, 8, tileSize, acrossX, tileSize, textureSide$x, textureSide$y);
		drawScaledCustomSizeModalRect(leftX + tileSize, bottomY, 24, tileSize, 8, tileSize, acrossX, tileSize, textureSide$x, textureSide$y);
		drawScaledCustomSizeModalRect(leftX, topY + tileSize, 0, 24, tileSize, 8, tileSize, acrossY, textureSide$x, textureSide$y);
		drawScaledCustomSizeModalRect(rightX, topY + tileSize, tileSize, 24, tileSize, 8, tileSize, acrossY, textureSide$x, textureSide$y);
		/**Background*/
		//GlStateManager.popMatrix();
		//GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, -10.0F);
		drawScaledCustomSizeModalRect(leftX + borderSize, topY + borderSize, tileSize*2, 0, tileSize/2, tileSize/2, acrossX + tileSize, acrossY + tileSize, textureSide$x, textureSide$y);
		GlStateManager.popMatrix();
		/**Setting slider constraints*/
		sliderTopY = topY + borderSize;
		sliderBotY = bottomY + borderSize - slider.height;
		slider.x = rightX + borderSize - slider.height;
		/**Setting text constraints*/
		maxLength = acrossX + 3 / 2 * borderSize - slider.width;
		textX = leftX + 24;
		textY = topY + 24;
		maxLines = (acrossY + borderSize) / 10;
	}

	/**
	 * Draw a foreground layer containing all the titles and all the text.
	 * TODO maybe work on the newline algorithm.
	 */
	protected void drawGuiForeGroundLayer() {
		/**Drawing Title and Page count*/
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, 15.0F);
		drawCenteredString(this.fontRenderer, StringUtils.translate("book.quickstart.title").trim(), this.width / 2, this.height * 1 / 8 + 4, 0xFFFFFF);
		drawCenteredString(this.fontRenderer, "Page " + page + " out of " + pageMax, this.width / 2, this.height - 25, 0xFFFFFF);
		GlStateManager.popMatrix();
		/**
		 * Preparing text to be drawn
		 * TODO make this elsewhere because it doens't have to be calculated every time it is drawn. Needs to update on page or size change.
		 **/
		String delimiter = "\\\\n";
		String currentPage = StringUtils.translate("book.quickstart." + page);
		//Add empty string here
		String[] currentPageLines = currentPage.split(delimiter);
		int length = 0;
		List<String> text = new ArrayList<>();
		for (String sentence : currentPageLines) {
			do {
				String line;
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
				text.add(line);
			} while (sentence.length() != 0);
		}
		/**Calculating translation amount and setting slider properties*/
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
		/**Drawing text*/
		for (int i = 0; i < text.size(); i++) {
			int drawY = textY + (10 * i) - (int)translateY;
			if (drawY > textY - 20 && drawY < (this.height * 7/8) - 10) {
				GlStateManager.pushMatrix();
				drawString(this.fontRenderer, text.get(i), textX, drawY, 0xFFFFFF);
				GlStateManager.popMatrix();
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawGuiBackgroundLayer();
		this.drawGuiForeGroundLayer();
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, 15.0F);
		super.drawScreen(mouseX, mouseY, partialTicks);
		GlStateManager.popMatrix();
	}

	@Override
	public void initGui() {
		this.buttonList.clear();
		this.nextPageBtn = this.addButton(new GuiButton(0, this.width / 2 + 100, this.height - 30, 20, 20, ">"));
		this.prevPageBtn = this.addButton(new GuiButton(1, this.width / 2 - 120, this.height - 30, 20, 20, "<"));
		this.slider = this.addButton(new GuiButton(2, 0, this.height * 1 / 8 + 16, 20, 20, "|||"));
		this.drawGuiBackgroundLayer();

		book = mc.player.inventory.getStackInSlot(itemSlot);
		if (book.hasTagCompound()) {
			NBTTagCompound nbtTagCompound = book.getTagCompound();
			this.page = nbtTagCompound.getInteger("page");
			this.sliderProgress = nbtTagCompound.getFloat("sliderProgress");
		} else {
			NBTTagCompound nbtTagCompound = new NBTTagCompound();
			this.page = 1;
			this.sliderProgress = 0.0F;
			nbtTagCompound.setInteger("page", 1);
			nbtTagCompound.setFloat("sliderProgress", sliderProgress);
			ItemGuideBook.setCurrentPage(itemSlot, mc.player, nbtTagCompound);
		}
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
		NBTTagCompound nbtTagCompound = new NBTTagCompound();
		nbtTagCompound.setInteger("page", 1);
		nbtTagCompound.setFloat("sliderProgress", sliderProgress);
		ItemGuideBook.setCurrentPage(itemSlot, mc.player, nbtTagCompound);
		super.onGuiClosed();
	}
}
