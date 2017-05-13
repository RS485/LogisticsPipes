package logisticspipes.proxy.buildcraft.subproxies;

import net.minecraft.item.ItemStack;

import net.minecraft.util.EnumFacing;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;

public interface IBCPipePluggable {

	ItemStack[] getDropItems(LogisticsTileGenericPipe container);

	boolean isBlocking();

	Object getOriginal();

	@SideOnly(Side.CLIENT)
	void renderPluggable(EnumFacing dir, int renderPass, int x, int y, int z);

	boolean isAcceptingItems(LPTravelingItemServer arrivingItem);

	LPTravelingItemServer handleItem(LPTravelingItemServer arrivingItem);
}
