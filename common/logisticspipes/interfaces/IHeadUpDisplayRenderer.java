package logisticspipes.interfaces;

import net.minecraft.client.Minecraft;

public interface IHeadUpDisplayRenderer {
	public void renderHeadUpDisplay(double d,boolean day, Minecraft mc);
	public boolean display();
	public boolean cursorOnWindow(int x, int y);
	public void handleCursor(int x, int y);
}
