package logisticspipes.utils.gui;

import net.minecraft.client.Minecraft;

public interface IGuiAccess {

	int getGuiLeft();

	int getGuiTop();

	int getXSize();

	int getYSize();

	int getRight();

	int getBottom();

	Minecraft getMC();
}
