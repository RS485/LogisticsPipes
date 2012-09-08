package logisticspipes.gui.hud;

import org.lwjgl.opengl.GL11;

import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.utils.gui.BasicGuiHelper;
import net.minecraft.client.Minecraft;

public class HUDProvider extends BasicHUDGui {
	
	private final PipeItemsProviderLogistics pipe;
	
	public HUDProvider(PipeItemsProviderLogistics pipe) {
		this.pipe = pipe;
	}


	@Override
	public void renderHeadUpDisplay(double d, boolean day, Minecraft mc) {
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
		GL11.glTranslatef(0.0F, 0.0F, -0.005F);
		GL11.glScalef(1.5F, 1.5F, 0.0001F);
		GL11.glScalef(0.8F, 0.8F, -1F);
		BasicGuiHelper.renderItemIdentifierStackListIntoGui(pipe.itemList, null, 0, -35, -35, 4, 16, 18, 18, mc, true, true);
	}

	@Override
	public boolean display() {
		return pipe.itemList.size() > 0;
	}
	
	@Override
	public boolean cursorOnWindow(int x, int y) {
		return false;
	}

	@Override
	public void handleCursor(int x, int y) {
		super.handleCursor(x, y);
	}
}
