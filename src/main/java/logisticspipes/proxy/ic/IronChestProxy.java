package logisticspipes.proxy.ic;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import cpw.mods.ironchest.client.gui.chest.GUIChest;
import cpw.mods.ironchest.common.tileentity.chest.TileEntityIronChest;

import logisticspipes.proxy.interfaces.IIronChestProxy;

public class IronChestProxy implements IIronChestProxy {

	@Override
	public boolean isIronChest(TileEntity tile) {
		return tile instanceof TileEntityIronChest;
	}

	@Override
	public @SideOnly(Side.CLIENT)
	boolean isChestGui(GuiScreen gui) {
		return gui instanceof GUIChest;
	}
}
