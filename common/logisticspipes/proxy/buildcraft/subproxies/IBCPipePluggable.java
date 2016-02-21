package logisticspipes.proxy.buildcraft.subproxies;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IBCPipePluggable {

	ItemStack[] getDropItems(LogisticsTileGenericPipe container);

	boolean isBlocking();

	Object getOriginal();

	@SideOnly(Side.CLIENT)
	void renderPluggable(RenderBlocks renderblocks, EnumFacing dir, int renderPass, int x, int y, int z);

	boolean isAcceptingItems(LPTravelingItemServer arrivingItem);

	LPTravelingItemServer handleItem(LPTravelingItemServer arrivingItem);

}
