package logisticspipes.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.Sys;

import logisticspipes.LPConstants;
import logisticspipes.utils.string.StringUtils;

public class GuiGuideBook extends GuiScreen {

	private static final ResourceLocation GUI_BOOK_TEXTURE = new ResourceLocation(LPConstants.LP_MOD_ID, "textures/gui/guide_book.png");

	private GuiButton nextPageBtn, prevPageBtn, slider;
	private boolean dragging = false;
	private int mouseX, mouseY;

	//Book vars
	private int PAGE = 1;
	private int PAGE_MAX = 10;
	private int MAX_LENGTH = 50;

	public GuiGuideBook() {
		super();
	}

	/** Draw a background container sized based on x, and y size. This is not 1:1 scale.*/
	protected void drawGuiBackgroundLayer(int x, int y) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(GUI_BOOK_TEXTURE);
		int xRem = x;
		int yRem = y;
		int eight = 8;
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, -2.0F);
		this.drawDefaultBackground();
		GlStateManager.popMatrix();
		GlStateManager.pushMatrix();
		GlStateManager.translate(-x*4, -y*4, -1.0F);
		/**This is the darker background like the inventory has. Check whether this is wanted or not.*/
		GlStateManager.translate(this.width / 2, this.height / 2, 0.0F);
		/**TopLeft*/
		drawTexturedModalRect(0, 0, 0, 0, eight * 4, eight * 4);
		xRem -= 4;
		/**TopRight*/
		drawTexturedModalRect(eight * (x - 4), 0, eight * 4, 0, eight * 4, eight * 4);
		xRem -= 4;
		/**BottomLeft*/
		drawTexturedModalRect(0, eight * (y - 4), 0, eight * 4, eight * 4, eight * 4);
		yRem -= 4;
		/**BottomRight*/
		yRem -= 4;
		drawTexturedModalRect(eight * (x - 4), eight * (y - 4), eight * 4, eight * 4, eight * 4, eight * 4);
		/** Top & Bottom/ */
		for(int i = 0; i < xRem; i++){
			drawTexturedModalRect(eight * (i + 4), 0, eight * 3, 0, eight * 2, eight * 4);
			drawTexturedModalRect(eight * (i + 4), eight * (y - 4), eight * 3, eight * 4, eight * 2, eight * 4);
		}
		/**Left, Right & Background*/
		for(int j = 0; j < yRem; j++) {
			drawTexturedModalRect(0, eight * (j + 4), 0, eight * 3, eight * 4, eight * 2);
			drawTexturedModalRect(eight * (x - 4), eight * (j + 4), eight * 4, eight * 3, eight * 4, eight * 2);
			for(int i = 0; i < xRem; i++){
				drawTexturedModalRect(eight * (i + 4), eight * (j + 4), eight * 3, eight * 3, eight * 2, eight * 2);
			}
		}
		GlStateManager.popMatrix();
	}

	public void drawGuiForeGroundLayer(){
		GlStateManager.pushMatrix();
		GlStateManager.translate(1.0F, 1.0F, 10.0F);
		drawCenteredString(this.fontRenderer, StringUtils.translate("book.quickstart.title").trim(), this.width / 2, (((-((this.height/4)*3)/8)*4)+this.height/2) + 4, 16777215);
		drawCenteredString(this.fontRenderer, "Page " + PAGE + " out of " + PAGE_MAX, this.width / 2, this.height - 25, 16777215);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		String delimiter = "\\\\n";
		String currentPage = StringUtils.translate("book.quickstart." + PAGE);
		MAX_LENGTH = this.width - 75 - (((-((this.width/4)*3)/8)*4)+this.width/2)*2;
		String[] currentPageLines = currentPage.split(delimiter);
		String line;
		int line_number = 0;
		int length = 0;
		for(String sentence : currentPageLines){
			do{
				line = fontRenderer.trimStringToWidth(sentence, MAX_LENGTH);
				length = line.length();
				if(fontRenderer.getStringWidth(sentence) > MAX_LENGTH){
					line = fontRenderer.trimStringToWidth(sentence, MAX_LENGTH);
					if(line.charAt(line.length()-1) != ' '){
						if(line.charAt(line.length()-2) != ' '){
							line = line.substring(0, line.length()-1) + '-';
							length-=1;
						}else{
							line = line.substring(0, line.length()-1) + ' ';
							length-=1;
						}
					}
					if(line.charAt(0) == ' ') line = line.substring(1);
					sentence = sentence.substring(length);
				}
				else{
					line = sentence;
					sentence = "";
				}
				drawString(this.fontRenderer, line, (((-((this.width/4)*3)/8)*4)+this.width/2) + 24, (((-((this.height/4)*3)/8)*4)+this.height/2) + 24 + (10 * line_number++), 16777215);
			}while(sentence.length() != 0);
			line_number++;
		}
		GlStateManager.popMatrix();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		this.drawGuiBackgroundLayer(((this.width/4)*3)/8, ((this.height/4)*3)/8);
		this.drawGuiForeGroundLayer();
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void initGui() {
		this.buttonList.clear();
		this.nextPageBtn = this.addButton(new GuiButton(0, this.width / 2 +100, this.height - 30 , 20, 20,">"));
		this.prevPageBtn = this.addButton(new GuiButton(1, this.width / 2 -120, this.height - 30, 20, 20,"<"));
		this.slider = this.addButton(new GuiButton(2, this.width - 30, 30, 20, 20, "|||"));
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if(button.id == 0) {
			if(PAGE < PAGE_MAX) PAGE++;
		}
		if(button.id == 1) {
			if(PAGE > 1) PAGE--;
		}
	}

	protected void updateSlider(GuiButton button){
		if(dragging){
			slider.y = mouseY - slider.height/2;
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if(slider.isMouseOver()) this.dragging = true;
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		/** Only needed for slider implementation*/
		updateSlider(slider);
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		this.dragging = false;
		super.mouseReleased(mouseX, mouseY, state);
	}
}
