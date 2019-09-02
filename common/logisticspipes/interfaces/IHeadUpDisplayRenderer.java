package logisticspipes.interfaces;

import net.minecraft.client.Minecraft;

public interface IHeadUpDisplayRenderer {

	void renderHeadUpDisplay(double d, boolean day, boolean shifted, Minecraft mc, IHUDConfig config);

	boolean display(IHUDConfig config);

	boolean cursorOnWindow(int x, int y);

	void handleCursor(int x, int y);
}
