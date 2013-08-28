package logisticspipes.utils.gui.hud;

import logisticspipes.interfaces.IHUDButton;
import logisticspipes.utils.gui.BasicGuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

public abstract class BasicHUDButton implements IHUDButton {

	protected final int posX;
	protected final int posY;
	protected final int sizeX;
	protected final int sizeY;
	protected long focusedTimeStart = 0;
	protected final String label;
	
	public BasicHUDButton(String name, int x, int y, int width, int heigth) {
		label = name;
		posX = x;
		posY = y;
		sizeX = width;
		sizeY = heigth;
	}
	
	@Override
	public int getX() {
		return posX;
	}

	@Override
	public int getY() {
		return posY;
	}

	@Override
	public int sizeX() {
		return sizeX;
	}

	@Override
	public int sizeY() {
		return sizeY;
	}

	@Override
	public void blockFocused() {
		focusedTimeStart = -1;
	}

	@Override
	public boolean isblockFocused() {
		return focusedTimeStart == -1;
	}

	@Override
	public void setFocused() {
		focusedTimeStart = System.currentTimeMillis();
	}

	@Override
	public boolean isFocused() {
		return focusedTimeStart != 0;
	}

	@Override
	public void clearFocused() {
		focusedTimeStart = 0;
	}

	@Override
	public int focusedTime() {
		return (int) (System.currentTimeMillis() - focusedTimeStart);
	}
	
	
	private static final ResourceLocation ITEMS = new ResourceLocation("textures/gui/icons.png");
	
			
	private static final ResourceLocation GUI = new ResourceLocation("textures/gui/widgets.png");
			
	
	
	@Override
	public void renderButton(boolean hover, boolean clicked) {
		Minecraft minecraft = FMLClientHandler.instance().getClient();
        //GL11.glBindTexture(3553 /*GL_TEXTURE_2D*/, 
        minecraft.renderEngine.func_110577_a(GUI); //TODO: check this change
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int k = !buttonEnabled() ? 0 : hover ? 2 : 1;
        
        BasicGuiHelper.drawTexturedModalRect(posX  			, posY				, 0			    , 46 + k * 20, sizeX / 2 ,sizeY / 2, 0);
        BasicGuiHelper.drawTexturedModalRect(posX + sizeX / 2	, posY				, 200 - sizeX / 2, 46 + k * 20, sizeX / 2, sizeY / 2, 0);
        
        BasicGuiHelper.drawTexturedModalRect(posX  			, posY + sizeY / 2, 0			    , 46 + 24 - sizeY  +  k * 20, sizeX / 2 ,sizeY / 2, 0);
        BasicGuiHelper.drawTexturedModalRect(posX + sizeX / 2	, posY + sizeY / 2, 200 - sizeX / 2, 46 + 24 - sizeY +  k * 20, sizeX / 2, sizeY / 2, 0);

		GL11.glTranslatef(0.0F, 0.0F, -0.001F);
		int color = 0;
        if(hover && !clicked) {
        	color = 0xffffa0;
    		GL11.glTranslatef(0.0F, 0.0F, -0.02F);
        } else if(!clicked) {
        	color = 0xffffa0;
        } else  {
        	color = 0xe0e0e0;
        }
        minecraft.fontRenderer.drawString(label , -(minecraft.fontRenderer.getStringWidth(label) / 2) + posX + sizeX / 2, posY + (sizeY - 8) / 2, color);
		if(hover && !clicked) {
			GL11.glTranslatef(0.0F, 0.0F, 0.02F);
		}
		GL11.glTranslatef(0.0F, 0.0F, 0.001F);
		minecraft.renderEngine.func_110577_a(ITEMS);
	}

	@Override
	public void renderAlways() {
		
	}
}
