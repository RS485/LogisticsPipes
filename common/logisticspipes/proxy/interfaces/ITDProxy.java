package logisticspipes.proxy.interfaces;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.td.subproxies.ITDPart;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ITDProxy {

	ITDPart getTDPart(LogisticsTileGenericPipe pipe);

	boolean isActive();

	void registerPipeInformationProvider();

	boolean isItemDuct(TileEntity tile);

	@SideOnly(Side.CLIENT)
	void renderPipeConnections(LogisticsTileGenericPipe pipeTile, RenderBlocks renderer);

	@SideOnly(Side.CLIENT)
	void registerTextures(IIconRegister iconRegister);

	boolean isBlockedSide(TileEntity with, EnumFacing opposite);
}
