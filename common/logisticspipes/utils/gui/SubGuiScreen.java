package logisticspipes.utils.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.opengl.GL11;

public class SubGuiScreen extends GuiScreen implements ISubGuiControler {
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
		this.xCenterOffset = xOffset;
		this.yCenterOffset = yOffset;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		this.guiLeft =  width/2 - xSize/2 + xCenterOffset;
		this.guiTop = height/2 - ySize/2  + yCenterOffset;
		
		this.right = width/2 + xSize/2 + xCenterOffset;
		this.bottom = height/2 + ySize/2 + yCenterOffset;
		
		this.xCenter = (right + guiLeft) / 2;
		this.yCenter = (bottom + guiTop) / 2;
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
	public void drawScreen(int par1, int par2, float par3){
		super.drawScreen(par1, par2, par3);
		if(subGui != null) {
			GL11.glTranslatef(0.0F, 0.0F, 1.0F);
			if(!subGui.hasSubGui()) {
		        GL11.glDisable(GL11.GL_DEPTH_TEST);
				super.drawDefaultBackground();
		        GL11.glEnable(GL11.GL_DEPTH_TEST);
			}
	        GL11.glEnable(GL11.GL_DEPTH_TEST);
			subGui.drawScreen(par1, par2, par3);
	        GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glTranslatef(0.0F, 0.0F, -1.0F);
		}
	}

	@Override
    public final void handleMouseInput() {
		if(subGui != null) {
			subGui.handleMouseInput();
		} else {
			this.handleMouseInputSub();
		}
    }
	
	public void handleMouseInputSub() {
		super.handleMouseInput();
	}
	
	@Override
	public final void handleKeyboardInput() {
		if(subGui != null) {
			subGui.handleKeyboardInput();
		} else {
			this.handleKeyboardInputSub();
		}
	}

	public void handleKeyboardInputSub() {
		super.handleKeyboardInput();
	}

	@Override
	public void setSubGui(SubGuiScreen gui) {
		if(subGui == null) {
			subGui = gui;
			subGui.setWorldAndResolution(this.mc, this.width, this.height);
			subGui.register(this);
			subGui.initGui();
		}
	}
	
	@Override
	public void setWorldAndResolution(Minecraft mc, int width, int height) {
		super.setWorldAndResolution(mc, width, height);
		if(subGui != null)
			subGui.setWorldAndResolution(mc, width, height);
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
}
