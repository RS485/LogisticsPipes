package logisticspipes.utils.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.List;

public abstract class SubGuiScreen extends GuiScreen implements ISubGuiControler {

	protected int guiLeft;
	protected int guiTop;
	protected int xCenter;
	protected int yCenter;
	protected int right;
	protected int bottom;
	protected int xSize;
	protected int ySize;
	protected int xCenterOffset;
	protected int yCenterOffset;

	private SubGuiScreen subGui;

	protected ISubGuiControler controler;

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
	public final void drawScreen(int par1, int par2, float par3) {
		RenderHelper.disableStandardItemLighting();
		renderGuiBackground(par1, par2);
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		super.drawScreen(par1, par2, par3);
		RenderHelper.enableGUIStandardItemLighting();

		GL11.glPushMatrix();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		short short1 = 240;
		short short2 = 240;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) short1 / 1.0F, (float) short2 / 1.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		this.drawGuiContainerForegroundLayer(par1, par2);

		GL11.glPopMatrix();

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		if (subGui != null) {
			GL11.glPushAttrib(GL11.GL_DEPTH_BUFFER_BIT);
			if (!subGui.hasSubGui()) {
				GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
				super.drawDefaultBackground();
			}
			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
			subGui.drawScreen(par1, par2, par3);
			GL11.glPopAttrib();
		}
		renderToolTips(par1, par2, par3);
	}

	protected void renderToolTips(int mouseX, int mouseY, float par3) {}

	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {}

	protected abstract void renderGuiBackground(int par1, int par2);

	@Override
	public final void handleMouseInput() {
		if (subGui != null) {
			subGui.handleMouseInput();
		} else {
			handleMouseInputSub();
		}
	}

	public void handleMouseInputSub() {
		super.handleMouseInput();
	}

	@Override
	public final void handleKeyboardInput() {
		if (subGui != null) {
			subGui.handleKeyboardInput();
		} else {
			super.handleKeyboardInput();
		}
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
	public LogisticsBaseGuiScreen getBaseScreen() {
		return controler.getBaseScreen();
	}

}
