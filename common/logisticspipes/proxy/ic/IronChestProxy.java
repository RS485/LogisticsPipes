package logisticspipes.proxy.ic;

import logisticspipes.proxy.interfaces.IIronChestProxy;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import cpw.mods.ironchest.TileEntityIronChest;
import cpw.mods.ironchest.client.GUIChest;

public class IronChestProxy implements IIronChestProxy {

	@Override
	public boolean isIronChest(TileEntity tile) {
		return tile instanceof TileEntityIronChest;
	}

	@Override
	public @SideOnly(Side.CLIENT) boolean isChestGui(GuiScreen gui) {
		return gui instanceof GUIChest;
	}
}
