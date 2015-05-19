package logisticspipes.utils.gui;

import net.minecraft.client.gui.Gui;

public final class SimpleGraphics {
	private SimpleGraphics() {
	}

	public static void drawHorizontalLine(int par1, int par2, int par3, int par4, int thickness) {
		if(par2 < par1) {
			int i1 = par1;
			par1 = par2;
			par2 = i1;
		}

		Gui.drawRect(par1, par3, par2 + 1, par3 + thickness, par4);
	}

	public static void drawVerticalLine(int par1, int par2, int par3, int par4, int thickness) {
		if(par3 < par2) {
			int i1 = par2;
			par2 = par3;
			par3 = i1;
		}

		Gui.drawRect(par1, par2 + 1, par1 + thickness, par3, par4);
	}
}
