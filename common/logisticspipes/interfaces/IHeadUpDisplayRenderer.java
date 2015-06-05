package logisticspipes.interfaces;

import net.minecraft.client.Minecraft;

public interface IHeadUpDisplayRenderer {

	public void renderHeadUpDisplay(double d, boolean day, boolean shifted, Minecraft mc, IHUDConfig config);

	public boolean display(IHUDConfig config);

	public boolean cursorOnWindow(int x, int y);

	public void handleCursor(int x, int y);
}
