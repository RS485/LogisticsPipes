package logisticspipes.proxy.interfaces;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IIronChestProxy {

	boolean isIronChest(TileEntity tile);

	@SideOnly(Side.CLIENT)
	boolean isChestGui(GuiScreen gui);
}
