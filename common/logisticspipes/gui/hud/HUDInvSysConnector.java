package logisticspipes.gui.hud;

import logisticspipes.hud.HUDConfig;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.utils.gui.BasicGuiHelper;
import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

public class HUDInvSysConnector extends BasicHUDGui {
	
	private PipeItemsInvSysConnector pipe;
	private long display = System.currentTimeMillis();
	
	public HUDInvSysConnector(PipeItemsInvSysConnector pipe) {
		this.pipe = pipe;
	}
	
	@Override
	public void renderHeadUpDisplay(double distance, boolean day, Minecraft mc, HUDConfig config) {
		if(day) {
        	GL11.glColor4b((byte)64, (byte)64, (byte)64, (byte)64);
        } else {
        	GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)64);	
        }
		BasicGuiHelper.drawGuiBackGround(mc, -50, -50, 50, 50, 0, false);
		if(day) {
        	GL11.glColor4b((byte)64, (byte)64, (byte)64, (byte)127);
        } else {
        	GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)127);	
        }
		
		GL11.glTranslatef(0.0F, 0.0F, -0.005F);
		GL11.glScalef(1.5F, 1.5F, 0.0001F);
		String message = "Expected:";
		mc.fontRenderer.drawString(message , -28, -25, 0);
		GL11.glScalef(0.8F, 0.8F, -1F);
		
		BasicGuiHelper.renderItemIdentifierStackListIntoGui(pipe.displayList, null, 0, -37, -18, 3, 9, 18, 18, mc, true, true, true, true);
	}

	@Override
	public boolean display(HUDConfig config) {
		if(!config.isHUDInvSysCon()) return false;
		if(display > System.currentTimeMillis()) {
			return true;
		}
		if(pipe.displayList.size() > 0) {
			display = System.currentTimeMillis() + (2 * 1000);
		}
		return pipe.displayList.size() > 0;
	}

	@Override
	public boolean cursorOnWindow(int x, int y) {
		return (-50 < x && x < 50 && -50 < y && y < 50);
	}
}
