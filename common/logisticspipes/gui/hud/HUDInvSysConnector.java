package logisticspipes.gui.hud;

import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.utils.gui.BasicGuiHelper;
import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

public class HUDInvSysConnector extends BasicHUDGui {
	
	private PipeItemsInvSysConnector pipe;
	private int cursorX = 0;
	private int cursorY = 0;
	
	public HUDInvSysConnector(PipeItemsInvSysConnector pipe) {
		this.pipe = pipe;
	}
	
	@Override
	public void renderHeadUpDisplay(double distance, boolean day, Minecraft mc) {
		if(day) {
        	GL11.glColor4b((byte)64, (byte)64, (byte)64, (byte)64);
        } else {
        	GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)64);	
        }
		BasicGuiHelper.drawGuiBackGround(mc, -50, -50, 50, 50, 0);
		if(day) {
        	GL11.glColor4b((byte)64, (byte)64, (byte)64, (byte)127);
        } else {
        	GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)127);	
        }
		
		GL11.glTranslatef(0.0F, 0.0F, -0.001F);
		
		GL11.glPushMatrix();
		BasicGuiHelper.drawRect(cursorX - 2, -50, cursorX + 2, 50, 0xff000000);
		BasicGuiHelper.drawRect(-50, cursorY - 2, 50, cursorY + 2, 0xff000000);
		GL11.glPopMatrix();
		
		BasicGuiHelper.renderItemIdentifierStackListIntoGui(pipe.getExpectedItems(), null, 0, 9, 29, 3, 9, 18, 18, mc, true, true);
	}

	@Override
	public boolean display() {
		return false;
	}

	@Override
	public boolean cursorOnWindow(int x, int y) {
		return (-50 < x && x < 50 && -50 < y && y < 50);
	}

	@Override
	public void handleCursor(int x, int y) {
		super.handleCursor(x, y);
		cursorX = x;
		cursorY = y;
	}

}
