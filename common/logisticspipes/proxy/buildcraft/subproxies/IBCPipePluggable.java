package logisticspipes.proxy.buildcraft.subproxies;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public interface IBCPipePluggable {

	ItemStack[] getDropItems(LogisticsTileGenericPipe container);
	boolean isBlocking();
	Object getOriginal();
	void renderPluggable(RenderBlocks renderblocks, ForgeDirection dir, int renderPass, int x, int y, int z);
	
}
