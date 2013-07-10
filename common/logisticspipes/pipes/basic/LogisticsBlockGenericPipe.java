package logisticspipes.pipes.basic;

import java.util.ArrayList;

import logisticspipes.LogisticsPipes;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import buildcraft.transport.BlockGenericPipe;

public class LogisticsBlockGenericPipe extends BlockGenericPipe {

	public LogisticsBlockGenericPipe(int i) {
		super(i);
	}

	@Override
	public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> result = super.getBlockDropped(world, x, y, z, metadata, fortune);
		for(int i=0;i<result.size();i++) {
			ItemStack stack = result.get(i);
			if(stack.itemID == LogisticsPipes.LogisticsBrokenItem.itemID) {
				result.remove(i);
				i--;
			}
		}
		return result;
	}
}
