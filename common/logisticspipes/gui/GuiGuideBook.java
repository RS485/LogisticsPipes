package logisticspipes.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

import logisticspipes.LPConstants;
import logisticspipes.utils.GuideBookContents;
import network.rs485.logisticspipes.init.Items;
import network.rs485.logisticspipes.init.Packets;
import network.rs485.logisticspipes.item.GuideBookItem;
import network.rs485.logisticspipes.packet.GuideBookPagePacket;

public class GuiGuideBook extends Screen {

	private static final Identifier GUI_BOOK_TEXTURE = new Identifier(LPConstants.LP_MOD_ID, "textures/gui/guide_book.png");

	private ButtonWidget nextPageBtn;
	private ButtonWidget prevPageBtn;
	private ButtonWidget slider;

	private Hand hand;

	// Slider vars
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

	public GuiGuideBook(Hand hand, GuideBookContents gbc) {
		super(new TranslatableText("gui.logisticspipes.guide_book"));
		this.hand = hand;
		this.gbc = gbc;
		this.page = gbc.getPage(0);
	}

	@Override
	protected void init() {
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
		this.buttons.clear();

		this.nextPageBtn = this.addButton(new ButtonWidget(this.width / 2 + 100, this.height - 30, 20, 20, ">", _button -> {
			if ((page.getIndex() + 1) < gbc.getPages()) {
				setPage(page.getIndex() + 1);
			}
		}));

		this.prevPageBtn = this.addButton(new ButtonWidget(this.width / 2 - 120, this.height - 30, 20, 20, "<", _button -> {
			if ((page.getIndex() - 1) >= 0) {
				setPage(page.getIndex() - 1);
			}
		}));

		this.slider = this.addButton(new ButtonWidget(rightX - borderSize - sliderHeight, 0, sliderWidth, sliderHeight, "|||", _button -> {}));

		updateSliderPosition();

		// Getting information from item NBT
		if (needsInit) {
			ItemStack book = minecraft.player.getStackInHand(hand);
			if (book.getItem() != Items.GuideBook) {
				minecraft.openScreen(null);
				return;
			}

			if (book.hasTag()) {
				CompoundTag nbtTagCompound = book.getTag();
				GuideBookContents.Page p = gbc.getPage(nbtTagCompound.getInt("page"));
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
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		// Calculating translation amount and setting slider properties

		final List<String> text = splitLines(page.getText(), font, maxLength);

		if (text.size() <= maxLines) {
			slider.active = false;
			sliderProgress = 0;
			updateSliderPosition();
		} else {
			slider.active = true;
		}

		// Drawing Translated Text
		float translateY = sliderProgress * 10 * (text.size() - maxLines);
		for (int i = 0; i < text.size(); i++) {
			int drawY = textY + (10 * i) - (int) translateY;
			if (drawY > textY - 20 && drawY < (this.height * 7 / 8) - 10) {
				drawString(font, text.get(i), textX, drawY, 0xFFFFFF);
			}
		}
	}

	private void drawPageBorder() {
		int rightX = leftX + tileSize + acrossX;
		int bottomY = topY + tileSize + acrossY;

		RenderSystem.enableBlend();
		// Drawing Foreground Border
		minecraft.getTextureManager().bindTexture(GUI_BOOK_TEXTURE);
		// TODO
		// TopLeft, TopRight, BottomLeft & BottomRight
		// drawScaledCustomSizeModalRect(leftX, topY, 0, 0, tileSize, tileSize, tileSize, tileSize, textureSizeX, textureSizeY);
		// drawScaledCustomSizeModalRect(rightX, topY, tileSize, 0, tileSize, tileSize, tileSize, tileSize, textureSizeX, textureSizeY);
		// drawScaledCustomSizeModalRect(leftX, bottomY, 0, tileSize, tileSize, tileSize, tileSize, tileSize, textureSizeX, textureSizeY);
		// drawScaledCustomSizeModalRect(rightX, bottomY, tileSize, tileSize, tileSize, tileSize, tileSize, tileSize, textureSizeX, textureSizeY);
		// Top, Bottom, Left & Right
		// drawScaledCustomSizeModalRect(leftX + tileSize, topY, 24, 0, 8, tileSize, acrossX, tileSize, textureSizeX, textureSizeY);
		// drawScaledCustomSizeModalRect(leftX + tileSize, bottomY, 24, tileSize, 8, tileSize, acrossX, tileSize, textureSizeX, textureSizeY);
		// drawScaledCustomSizeModalRect(leftX, topY + tileSize, 0, 24, tileSize, 8, tileSize, acrossY, textureSizeX, textureSizeY);
		// drawScaledCustomSizeModalRect(rightX, topY + tileSize, tileSize, 24, tileSize, 8, tileSize, acrossY, textureSizeX, textureSizeY);
	}

	private void drawPageBackground() {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		minecraft.getTextureManager().bindTexture(GUI_BOOK_TEXTURE);
		// TODO
		// drawScaledCustomSizeModalRect(leftX + borderSize, topY + borderSize, tileSize * 2, 0, tileSize / 2, tileSize / 2, acrossX + tileSize, acrossY + tileSize, textureSizeX, textureSizeY);
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		renderBackground();
		drawPageBackground();
		drawPageContents();
		drawPageBorder();
		drawCenteredString(font, gbc.getTitle(), this.width / 2, this.height / 8 + 4, 0xFFFFFF);
		drawCenteredString(font, String.format("Page %d out of %d", page.getIndex() + 1, gbc.getPages()), this.width / 2, this.height - 25, 0xFFFFFF);
		super.render(mouseX, mouseY, delta);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void updateButtonStates() {
		this.nextPageBtn.active = page.getIndex() < gbc.getPages() - 1;
		this.prevPageBtn.active = page.getIndex() > 0;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dX, double dY) {
		if (slider.active) {
			slider.y = (int) MathHelper.clamp(slider.y + dY, sliderTopY, sliderBotY);
			sliderProgress = ((float) slider.y - sliderTopY) / (sliderBotY - sliderTopY);
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, dX, dY);
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
	public void onClose() {
		Packets.C2S.GuideBookPage.send(new GuideBookPagePacket(hand, sliderProgress, page.getIndex()));
		super.onClose();
	}

	/*
	 * Make the slider button follow the mouse if the click was performed over it as well as
	 * calculate sliderProgress based on the slider position between it's max and min Y.
	 */
	private void updateSliderPosition() {
		slider.y = (int) (sliderTopY + (sliderBotY - sliderTopY) * sliderProgress);
	}

	private static ArrayList<String> splitLines(String text, TextRenderer font, int maxLength) {
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
				curLength = font.getStringWidth(currentLine.toString());
			} else if (Character.isWhitespace(c)) {
				currentLine.append(c);
				curLength += font.getCharWidth(c);
				lastSplitPoint = currentLine.length();
			} else {
				if ((currentLine.length() > 1 && curLength + font.getCharWidth(c) > maxLength)) {
					if (lastSplitPoint == 0) lastSplitPoint = currentLine.length();
					lines.add(currentLine.substring(0, lastSplitPoint));
					currentLine.delete(0, lastSplitPoint);
					lastSplitPoint = 0;
					curLength = font.getStringWidth(currentLine.toString());
				}
				currentLine.append(c);
				curLength += font.getCharWidth(c);
			}
		}
		return lines;
	}

}
