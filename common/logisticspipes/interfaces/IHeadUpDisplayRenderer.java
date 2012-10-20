package logisticspipes.interfaces;

import logisticspipes.hud.HUDConfig;
import net.minecraft.client.Minecraft;

public interface IHeadUpDisplayRenderer {
	public void renderHeadUpDisplay(double d,boolean day, Minecraft mc, HUDConfig config);
	public boolean display(HUDConfig config);
	public boolean cursorOnWindow(int x, int y);
	public void handleCursor(int x, int y);
}
