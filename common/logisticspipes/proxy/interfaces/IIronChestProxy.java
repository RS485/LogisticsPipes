package logisticspipes.proxy.interfaces;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IIronChestProxy {

	public boolean isIronChest(TileEntity tile);

	public @SideOnly(Side.CLIENT) boolean isChestGui(GuiScreen gui);
}
