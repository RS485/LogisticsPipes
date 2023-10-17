package logisticspipes.utils.gui;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;

import lombok.Getter;
import org.lwjgl.opengl.GL11;

public abstract class SubGuiScreen extends GuiScreen implements ISubGuiControler, IGuiAccess {

	@Getter
	protected int guiLeft;
	@Getter
	protected int guiTop;
	protected int xCenter;
	protected int yCenter;
	@Getter
	protected int right;
	@Getter
	protected int bottom;
	@Getter
	protected int xSize;
	@Getter
	protected int ySize;
	protected int xCenterOffset;
	protected int yCenterOffset;
	protected ISubGuiControler controler;
	private SubGuiScreen subGui;

	public SubGuiScreen(int xSize, int ySize, int xOffset, int yOffset) {
		this.xSize = xSize;
		this.ySize = ySize;
		xCenterOffset = xOffset;
		yCenterOffset = yOffset;
	}

	@Override
	public void initGui() {
		super.initGui();
		guiLeft = width / 2 - xSize / 2 + xCenterOffset;
		guiTop = height / 2 - ySize / 2 + yCenterOffset;

		right = width / 2 + xSize / 2 + xCenterOffset;
		bottom = height / 2 + ySize / 2 + yCenterOffset;

		xCenter = (right + guiLeft) / 2;
		yCenter = (bottom + guiTop) / 2;
	}

	public void register(ISubGuiControler gui) {
		controler = gui;
	}

	public void exitGui() {
		controler.resetSubGui();
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		if (par2 == 1) {
			exitGui();
		}
	}

	@Override
	public final void drawScreen(int mouseX, int mouseY, float partialTicks) {
		RenderHelper.disableStandardItemLighting();
		renderGuiBackground(mouseX, mouseY);
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableLighting();
		GlStateManager.disableDepth();
		super.drawScreen(mouseX, mouseY, partialTicks);
		RenderHelper.enableGUIStandardItemLighting();

		GlStateManager.pushMatrix();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableRescaleNormal();
		short short1 = 240;
		short short2 = 240;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) short1 / 1.0F, (float) short2 / 1.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		this.drawGuiContainerForegroundLayer(mouseX, mouseY, partialTicks);

		GlStateManager.popMatrix();

		GlStateManager.enableLighting();
		GlStateManager.enableDepth();
		if (subGui != null) {
			GL11.glPushAttrib(GL11.GL_DEPTH_BUFFER_BIT);
			if (!subGui.hasSubGui()) {
				GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
				super.drawDefaultBackground();
			}
			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
			subGui.drawScreen(mouseX, mouseY, partialTicks);
			GL11.glPopAttrib();
		}
		renderToolTips(mouseX, mouseY, partialTicks);
	}

	protected void renderToolTips(int mouseX, int mouseY, float par3) {}

	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY, float partialTick) {
		drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {}

	protected abstract void renderGuiBackground(int mouseX, int mouseY);

	@Override
	public final void handleMouseInput() throws IOException {
		if (subGui != null) {
			subGui.handleMouseInput();
		} else {
			handleMouseInputSub();
		}
	}

	public void handleMouseInputSub() throws IOException {
		super.handleMouseInput();
	}

	@Override
	public final void handleKeyboardInput() throws IOException {
		if (subGui != null) {
			subGui.handleKeyboardInput();
		} else {
			super.handleKeyboardInput();
		}
	}

	@Override
	public void setWorldAndResolution(Minecraft mc, int width, int height) {
		super.setWorldAndResolution(mc, width, height);
		if (subGui != null) {
			subGui.setWorldAndResolution(mc, width, height);
		}
	}

	@Override
	public void resetSubGui() {
		subGui = null;
	}

	@Override
	public boolean hasSubGui() {
		return subGui != null;
	}

	@Override
	public SubGuiScreen getSubGui() {
		return subGui;
	}

	@Override
	public void setSubGui(SubGuiScreen gui) {
		if (subGui == null) {
			subGui = gui;
			subGui.register(this);
			subGui.setWorldAndResolution(mc, width, height);
			subGui.initGui();
		}
	}

	@Override
	public LogisticsBaseGuiScreen getBaseScreen() {
		return controler.getBaseScreen();
	}

	@Override
	public Minecraft getMC() {
		return mc;
	}
}
